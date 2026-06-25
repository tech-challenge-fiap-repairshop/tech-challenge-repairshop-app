# Repositório ECR privado no AWS Academy para armazenar a imagem do repairshop
resource "aws_ecr_repository" "repairshop" {
  name                 = "repairshop"
  image_tag_mutability = "MUTABLE"
  force_delete         = true

  image_scanning_configuration {
    scan_on_push = true
  }

  tags = {
    Name = "repairshop"
  }
}
