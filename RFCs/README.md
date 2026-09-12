# 📜 Request for Comments (RFCs) - Oficina API (RepairShop)

Este diretório contém os documentos de **Request for Comments (RFCs)** do projeto **Oficina API (RepairShop)**.

As RFCs atuam como instrumentos formais de **proposta técnica, discussão assíncrona e alinhamento de decisões arquiteturais** dentro do time de engenharia. Elas antecedem a criação das **ADRs (Architecture Decision Records)**, servindo como o espaço onde problemas são expostos, propostas são detalhadas, alternativas são avaliadas criticamente e o consenso técnico é amadurecido antes da implementação.

---

## 🔄 Fluxo de Decisão: De RFC a ADR

```
 [ Nova Ideia / Problema ]
            |
            v
   +-----------------+
   |    Criar RFC    | ---> Status: Rascunho / Aberta para comentários
   +-----------------+
            |
            v  (Discussão com a equipe, análise de impactos e alternativas)
   +-----------------+
   | RFC Atualizada  | ---> Status: Revisada
   +-----------------+
            |
            +-------------------------------+
            |                               |
    (Consenso e Aprovação)         (Proposta Inviável)
            |                               |
            v                               v
   +---------------------+        +---------------------+
   | Encerrada – Aprovada |        | Encerrada – Rejeitada|
   +---------------------+        +---------------------+
            |
            v
   +---------------------+
   |  Formalização ADR   | ---> Registro imutável de decisão (ADR-XXX)
   +---------------------+
```

---

## 📑 Catálogo Geral de RFCs

| RFC | Título da Proposta | Status | Data | ADR Relacionada | Domínio / Área |
| :--- | :--- | :---: | :---: | :---: | :--- |
| **[RFC-001](RFC-001-adocao-kotlin-spring-boot-clean-architecture.md)** | Adoção de Kotlin e Spring Boot com Clean Architecture e DDD para o Core da Oficina | `Encerrada – Aprovada` | 15/08/2026 | [ADR-001](../ADRs/ADR-001-linguagem-e-framework-aplicacao-principal.md) | Aplicação / Core |
| **[RFC-002](RFC-002-banco-de-dados-relacional-postgresql-rds.md)** | Escolha do Banco de Dados Relacional PostgreSQL via AWS RDS e Versionamento com Flyway | `Encerrada – Aprovada` | 16/08/2026 | [ADR-002](../ADRs/ADR-002-banco-de-dados-relacional-postgresql.md) | Dados / Persistência |
| **[RFC-003](RFC-003-desmembramento-em-micro-repositorios-e-governanca-git.md)** | Desmembramento da Arquitetura em Múltiplos Repositórios Especializados e Governança Git | `Encerrada – Aprovada` | 18/08/2026 | [ADR-003](../ADRs/ADR-003-desmembramento-em-micro-repositorios-e-governanca-git.md) | Governança / Repositórios |
| **[RFC-004](RFC-004-infraestrutura-como-codigo-terraform-e-ambientes.md)** | Provisionamento de Infraestrutura como Código com Terraform e Segregação de Ambientes | `Encerrada – Aprovada` | 19/08/2026 | [ADR-004](../ADRs/ADR-004-infraestrutura-como-codigo-terraform-e-ambientes.md) | Infraestrutura / IaC |
| **[RFC-005](RFC-005-topologia-de-rede-vpc-e-security-groups-descentralizados.md)** | Topologia de Rede VPC Unificada e Descentralização do Ciclo de Vida de Security Groups | `Encerrada – Aprovada` | 20/08/2026 | [ADR-005](../ADRs/ADR-005-topologia-de-rede-vpc-e-security-groups-descentralizados.md) | Redes / Segurança |
| **[RFC-006](RFC-006-orquestracao-de-conteineres-com-kubernetes-aws-eks.md)** | Orquestração de Contêineres com AWS EKS e Escalonamento Automático via HPA | `Encerrada – Aprovada` | 21/08/2026 | [ADR-006](../ADRs/ADR-006-orquestracao-de-conteineres-com-kubernetes-aws-eks.md) | Computação / Orquestração |
| **[RFC-007](RFC-007-microsservico-serverless-de-autenticacao-aws-lambda-auth.md)** | Isolamento do Serviço de Autenticação em Microsserviço Serverless AWS Lambda e JWT Stateless | `Encerrada – Aprovada` | 22/08/2026 | [ADR-007](../ADRs/ADR-007-microsservico-serverless-de-autenticacao-aws-lambda-auth.md) | Segurança / Serverless |
| **[RFC-008](RFC-008-ponto-unico-de-entrada-com-aws-api-gateway.md)** | Ponto Único de Entrada com AWS API Gateway (HTTP API v2) e Roteamento Desacoplado | `Encerrada – Aprovada` | 23/08/2026 | [ADR-008](../ADRs/ADR-008-ponto-unico-de-entrada-com-aws-api-gateway.md) | Ingress / Gateway |
| **[RFC-009](RFC-009-pilha-de-observabilidade-unificada-opentelemetry.md)** | Pilha de Observabilidade Unificada com OpenTelemetry, Prometheus, Jaeger e Grafana Loki | `Encerrada – Aprovada` | 24/08/2026 | [ADR-009](../ADRs/ADR-009-pilha-de-observabilidade-unificada-opentelemetry.md) | Observabilidade / Telemetria |
| **[RFC-010](RFC-010-esteira-ci-cd-automacao-de-testes-e-quality-gate.md)** | Automação de CI/CD com GitHub Actions, Testes com Testcontainers, Trivy e Quality Gate SonarCloud | `Encerrada – Aprovada` | 25/08/2026 | [ADR-010](../ADRs/ADR-010-esteira-ci-cd-automacao-de-testes-e-quality-gate.md) | CI/CD / Qualidade |
| **[RFC-011](RFC-011-notificacoes-assincronas-e-ambiente-de-emulacao-email.md)** | Notificações de Status de Ordem de Serviço e Interceptação em Desenvolvimento com Mailpit | `Encerrada – Aprovada` | 26/08/2026 | [ADR-011](../ADRs/ADR-011-notificacoes-assincronas-e-ambiente-de-emulacao-email.md) | Notificações / Testes |
| **[RFC-012](RFC-012-procedimento-seguro-de-destruicao-de-infraestrutura.md)** | Procedimento Controlado de Destruição de Infraestrutura em Nuvem com Safety Gate | `Encerrada – Aprovada` | 27/08/2026 | [ADR-012](../ADRs/ADR-012-procedimento-seguro-de-destruicao-de-infraestrutura.md) | DevOps / Operação |
| **[RFC-013](RFC-013-substituicao-do-mailhog-pelo-mailpit-para-testes-de-email.md)** | Substituição do MailHog pelo Mailpit como Servidor SMTP de Testes e Interceptação de E-mails | `Encerrada – Aprovada` | 05/09/2026 | [ADR-013](../ADRs/ADR-013-substituicao-do-mailhog-pelo-mailpit-para-testes-de-email.md) | Emulação / SMTP |
| **[RFC-014](RFC-014-dashboard-executivo-grafana-metricas-gerenciais-e-negocio.md)** | Criação de Dashboard Executivo e Exposição de Métricas de Negócio com Micrometer e Grafana | `Encerrada – Aprovada` | 09/09/2026 | ADR-009 / Demanda 11 | Observabilidade / Negócios |

