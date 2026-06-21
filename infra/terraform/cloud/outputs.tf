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
  description = "Endpoint da Instância RDS PostgreSQL"
  value       = aws_db_instance.postgres.endpoint
}

output "rds_database_name" {
  description = "Nome do banco de dados RDS"
  value       = aws_db_instance.postgres.db_name
}
