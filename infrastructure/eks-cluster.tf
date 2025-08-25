module "eks" {
  source          = "terraform-aws-modules/eks/aws"
  version         = "20.8.4"
  cluster_name    = local.cluster_name
  cluster_version = var.kubernetes_version
  subnet_ids      = module.vpc.private_subnets
  vpc_id          = module.vpc.vpc_id
  enable_irsa     = true
  cluster_endpoint_public_access  = true
  cluster_endpoint_private_access = false

  tags = {
    cluster = "eks-demo"
  }
  
  cluster_encryption_config = [{
    resources        = ["secrets"]
    provider_key_arn = aws_kms_key.eks_secrets.arn
  }]

  eks_managed_node_group_defaults = {
    ami_type               = "AL2_x86_64"
    instance_types         = ["t3.medium"]
    vpc_security_group_ids = [aws_security_group.all_worker_mgmt.id]
  }

  eks_managed_node_groups = {

    node_group = {
      min_size     = 1
      max_size     = 3
      desired_size = 2
    }
  }
}