---

## 📐 Estrutura Padronizada de uma RFC

Toda nova RFC criada neste diretório deve seguir o template oficial:

```markdown
# TÍTULO
Deve ser curto, objetivo e descritivo. Exemplo: "RFC – Adoção de fila de eventos com Apache Kafka"

## DATA
Formato: dd/mm/yyyy
Refere-se à data de criação da proposta.

## STATUS
- Rascunho: Documento inicial, ainda sem revisão da equipe.
- Aberta para comentários: Em discussão ativa pelo time.
- Revisada: Atualizada com base nos feedbacks.
- Encerrada – Aprovada: Proposta aprovada e pode originar uma ADR.
- Encerrada – Rejeitada: Proposta descartada, com justificativa registrada.
- Cancelada: Interrompida por falta de relevância, mudanças de escopo ou substituição.

## RESUMO
Breve descrição da proposta. O objetivo principal deve ser claro em até 3 linhas.

## PROBLEMA
Explique o cenário atual, a limitação técnica, o desafio ou dor que motivou a proposta. Seja específico, mas evite aprofundar na solução nesta seção.

## PROPOSTA TÉCNICA
Apresente a ideia sugerida. Pode conter diagramas, pseudocódigo ou fluxos, desde que ajudem a ilustrar a proposta de forma clara e objetiva.

## IMPACTO ESPERADO
Liste os benefícios, riscos, impactos operacionais, custos ou restrições técnicas que podem surgir com a implementação da proposta.

## ALTERNATIVAS CONSIDERADAS
Mencione outras abordagens avaliadas e explique por que foram descartadas. Isso demonstra que a proposta foi pensada de forma crítica e comparativa.

## PONTOS EM ABERTO
Quais decisões ou aspectos ainda precisam de alinhamento? Liste tópicos que precisam de validação, estudo ou decisão coletiva.
```
