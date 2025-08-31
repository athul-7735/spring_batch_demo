# spring_batch_demo

This repository contains the implementation code for the MSc Dissertation on  
**A Zero Trust Framework for Security Enhancement in Kubernetes Based Batch Processing System** 

The project demonstrates how Zero Trust principles can be applied to Kubernetes-based batch processing workloads using Spring Batch, AWS EKS, Terraform, and GitHub CICD pipelines. It integrates multiple security tools to enforce least privilege, runtime monitoring, image signing, and secrets management.

---

## Features

- **Spring Batch Application**: Reads data from CSV, processes it, and persists into MySQL.  
- **Containerization**: Packaged as Docker image for portability.  
- **Infrastructure as Code**: Provisioned using Terraform on AWS (EKS, IAM, VPC, Security Groups).  
- **CI/CD Pipeline**: GitHub Actions for build, scan, sign, and deploy.  
- **Security Controls (Zero Trust)**:  
  - Vulnerability scanning with Trivy  
  - Image signing with Cosign  
  - Admission policy enforcement with Kyverno  
  - Runtime threat detection with Falco  
  - Secrets and encryption with AWS KMS, Secrets Manager, GitHub Secrets  
  - Least Privilege Access using AWS IAM  

---

## Prerequisites

- **AWS Account** with IAM permissions for EKS, ECR, S3, KMS  
- **Terraform v1.12+**  
- **Docker v27+**  
- **Kubectl & Helm**  
- **Java 17 + Maven**  
- **GitHub Actions** (configured with OIDC & GitHub Secrets)  

---

## Setup & Deployment

### 1. Provision Infrastructure
```bash
cd terraform
terraform init
terraform apply
```

### 2. Run Pipelines
- Manually trigger cluster-setup pipeline
- When any code changes happen, the app-deploy pipeline runs and deploys the code
- Configure the following secrets in GitHub Secrets:
AWS_REGION, AWS_SECRET_ACCESS_KEY, COSIGN_KEY, COSIGN_PASSWORD, ECR_REPOSITORY, EKS_CLUSTER_NAME, SLACK_WEBHOOK

---

## Monitoring & Logs

- Falco: Detects runtime anomalies, alerts via Slack
- Prometheus & Grafana: Collect and visualize system metrics
- CloudTrail: Audits IAM/KMS usage

---

## License

This project is developed as part of MSc Dissertation research.  
Feel free to reference.  
