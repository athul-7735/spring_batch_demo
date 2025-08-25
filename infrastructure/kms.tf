data "aws_caller_identity" "current" {}

resource "aws_kms_key" "eks_secrets" {
  description         = "KMS key for encrypting Kubernetes secrets (EKS)"
  enable_key_rotation = true

  policy = jsonencode({
    Version = "2012-10-17",
    Statement = [
      {
        Sid       = "AllowRootAccountAdmin",
        Effect    = "Allow",
        Principal = {
          AWS = "arn:aws:iam::${data.aws_caller_identity.current.account_id}:root"
        },
        Action   = "kms:*",
        Resource = "*"
      },
      {
        Sid       = "AllowEKSToUseKey",
        Effect    = "Allow",
        Principal = { Service = "eks.amazonaws.com" },
        Action    = ["kms:CreateGrant","kms:DescribeKey"],
        Resource  = "*",
        Condition = {
          StringEquals = {
            "kms:ViaService" = "eks.${var.aws_region}.amazonaws.com"
          }
        }
      }
    ]
  })
}
