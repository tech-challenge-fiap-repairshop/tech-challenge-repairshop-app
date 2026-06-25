# Grupo de Subnets para o RDS (usando as sub-redes privadas da VPC)
resource "aws_db_subnet_group" "rds_subnet_group" {
  name       = "${var.cluster_name}-rds-subnet-group"
  subnet_ids = aws_subnet.private[*].id

  tags = {
    Name = "${var.cluster_name}-rds-subnet-group"
  }
}

# Security Group para o RDS PostgreSQL
resource "aws_security_group" "rds_sg" {
  name        = "${var.cluster_name}-rds-sg"
  description = "Security Group para o RDS PostgreSQL"
  vpc_id      = aws_vpc.main.id

  # Permitir entrada de tráfego na porta 5432 vinda de qualquer recurso dentro da VPC
  ingress {
    description = "Acesso PostgreSQL a partir da VPC"
    from_port   = 5432
    to_port     = 5432
    protocol    = "tcp"
    cidr_blocks = [aws_vpc.main.cidr_block]
  }

  egress {
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
  }

  tags = {
    Name = "${var.cluster_name}-rds-sg"
  }
}

# Instância de Banco de Dados RDS PostgreSQL
resource "aws_db_instance" "postgres" {
  identifier             = "${var.cluster_name}-postgres"
  engine                 = "postgres"
  engine_version         = "16"
  instance_class         = "db.t3.micro" # Econômico, ideal para laboratórios/estudos
  allocated_storage      = 20
  storage_type           = "gp3"
  db_name                = var.db_name
  username               = var.db_username
  password               = var.db_password
  port                   = 5432
  db_subnet_group_name   = aws_db_subnet_group.rds_subnet_group.name
  vpc_security_group_ids = [aws_security_group.rds_sg.id]

  storage_encrypted   = true
  skip_final_snapshot     = true
  publicly_accessible     = false
  backup_retention_period = 7

  tags = {
    Name = "${var.cluster_name}-postgres"
  }
}
