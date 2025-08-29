# IAM Role for EBS CSI driver
resource "aws_iam_role" "ebs_csi_driver" {
  name = "AmazonEKS_EBS_CSI_DriverRole"

  assume_role_policy = jsonencode({
    Version = "2012-10-17",
    Statement = [
      {
        Effect = "Allow",
        Principal = {
          Federated = module.eks.oidc_provider_arn
        },
        Action = "sts:AssumeRoleWithWebIdentity",
        Condition = {
          StringEquals = {
            "${module.eks.oidc_provider}:sub" = "system:serviceaccount:kube-system:ebs-csi-controller-sa"
          }
        }
      }
    ]
  })
}

# Attach EBS CSI policy to the role
resource "aws_iam_role_policy_attachment" "ebs_csi_driver_attach" {
  role       = aws_iam_role.ebs_csi_driver.name
  policy_arn = "arn:aws:iam::aws:policy/service-role/AmazonEBSCSIDriverPolicy"
}

# Deploy the EBS CSI driver addon
resource "aws_eks_addon" "ebs_csi" {
  cluster_name                  = module.eks.cluster_name
  addon_name                    = "aws-ebs-csi-driver"
  addon_version                 = "v1.28.0-eksbuild.1"
  service_account_role_arn      = aws_iam_role.ebs_csi_driver.arn
  resolve_conflicts_on_create   = "OVERWRITE"
  resolve_conflicts_on_update   = "OVERWRITE"

  depends_on = [aws_iam_role_policy_attachment.ebs_csi_driver_attach, module.eks]
}
