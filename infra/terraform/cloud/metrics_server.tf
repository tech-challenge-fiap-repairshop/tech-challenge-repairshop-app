# Instalação automática do Metrics Server no EKS via Helm
resource "helm_release" "metrics_server" {
  name             = "metrics-server"
  repository       = "https://kubernetes-sigs.github.io/metrics-server/"
  chart            = "metrics-server"
  version          = "3.12.1" # Versão estável do Chart do Metrics Server
  namespace        = "kube-system"
  create_namespace = false

  # Garante que o Metrics Server seja instalado apenas após os nós estarem prontos
  depends_on = [aws_eks_node_group.nodes]
}
