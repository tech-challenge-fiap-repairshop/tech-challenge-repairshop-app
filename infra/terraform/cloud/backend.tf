terraform {
  backend "s3" {
    bucket  = "fiap-repairshop"
    key     = "terraform-config/tfstate/terraform.tfstate"
    region  = "us-east-1"
    encrypt = true
  }
}
