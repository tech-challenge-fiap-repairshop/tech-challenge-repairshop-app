# Criação do Namespace isolado para a aplicação
resource "kubernetes_namespace" "repairshop" {
  metadata {
    name = "repairshop"
  }

  # Aguarda a criação física do Node Group para garantir que o cluster está operacional
  depends_on = [aws_eks_node_group.nodes]
}

# Criação do Secret com as credenciais do banco para injeção na aplicação e no container do Postgres
resource "kubernetes_secret" "db_credentials" {
  metadata {
    name      = "repairshop-db-credentials"
    namespace = kubernetes_namespace.repairshop.metadata[0].name
  }

  data = {
    SPRING_DATASOURCE_URL      = "jdbc:postgresql://postgres-service:5432/${var.db_name}"
    SPRING_DATASOURCE_USERNAME = var.db_username
    SPRING_DATASOURCE_PASSWORD = var.db_password
    POSTGRES_USER              = var.db_username
    POSTGRES_PASSWORD          = var.db_password
    POSTGRES_DB                = var.db_name
  }

  type = "Opaque"
}




