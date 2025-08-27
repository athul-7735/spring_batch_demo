# EKS Access Entry: Grants IAM principal access to the cluster
resource "aws_eks_access_entry" "eks_admin_access" {
  cluster_name  = module.eks.cluster_name
  principal_arn = "arn:aws:iam::557690581666:user/Athul_IAM"
}

# Attach predefined cluster admin access policy
resource "aws_eks_access_policy_association" "eks_admin_policy" {
  cluster_name  = module.eks.cluster_name
  principal_arn = aws_eks_access_entry.eks_admin_access.principal_arn
  policy_arn    = "arn:aws:eks::aws:cluster-access-policy/AmazonEKSClusterAdminPolicy"

  access_scope {
    type = "cluster"
  }

  depends_on = [aws_eks_access_entry.eks_admin_access, module.eks]
}

resource "aws_iam_policy" "ecr_pull_policy" {
  name        = "ECRPullPolicy"
  description = "Allows pulling images from Amazon ECR"
  
  policy = jsonencode({
    Version = "2012-10-17",
    Statement = [{
      Effect   = "Allow",
      Action   = [
        "ecr:GetAuthorizationToken",
        "ecr:BatchCheckLayerAvailability",
        "ecr:GetDownloadUrlForLayer",
        "ecr:BatchGetImage"
      ],
       "Resource": [
        "arn:aws:ecr:eu-west-1:557690581666:repository/spring-batch-eks"
      ]
    }]
  })
}

resource "aws_iam_role_policy_attachment" "attach_ecr_policy_to_nodes" {
  role       = module.eks.eks_managed_node_groups["node_group"].iam_role_name
  policy_arn = aws_iam_policy.ecr_pull_policy.arn

  depends_on = [aws_eks_access_entry.eks_admin_access, module.eks, aws_eks_access_policy_association.eks_admin_policy]
}


resource "aws_iam_policy" "spring_batch_s3_access_policy" {
  name        = "SpringBatchS3AccessPolicy"
  description = "Allows Spring Batch app to access S3 objects in a specific bucket"

  policy = jsonencode({
    Version = "2012-10-17",
    Statement = [
      {
        Effect = "Allow",
        Action = [
          "s3:GetObject",
          "s3:ListBucket"
        ],
        Resource = [
          "arn:aws:s3:::batch-process-store",
          "arn:aws:s3:::batch-process-store/*"
        ]
      }
    ]
  })
}

resource "aws_iam_role_policy_attachment" "attach_s3_policy_to_nodes" {
  role       = module.eks.eks_managed_node_groups["node_group"].iam_role_name
  policy_arn = aws_iam_policy.spring_batch_s3_access_policy.arn

  depends_on = [
    aws_iam_policy.spring_batch_s3_access_policy,
    module.eks
  ]
}

resource "aws_iam_policy" "kms_decrypt_policy" {
  name        = "KMSDecryptPolicy"
  description = "Allows EKS nodes to decrypt data with the batch KMS key"

  policy = jsonencode({
    Version = "2012-10-17",
    Statement = [
      {
        Effect = "Allow",
        Action = [
          "kms:Decrypt"
        ],
        Resource = "arn:aws:kms:eu-west-1:557690581666:key/e43b2237-096c-412c-a1cd-76fb0d3b6d13"
      }
    ]
  })
}

# resource "aws_iam_role_policy_attachment" "attach_kms_policy_to_nodes" {
#   role       = module.eks.eks_managed_node_groups["node_group"].iam_role_name
#   policy_arn = aws_iam_policy.kms_decrypt_policy.arn

#   depends_on = [aws_iam_policy.kms_decrypt_policy, module.eks]
# }



data "aws_eks_cluster" "this" {
  name = module.eks.cluster_name
  depends_on = [module.eks]
}

data "aws_eks_cluster_auth" "this" {
  name = module.eks.cluster_name
  depends_on = [module.eks]
}

data "aws_iam_openid_connect_provider" "oidc" {
  arn = module.eks.oidc_provider_arn
}


resource "aws_iam_role" "spring_batch_irsa" {
  name               = "SpringBatchIRSA"
  assume_role_policy = jsonencode({
    Version = "2012-10-17",
    Statement = [{
      Effect = "Allow",
      Principal = {
        Federated = module.eks.oidc_provider_arn
      },
      Action = "sts:AssumeRoleWithWebIdentity",
      Condition = {
        StringEquals = {
          "${replace(data.aws_iam_openid_connect_provider.oidc.url, "https://", "")}:sub" = "system:serviceaccount:batch:spring-batch"
        }
      }
    }]
  })
}

