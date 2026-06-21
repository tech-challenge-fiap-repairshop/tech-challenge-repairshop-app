variable "aws_region" {
  description = "Região da AWS para provisionamento"
  type        = string
  default     = "us-east-1"
}

variable "cluster_name" {
  description = "Nome do Cluster EKS"
  type        = string
  default     = "repairshop-eks"
}

variable "vpc_cidr" {
  description = "Bloco CIDR para a VPC"
  type        = string
  default     = "10.0.0.0/16"
}

variable "db_name" {
  description = "Nome do banco de dados no RDS"
  type        = string
  default     = "repairshop"
}

variable "db_username" {
  description = "Usuário master do banco de dados RDS"
  type        = string
  default     = "postgres"
}

variable "db_password" {
  description = "Senha master do banco de dados RDS (passada de forma segura)"
  type        = string
  sensitive   = true
}

variable "db_instance_class" {
  description = "Classe da instância do RDS"
  type        = string
  default     = "db.t4g.micro"
}

variable "eks_node_instance_type" {
  description = "Tipo de instância EC2 para os worker nodes do EKS"
  type        = string
  default     = "t3.micro"
}

variable "lab_role_arn" {
  description = "ARN da LabRole pré-criada na AWS Academy"
  type        = string
  default     = "arn:aws:iam::154448561009:role/LabRole"
}
