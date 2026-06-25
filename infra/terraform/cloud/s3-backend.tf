# Bucket S3 para armazenar o arquivo de estado (tfstate) do Terraform
resource "aws_s3_bucket" "terraform_state" {
  bucket        = "fiap-repairshop"
  force_destroy = false

  tags = {
    Name        = "Terraform State Backend"
    Environment = "Prod"
  }
}

# Ativa o versionamento para manter o histórico de alterações do tfstate
resource "aws_s3_bucket_versioning" "terraform_state" {
  bucket = aws_s3_bucket.terraform_state.id
  versioning_configuration {
    status = "Enabled"
  }
}

# Habilita a criptografia em repouso por padrão (necessário para encrypt = true no backend)
resource "aws_s3_bucket_server_side_encryption_configuration" "terraform_state" {
  bucket = aws_s3_bucket.terraform_state.id

  rule {
    apply_server_side_encryption_by_default {
      sse_algorithm = "AES256"
    }
  }
}

# Bloqueia todo o acesso público ao bucket (segurança de dados confidenciais)
resource "aws_s3_bucket_public_access_block" "terraform_state" {
  bucket = aws_s3_bucket.terraform_state.id

  block_public_acls       = true
  block_public_policy     = true
  ignore_public_acls      = true
  restrict_public_buckets = true
}

# Controles de propriedade do S3 (desabilitando ACLs em favor de políticas de bucket)
resource "aws_s3_bucket_ownership_controls" "terraform_state" {
  bucket = aws_s3_bucket.terraform_state.id
  rule {
    object_ownership = "BucketOwnerEnforced"
  }
}

# Política do bucket S3 para garantir que as comunicações sejam feitas apenas via HTTPS/SSL (Regra S6249 do SonarQube)
resource "aws_s3_bucket_policy" "terraform_state" {
  bucket = aws_s3_bucket.terraform_state.id

  policy = jsonencode({
    Version = "2012-10-17"
    Statement = [
      {
        Sid       = "EnforceSSLOnly"
        Effect    = "Deny"
        Principal = "*"
        Action    = "s3:*"
        Resource = [
          aws_s3_bucket.terraform_state.arn,
          "${aws_s3_bucket.terraform_state.arn}/*"
        ]
        Condition = {
          Bool = {
            "aws:SecureTransport" = "false"
          }
        }
      }
    ]
  })
}