resource "aws_iam_policy" "spring_batch_minimal" {
  name        = "SpringBatchMinimalS3KMS"
  description = "Minimal S3/KMS access for Spring Batch"
  policy      = jsonencode({
    Version = "2012-10-17",
    Statement = [
      {
        Effect   = "Allow",
        Action   = ["s3:GetObject", "s3:ListBucket","kms:Decrypt"],
        Resource = [
          "arn:aws:s3:::batch-process-store",
          "arn:aws:s3:::batch-process-store/*",
          "arn:aws:kms:eu-west-1:557690581666:key/e43b2237-096c-412c-a1cd-76fb0d3b6d13"
        ]
      },
      {
        Effect   = "Allow",
        Action   = ["kms:Decrypt"],
        Resource = "arn:aws:kms:eu-west-1:557690581666:key/e43b2237-096c-412c-a1cd-76fb0d3b6d13"
      }
    ]
  })
}

resource "aws_iam_role_policy_attachment" "spring_batch_attach" {
  role       = aws_iam_role.spring_batch_irsa.name
  policy_arn = aws_iam_policy.spring_batch_minimal.arn
}

data "aws_kms_key" "batch_key" {
  key_id = "arn:aws:kms:eu-west-1:557690581666:key/e43b2237-096c-412c-a1cd-76fb0d3b6d13"
}

resource "aws_kms_key_policy" "allow_spring_batch_irsa" {  # CHANGE: Added to let IRSA role use the KMS key
  key_id = data.aws_kms_key.batch_key.key_id

  policy = jsonencode({
    Version = "2012-10-17",
    Id      = "spring-batch-irsa-policy",
    Statement = [
      {
        Sid       = "AllowRootAccount",
        Effect    = "Allow",
        Principal = { AWS = "arn:aws:iam::557690581666:root" },
        Action    = "kms:*",
        Resource  = "*"
      },
      {
        Sid       = "AllowSpringBatchIRSADecrypt",  # CHANGE: New statement for IRSA role
        Effect    = "Allow",
        Principal = {
          AWS = aws_iam_role.spring_batch_irsa.arn
        },
        Action = [
          "kms:Decrypt",
          "kms:DescribeKey"
        ],
        Resource = "*"
      }
    ]
  })
}

# resource "kubernetes_service_account" "spring_batch" { 
#   metadata {
#     name      = "spring-batch"
#     annotations = {
#       "eks.amazonaws.com/role-arn" = aws_iam_role.spring_batch_irsa.arn 
#     }
#   }
#   depends_on = [
#     module.eks
#   ]
# }

resource "aws_iam_role" "cicd_deployer_irsa" {
  name               = "CICDDeployerIRSA"
  assume_role_policy = jsonencode({
    Version = "2012-10-17",
    Statement = [{
      Effect = "Allow",
      Principal = {
        Federated = module.eks.oidc_provider_arn
      },
      Action = "sts:AssumeRoleWithWebIdentity",
      Condition = {
        StringEquals = {
          "${replace(data.aws_iam_openid_connect_provider.oidc.url, "https://", "")}:sub" = "system:serviceaccount:cicd:cicd-deployer"
        }
      }
    }]
  })
}

resource "aws_iam_policy" "cicd_deployer_ecr" {
  name        = "CICDDeployerECRPullOnly"
  description = "ECR read-only for CICD deployer"
  policy      = jsonencode({
    Version = "2012-10-17",
    Statement = [
      {
        Effect   = "Allow",
        Action   = [
          "ecr:GetAuthorizationToken",
          "ecr:BatchCheckLayerAvailability",
          "ecr:GetDownloadUrlForLayer",
          "ecr:BatchGetImage"
        ],
        Resource = [
          "arn:aws:ecr:eu-west-1:557690581666:repository/spring-batch-eks",
        ]
      }
    ]
  })
}

resource "aws_iam_role_policy_attachment" "cicd_deployer_attach" {
  role       = aws_iam_role.cicd_deployer_irsa.name
  policy_arn = aws_iam_policy.cicd_deployer_ecr.arn
}


# Delay resource (waits for IAM access propagation)
resource "time_sleep" "wait_for_eks_access" {
  depends_on = [
    aws_eks_access_policy_association.eks_admin_policy
  ]
  create_duration = "60s"
}

resource "kubernetes_service_account" "no_kms_sa" {
  metadata {
    name      = "no-kms-access"
    namespace = "default"
  }
  depends_on = [
    module.eks,
    aws_eks_access_entry.eks_admin_access,
    aws_eks_access_policy_association.eks_admin_policy,
    time_sleep.wait_for_eks_access
  ]
}