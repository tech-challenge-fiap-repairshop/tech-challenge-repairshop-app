output "vpc_id" {
  description = "ID da VPC criada"
  value       = aws_vpc.main.id
}

output "eks_cluster_name" {
  description = "Nome do Cluster EKS"
  value       = aws_eks_cluster.eks.name
}

output "eks_cluster_endpoint" {
  description = "Endpoint do Cluster EKS"
  value       = aws_eks_cluster.eks.endpoint
}

output "eks_cluster_certificate_authority" {
  description = "Autoridade de certificação do Cluster EKS"
  value       = aws_eks_cluster.eks.certificate_authority[0].data
}

output "rds_endpoint" {
  description = "Endpoint de conexao para o banco de dados RDS"
  value       = aws_db_instance.postgres.endpoint
}

output "mailhog_endpoint" {
  description = "Endpoint publico para acessar a interface web do MailHog"
  value       = kubernetes_service.mailhog.status[0].load_balancer[0].ingress[0].hostname
}

