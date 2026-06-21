# Checklist - Tech Challenge (Fase 2)

[PDF de referência](docs/foundation/14SOAT%20-%20Fase%202%20-%20Tech%20challenge.pdf)

## Sumário

- [Objetivos da Fase](#-objetivos-da-fase)
- [Evolução da Aplicação (Refatoração e Código)](#-evolução-da-aplicação-refatoração-e-código)
- [Infraestrutura e DevOps](#️-infraestrutura-e-devops)
- [Entregáveis Finais](#-entregáveis-finais)

---

## 🎯 Objetivos da Fase

> Objetivos de alto nível — são atingidos quando os itens abaixo estão completos.

- [ ] Evoluir a aplicação desenvolvida na Fase 1 para garantir qualidade, resiliência e escalabilidade
- [ ] Incorporar práticas modernas de infraestrutura e automação
- [ ] Reduzir riscos operacionais por meio de infraestrutura escalável
- [ ] Automatizar o provisionamento e o deploy do ambiente
- [x] Melhorar a qualidade e a organização do código
- [ ] Preparar a aplicação para suportar grandes volumes de ordens de serviço com escalabilidade dinâmica

---

## 💻 Evolução da Aplicação (Refatoração e Código)

- [x] Refatorar o código aplicando princípios de Clean Code (nomes claros, simplicidade, coesão)
  > ✅ 38 itens de backlog resolvidos (BKL-001 a BKL-038), incluindo renomeações, remoção de anti-patterns, encapsulamento via aggregates
- [x] Implementar Clean Architecture ou Arquitetura Hexagonal
  > ✅ Estrutura `application/gateways` + `usecases/impl` + `domain/entities` + `infra/controller` + `infra/persistence` em todos os bounded contexts. Documentado em `clean_arq_plane.md`
- [x] Criar testes automatizados (unitários e/ou integração) para cobrir os fluxos críticos
  > ✅ 29 arquivos de teste: unitários (domínio + service + controller), integração com Testcontainers (PostgreSQL), JaCoCo com mínimo de 90%

### APIs a Alterar/Criar

- [x] **Abertura de OS:** Receber dados do cliente, veículo, serviços e peças; retornar a identificação única da OS
  > ✅ `POST /service-orders` — `CreateServiceOrderImpl.kt` recebe email do cliente, placa do veículo, lista de serviços com insumos
- [x] **Consulta de status:** Informar a situação atual da OS (Recebida, Diagnóstico, Aguardando Aprovação, Execução, Finalizada, Entregue)
  > ✅ `GET /service-orders/{id}` — `FindServiceOrderImpl.kt` + `ServiceOrderStatus.kt` com 9 estados completos
- [x] **Aprovação de orçamento:** Endpoint para receber notificações externas de aprovação/recusa do orçamento
  > ✅ `POST /service-orders/{id}/approve` — `ApproveServiceOrderImpl.kt` com `ApprovalDomainService`, dedução de estoque na aprovação
- [ ] **Listagem de OS (Ordenação):** Ordenar por status (Em Execução > Aguardando Aprovação > Diagnóstico > Recebida)
  > ❌ `findAll(pageable)` retorna sem ordenação por prioridade de status. Requer query customizada com `CASE WHEN`
- [ ] **Listagem de OS (Tempo):** Ordenar exibindo as mais antigas primeiro
  > ❌ Sem ordenação padrão por `enterTime ASC`. O Spring aceita `?sort=enterTime,asc` via Pageable, mas não está como default
- [ ] **Listagem de OS (Filtro):** Excluir (lógica não física) as OS finalizadas e entregues da listagem
  > ❌ `findAll(pageable)` retorna TODAS as OS incluindo FINALIZED, PAID e CANCELED. Requer filtro `WHERE status NOT IN (...)`
- [x] **Notificação:** Atualização de status da OS via e-mail ou ferramenta similar
  > ✅ `EmailService` + `JavaMailEmailService` + MailHog no docker-compose. Disparo de e-mail em `AdvanceServiceOrderStatusImpl` na transição para `WAITING_APPROVAL`

---

## 🏗️ Infraestrutura e DevOps

### Conteinerização

- [x] Atualizar o `Dockerfile`
  > ✅ Multi-stage build (Maven builder → JRE runtime), usuário não-root (`appuser`), imagem `eclipse-temurin:24-jre`
- [x] Atualizar/Criar o `docker-compose` para desenvolvimento local
  > ✅ 4 serviços: PostgreSQL 17 (healthcheck), App Spring Boot, MailHog (SMTP + Dashboard), SonarQube

### Orquestração (Kubernetes - K8s)

- [x] Criar manifestos YAML para Deployments
  > ✅ Criados manifestos `02-database.yaml` e `03-application.yaml` na pasta `/k8s`
- [x] Criar manifestos YAML para Services
  > ✅ Criados em conjunto com as aplicações e banco de dados
- [x] Criar manifestos YAML para ConfigMaps e Secrets (para variáveis sensíveis)
  > ✅ Arquivo `01-config.yaml` provisionado
- [x] Configurar Horizontal Pod Autoscaler (HPA) baseado em CPU/memória
  > ✅ HPA configurado para escalar a aplicação de 2 até 5 réplicas (CPU 70%, Memória 80%)

### Infraestrutura como Código (IaC - Terraform)

- [x] Criar scripts Terraform para provisionar o cluster Kubernetes
  > ✅ Scripts em `infra/terraform/local/` usando provider local (Minikube/Docker Desktop)
- [x] Criar scripts Terraform para provisionar o Banco de Dados
  > ✅ `database.tf` criado (PostgreSQL 17 com PersistentVolumeClaim)
- [x] Documentar quais recursos estão sendo criados e como aplicar os scripts
  > ✅ Detalhado no arquivo `creation_infrastructure.md`

### CI/CD

- [x] Configurar pipeline de CI/CD (ex: GitHub Actions)
  > ✅ `.github/workflows/ci.yml` com 3 stages: Build → Test → Quality Gate (SonarCloud)
- [x] Pipeline deve executar o Build da aplicação
  > ✅ Stage "Build" — `mvn clean compile -B -ntp -DskipTests`
- [x] Pipeline deve executar os testes automatizados
  > ✅ Stage "Test" — `mvn clean verify -B -ntp` com Testcontainers + upload de artefatos JaCoCo
- [x] Pipeline deve fazer o Build da imagem Docker
  > ✅ Job `docker-build-push` adicionado no `.github/workflows/ci.yml`
- [ ] Pipeline deve realizar o Deploy no cluster K8s
  > ❌ Pendente
- [ ] Pipeline deve realizar o Deploy do banco de dados
  > ❌ Pendente
- [ ] Pipeline deve aplicar os manifestos YAML no cluster
  > ❌ Pendente

---

## 📦 Entregáveis Finais

### Repositório Git

- [x] Código-fonte atualizado e refatorado
  > ✅ Clean Architecture aplicada em todos os bounded contexts
- [x] Arquivos `Dockerfile` e `docker-compose` na raiz ou local apropriado
  > ✅ Ambos na raiz do projeto
- [x] Pasta `/k8s` contendo os manifestos do Kubernetes
  > ✅ Manifestos gerados e disponíveis
- [x] Pasta `/infra` contendo os scripts Terraform
  > ✅ Scripts organizados em `infra/terraform/local/`
- [x] Arquivos de configuração da pipeline CI/CD
  > ✅ `.github/workflows/ci.yml`

### README.md

- [ ] Descrição da solução e dos objetivos da fase
  > ⚠️ README.md referencia "Fase 1" (`POSTECH 15SOAT — Tech Challenge Fase 1`). Precisa ser atualizado para Fase 2
- [x] Desenho da arquitetura (Componentes, Infraestrutura, Fluxo de deploy)
  > ✅ Diagramas em `docs/delivery/` (ERD, status chain, drawio)
- [x] Instruções para execução local
  > ✅ Seção "Como Executar" com docker-compose e Maven
- [ ] Instruções para deploy no Kubernetes
  > ❌ Não existe (depende dos manifestos K8s)
- [ ] Instruções para provisionamento com Terraform
  > ❌ Não existe (depende dos scripts Terraform)
- [x] Link para a collection das APIs (Postman, Swagger, etc.)
  > ✅ Referencia `docs/postman/` e Swagger UI (`http://localhost:8080/swagger-ui/index.html`)
- [ ] Link para o vídeo demonstrativo
  > ❌ Não existe no README

### Vídeo Demonstrativo

- [ ] Duração máxima de 15 minutos (YouTube ou Vimeo)
- [ ] Demonstrar o Deploy da aplicação
- [ ] Demonstrar a execução do CI/CD
- [ ] Demonstrar o consumo das APIs
- [ ] Demonstrar a escalabilidade automática (simulando carga)

### Portal do Aluno

- [ ] Submeter um arquivo PDF no portal
- [ ] O PDF deve conter o link do repositório GitHub
- [ ] O repositório deve estar compartilhado com o usuário `soat-architecture`
- [ ] O PDF deve conter o desenho da arquitetura com os recursos escolhidos
- [ ] O PDF deve conter o link do vídeo demonstrativo