# spring\_batch\_demo



This repository contains the implementation code for the MSc Dissertation on  

\*\*A Zero Trust Framework for Security Enhancement in Kubernetes Based Batch Processing System\*\* 



The project demonstrates how Zero Trust principles can be applied to Kubernetes-based batch processing workloads using Spring Batch, AWS EKS, Terraform, and GitHub CICD pipelines. It integrates multiple security tools to enforce least privilege, runtime monitoring, image signing, and secrets management.



---



\## Features



\- \*\*Spring Batch Application\*\*: Reads data from CSV, processes it, and persists into MySQL.  

\- \*\*Containerization\*\*: Packaged as Docker image for portability.  

\- \*\*Infrastructure as Code\*\*: Provisioned using Terraform on AWS (EKS, IAM, VPC, Security Groups).  

\- \*\*CI/CD Pipeline\*\*: GitHub Actions for build, scan, sign, and deploy.  

\- \*\*Security Controls (Zero Trust)\*\*:  

&nbsp; - Vulnerability scanning with Trivy  

&nbsp; - Image signing with Cosign  

&nbsp; - Admission policy enforcement with Kyverno  

&nbsp; - Runtime threat detection with Falco  

&nbsp; - Secrets and encryption with AWS KMS, Secrets Manager, GitHub Secrets

&nbsp; - Least Privilege Access using AWS IAM



---



---



\## Prerequisites



\- \*\*AWS Account\*\* with IAM permissions for EKS, ECR, S3, KMS  

\- \*\*Terraform v1.12+\*\*  

\- \*\*Docker v27+\*\*  

\- \*\*Kubectl \& Helm\*\*  

\- \*\*Java 17 + Maven\*\*  

\- \*\*GitHub Actions\*\* (configured with OIDC \& GitHub Secrets)



---



\## Setup \& Deployment



\### 1. Provision Infrastructure

```bash

cd terraform

terraform init

terraform apply

```



\### 2. Run Pipelines
Manually trigger cluster-setup pipeline
When any code changes happen, the app-deploy pipelines runs and deploys the code.

Configure below secrets in GitHub secrets: AWS\_REGION, AWS\_SECRET\_ACCESS\_KEY, COSIGN\_KEY, COSIGN\_PASSWORD, ECR\_REPOSITORY, EKS\_CLUSTER\_NAME, SLACK\_WEBHOOK


---



\## Monitoring \& Logs



Falco: Detects runtime anomalies, alerts via Slack

Prometheus \& Grafana: Collect and visualize system metrics

CloudTrail: Audits IAM/KMS usage



---



\## License



This project is developed as part of MSc Dissertation research.

Feel free to reference with attribution.

