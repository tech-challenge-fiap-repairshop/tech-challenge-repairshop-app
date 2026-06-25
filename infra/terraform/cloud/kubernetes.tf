# Criação do Namespace isolado para a aplicação
resource "kubernetes_namespace" "repairshop" {
  metadata {
    name = "repairshop"
  }

  # Aguarda a criação física do Node Group para garantir que o cluster está operacional
  depends_on = [aws_eks_node_group.nodes]
}

# Criação do ConfigMap com as configurações não-sensíveis do banco (URL e Nome do Banco)
resource "kubernetes_config_map" "db_config" {
  metadata {
    name      = "repairshop-db-config"
    namespace = kubernetes_namespace.repairshop.metadata[0].name
  }

  data = {
    SPRING_DATASOURCE_URL = "jdbc:postgresql://${aws_db_instance.postgres.endpoint}/${var.db_name}"
    POSTGRES_DB           = var.db_name
  }
}


# Serviço do MailHog do tipo LoadBalancer para exposição externa
resource "kubernetes_service" "mailhog" {
  metadata {
    name      = "mailhog"
    namespace = kubernetes_namespace.repairshop.metadata[0].name
  }

  spec {
    selector = {
      app = "mailhog"
    }

    port {
      port        = 1025
      target_port = 1025
      name        = "smtp"
    }

    port {
      port        = 8025
      target_port = 8025
      name        = "http"
    }

    type = "LoadBalancer"
  }
}
