# Infraestrutura Local do Kubernetes com Terraform

Este documento registra o histórico de design e provisionamento da infraestrutura local em Kubernetes (Minikube / Docker Desktop / Kind) utilizando o **Terraform**. Esta infraestrutura faz parte da Fase 2 do Tech Challenge - FIAP.

---

## 📂 Estrutura de Arquivos

Toda a configuração do Terraform está localizada no diretório `infra/terraform/local/`. A seguir, os arquivos criados e suas respectivas responsabilidades:

| Arquivo | Função Principal |
| :--- | :--- |
| `main.tf` | Configura os requisitos mínimos do Terraform e o provedor Kubernetes local (`config_path` e `config_context`). |
| `variables.tf` | Declara todas as variáveis parametrizáveis do ambiente (credenciais de BD, tags de imagem, limites, etc.). |
| `namespace.tf` | Cria o namespace dedicado `repairshop` para isolamento de recursos do Kubernetes. |
| `configmap_secret.tf` | Cria o `ConfigMap` (`app-config`) para variáveis de ambiente comuns (URLs de conexão, SMTP) e o `Secret` (`db-secret`) para dados confidenciais. |
| `database.tf` | Cria a infraestrutura do PostgreSQL 17: `PersistentVolumeClaim` (1Gi), `Deployment` (com limites de recursos e probes de saúde) e `Service` ClusterIP (porta 5432). |
| `application.tf` | Provisiona a API Java/Kotlin Spring Boot: `Deployment` (2 réplicas, estratégia RollingUpdate, probes HTTP Actuator), `Service` NodePort (porta `30080`) e `HorizontalPodAutoscaler` (HPA v2). |
| `outputs.tf` | Exibe os outputs principais pós-deploy (Namespace, IP/Porta de acesso, status do HPA e comandos úteis). |
| `terraform.tfvars.example` | Arquivo de exemplo com valores padrão para facilitar o provisionamento local rápido. |

---

## 🛠️ Detalhes dos Componentes Criados

### 1. Provedor & Conectividade (`main.tf` & `variables.tf`)
- **Provedor:** `hashicorp/kubernetes` (versão mínima `~> 2.35`).
- **Conexão:** Conecta ao cluster local via arquivo de configuração padrão do Kubeconfig (`~/.kube/config`). O contexto padrão é configurado para `minikube`, mas pode ser alterado via variáveis para `docker-desktop` ou `kind-kind`.

### 2. Namespace de Isolamento (`namespace.tf`)
- Cria o namespace `repairshop`. Todos os recursos subsequentes são gerados dentro deste namespace, garantindo isolamento organizacional e de rede.

### 3. Gerenciamento de Configurações (`configmap_secret.tf`)
- **ConfigMap (`app-config`):** Contém chaves como `SPRING_DATASOURCE_URL` (apontando para o serviço interno do PostgreSQL), `SPRING_MAIL_HOST` e `SPRING_MAIL_PORT`.
- **Secret (`db-secret`):** Contém credenciais sensíveis codificadas em Base64 como `db_user` e `db_password`.

### 4. Banco de Dados PostgreSQL 17 (`database.tf`)
- **Armazenamento:** `PersistentVolumeClaim` (PVC) chamado `postgres-pvc` com capacidade de `1Gi` e permissão `ReadWriteOnce`.
- **Deployment:**
  - Imagem oficial: `postgres:17-alpine`.
  - Variáveis de ambiente vinculadas ao Secret (`db-secret`).
  - **Liveness Probe:** Comando executando `pg_isready` a cada 10 segundos.
  - **Readiness Probe:** Comando executando `pg_isready` a cada 5 segundos.
  - **Recursos Limites:** Limite de CPU `500m`, memória `512Mi`; Garantia de CPU `250m`, memória `256Mi`.
- **Serviço:** ClusterIP interno exposto na porta `5432`.

### 5. API Spring Boot (`application.tf`)
- **Deployment:**
  - Imagem customizada parametrizada via variável.
  - Réplicas padrão: `2`.
  - Estratégia de Atualização: `RollingUpdate` (máximo `25%` indisponível, máximo `25%` novos pods durante atualização).
  - Variáveis mapeadas a partir do `ConfigMap` e `Secret`.
  - **Liveness Probe:** Requisição HTTP GET no endpoint `/actuator/health/liveness` (porta 8080).
  - **Readiness Probe:** Requisição HTTP GET no endpoint `/actuator/health/readiness` (porta 8080).
  - **Recursos Limites:** Limite de CPU `1000m`, memória `1024Mi`; Garantia de CPU `500m`, memória `512Mi`.
- **Serviço NodePort:**
  - Expõe a aplicação externamente na porta física `30080` de cada nó do cluster (acessível em `http://localhost:30080` ou via IP do Minikube).
- **Autoscaler (HPA v2):**
  - Monitora os pods da aplicação.
  - Limite Mínimo: `2` réplicas.
  - Limite Máximo: `5` réplicas.
  - Gatilho: `70%` de média de CPU ou `80%` de média de Memória atingido nos pods.

---

## 🚀 Como Executar Localmente

Siga os passos abaixo para provisionar e validar a infraestrutura:

### Pré-requisitos
- Terraform instalado localmente (v1.7.0+).
- Cluster de Kubernetes local ativo (Minikube iniciado com `minikube start` ou similar).
- kubectl instalado e configurado no contexto correto.

### Passo a Passo

1. **Acessar o Diretório de Infraestrutura:**
   ```bash
   cd infra/terraform/local
   ```

2. **Preparar as Variáveis:**
   Crie seu arquivo `terraform.tfvars` a partir do modelo de exemplo:
   ```bash
   cp terraform.tfvars.example terraform.tfvars
   ```
   *(Ajuste o `kubeconfig_context` para seu cluster local, ex: `docker-desktop` ou `minikube`)*

3. **Inicializar o Terraform:**
   Instala o provedor Kubernetes necessário.
   ```bash
   terraform init
   ```

4. **Verificar o Plano de Execução:**
   Certifique-se de que os recursos a serem criados estão corretos.
   ```bash
   terraform plan
   ```

5. **Aplicar a Configuração:**
   Applique a infraestrutura no Kubernetes local:
   ```bash
   terraform apply -auto-approve
   ```

6. **Validar o Status:**
   Após a aplicação com sucesso, utilize o kubectl para inspecionar os recursos no namespace `repairshop`:
   ```bash
   kubectl get all -n repairshop
   ```

---

## 🔄 Integração com Pipelines de CI/CD

Esta infraestrutura do Terraform é sincronizada e ativada a partir da pipeline do GitHub Actions configurada em `.github/workflows/infra.yml`. Ela escuta modificações no diretório `infra/**` ou `k8s/**` nas branches principais (`main`, `develop`), garantindo práticas modernas de **GitOps** e Infraestrutura como Código (IaC).
