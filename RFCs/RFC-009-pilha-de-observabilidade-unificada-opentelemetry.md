# RFC – Pilha de Observabilidade Unificada com OpenTelemetry, Prometheus, Jaeger e Grafana Loki

## DATA
24/08/2026

## STATUS
Encerrada – Aprovada

> **Nota de Encerramento:** Proposta amplamente validada pelo time técnico e aprovada, originando formalmente o [ADR-009](../ADRs/ADR-009-pilha-de-observabilidade-unificada-opentelemetry.md).

## RESUMO
Proposta de implementação da pilha de observabilidade unificada baseada no padrão aberto OpenTelemetry (OTel), utilizando instrumentação automática via Java Agent na aplicação principal, OTel Collector Gateway centralizado no Kubernetes, Prometheus para métricas, Jaeger para rastreamento distribuído e Grafana Loki para logs estruturados.

## PROBLEMA
Em uma arquitetura distribuída composta por API Gateway, Lambda Serverless, Kubernetes EKS e banco relacional RDS PostgreSQL, identificar a causa raiz de um erro ou lentidão torna-se extremamente difícil:
1. **Falta de Rastreamento de Ponta a Ponta (*Distributed Tracing*):** Uma requisição lenta pode sofrer gargalos na autenticação da Lambda, na latência do balanceador, no processamento da CPU no Pod ou em um lock de tabela no banco de dados. Sem um *Trace ID* propagado entre todos os nós, o diagnóstico depende de suposições manuais.
2. **Métricas Fragmentadas:** O time não consegue correlacionar em tempo real a taxa de requisições por segundo (RPS), códigos de erro HTTP (5xx) e o consumo de memória heap da JVM.
3. **Logs Dispersos e Efêmeros:** No Kubernetes, Pods são recriados a qualquer momento; logs retidos no contêiner são perdidos ao reiniciar.
4. **Risco de Acoplamento Proprietário (*Vendor Lock-in*):** Utilizar SDKs proprietários (Datadog, Dynatrace ou New Relic) embutidos no código-fonte Kotlin engessa a base de código e dificulta a troca futura de provedor de observabilidade.

## PROPOSTA TÉCNICA
Propõe-se a adoção do **OpenTelemetry (OTel)** como padrão universal de telemetria, desacoplando o código de aplicação da plataforma de visualização:

```
+-----------------------------------------------------------------------------------------+
|                                    CLUSTER KUBERNETES                                   |
|                                                                                         |
| [ Pods Aplicação Spring Boot ]                     [ AWS Lambda Auth ]                  |
|  - opentelemetry-javaagent.jar                      - ADOT Lambda Layer                 |
|  - Zero código manual de telemetria                 - Variáveis OTLP                    |
|             \                                             /                             |
|              \ (Protocolo OTLP - Portas 4317/4318)       /                              |
|               v                                         v                               |
|        +-------------------------------------------------------+                        |
|        |         OpenTelemetry Collector Gateway               |                        |
|        |         - Receivers: OTLP (gRPC / HTTP)               |                        |
|        |         - Processors: Batch, Memory Limiter           |                        |
|        |         - Exporters: Prometheus, Jaeger, Loki         |                        |
|        +-------------------------------------------------------+                        |
|                     /                     |                     \                       |
|                    /                      |                      \                      |
|                   v                       v                       v                     |
|           +---------------+       +---------------+       +---------------+             |
|           |   PROMETHEUS  |       |   JAEGER UI   |       |  GRAFANA LOKI |             |
|           |   (Métricas)  |       |   (Traces)    |       |    (Logs)     |             |
|           +---------------+       +---------------+       +---------------+             |
|                                           |                                             |
|                                           v                                             |
|                                 +-------------------+                                   |
|                                 | GRAFANA DASHBOARD |                                   |
|                                 +-------------------+                                   |
+-----------------------------------------------------------------------------------------+
```

### Detalhes Estruturais da Proposta:

1. **Instrumentação Zero-Code via Java Agent:**
   - No `Dockerfile`, baixa-se o `opentelemetry-javaagent.jar` e integra-se via flag `-javaagent:` na inicialização da JVM.
   - O agente instrumenta automaticamente requisições Spring Web MVC, consultas JDBC do Hibernate, chamadas HTTP e correlaciona *Trace ID* nos logs do Logback.
2. **OTel Collector Gateway (`k8s/observability/`):**
   - Centraliza o recebimento de dados OTLP nas portas `4317` (gRPC) e `4318` (HTTP), agrupa em lotes e despacha para os destinos finais.
3. **Três Pilares da Observabilidade Especializados:**
   - **Métricas:** Prometheus coletando métricas de infraestrutura e negócio (`/actuator/prometheus`).
   - **Traces:** Jaeger All-In-One recebendo spans OTLP para visualização de diagramas em cascata (*waterfall diagrams*).
   - **Logs:** Grafana Loki recebendo logs estruturados em formato JSON com campos indexados (`trace_id`, `span_id`, `level`, `app`).
4. **Deploy Declarativo com Kustomize (`configMapGenerator`):**
   - Configurações puras em YAML mantidas em `k8s/observability/configs/` (`otel-collector-config.yaml`, `prometheus.yml`, `loki.yml`) e transformadas em ConfigMaps de forma limpa pelo `kustomization.yaml`.

## IMPACTO ESPERADO
- **Benefícios:**
  - Visibilidade total e correlacionada dos três pilares da observabilidade em uma única arquitetura.
  - Zero linhas de código de domínio ou caso de uso poluídas com anotações de telemetria.
  - Total liberdade tecnológica: mudar o destino final de dados (ex: enviar para Grafana Cloud ou Datadog) exige apenas alterar linhas no arquivo de configuração do Collector.
  - Redução drástica no tempo médio de resolução de incidentes (*MTTR*).
- **Riscos e Mitigações:**
  - *Risco:* Consumo de recursos de memória pelos componentes de monitoramento no Kubernetes.
  - *Mitigação:* Configuração rigorosa de `resources.limits` e `requests` em todos os manifestos de observabilidade.
- **Impacto Operacional:**
  - Diagnóstico simplificado e padronizado em todas as camadas da solução.

## ALTERNATIVAS CONSIDERADAS
1. **Instrumentação Manual com Micrometer Tracing e Logstash:**
   - *Prós:* Menor tamanho inicial de binário.
   - *Contras:* Exige código e dependências manuais espalhadas por toda a aplicação e maior complexidade de configuração.
2. **Plataforma Proprietária Exclusiva (Datadog / Dynatrace / New Relic direto na aplicação):**
   - *Prós:* Painéis pré-configurados pelo fornecedor.
   - *Contras:* Custos elevados de licenciamento e acoplamento direto da base de código ao SDK do fornecedor.

## PONTOS EM ABERTO
- [x] Portas de comunicação do Collector (Alinhado: 4317 gRPC e 4318 HTTP).
- [x] Formato de logs (Alinhado: Formato estruturado com injeção automática de Trace ID pelo Logback).
