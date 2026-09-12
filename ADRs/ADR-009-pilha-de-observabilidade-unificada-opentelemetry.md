# ADR-009: Pilha de Observabilidade Unificada com OpenTelemetry, Prometheus, Jaeger e Grafana Loki

**Data:** 2026-09-01  
**Status:** Aceito  
**Autor:** Grupo CAO (POSTECH 15SOAT)  

---

## Contexto

Em uma arquitetura moderna distribuída em nuvem (composta por API Gateway, funções Serverless AWS Lambda, contêineres orquestrados no AWS EKS e banco relacional AWS RDS), o diagnóstico de falhas, gargalos de desempenho e anomalias operacionais torna-se um grande desafio sem ferramentas adequadas.

Cenários críticos a serem endereçados:
1. **Rastreamento Distribuído (*Distributed Tracing*):** Uma requisição lenta pode ter sua latência consumida na validação de token na Lambda, na fila do Load Balancer, no processamento da regra de negócio no Pod ou em uma consulta SQL bloqueada no PostgreSQL. É vital ter um identificador único (*Trace ID*) que correlacione toda essa jornada.
2. **Coleta de Métricas em Tempo Real:** Monitorar o consumo de CPU, memória heap da JVM, taxas de requisições por segundo (RPS) e códigos de status HTTP (2xx, 4xx, 5xx) para acionar alertas e embasar o escalonamento automático.
3. **Agregação Centralizada de Logs:** Pods efêmeros no Kubernetes são destruídos ou reiniciados a qualquer momento; seus logs não podem ficar retidos localmente no disco do contêiner.
4. **Evitar Acoplamento a Fornecedores Proprietários (*No Vendor Lock-in*):** A instrumentação de código não deve depender de SDKs proprietários (como Datadog ou Dynatrace) que engessem o código-fonte.

---

## Decisão

Adotar o padrão aberto **OpenTelemetry (OTel)** da CNCF como a espinha dorsal de telemetria da aplicação, integrando **Instrumentação Automática via Java Agent**, um **OpenTelemetry Collector Gateway** centralizado no Kubernetes, e os seguintes destinos especializados para os três pilares da observabilidade:
- **Métricas:** **Prometheus** (com suporte ao endpoint Actuator `/actuator/prometheus`).
- **Traces Distribuídos:** **Jaeger UI** (recebendo dados via protocolo OTLP).
- **Logs Agregados:** **Grafana Loki** (coleta de logs estruturados via OTLP sem necessidade de agentes de host como Promtail/FluentBit).

### Detalhes Técnicos e Arquiteturais:

1. **Instrumentação Automática Sem Acoplamento de Código:**
   - No `Dockerfile` da aplicação principal (`tech-challenge-repairshop-app`), baixamos o `opentelemetry-javaagent.jar` oficial durante o build e o acoplamos na inicialização da JVM via argumento `-javaagent:opentelemetry-javaagent.jar`.
   - O agente intercepta chamadas HTTP do Spring Web MVC, transações JDBC do Hibernate/PostgreSQL e logs do Logback de forma transparente, injetando *Trace IDs* e *Span IDs* automaticamente em todas as operações sem exigir uma única linha de código manual em Kotlin.

2. **OpenTelemetry Collector Gateway (`k8s/observability/`):**
   - Provisionado como um Deployment e Service dedicado no namespace `repairshop` escutando nas portas `4317` (gRPC) e `4318` (HTTP).
   - Atua como um buffer inteligente e roteador de telemetria: recebe dados OTLP da aplicação e da Lambda, processa em lotes (*batch processor*) e despacha para os exportadores configurados.

3. **Arquitetura Declarativa com Kustomize (`configMapGenerator`):**
   - Os arquivos de configuração das ferramentas (`otel-collector-config.yaml`, `prometheus.yml`, `loki.yml`) são mantidos em sua sintaxe YAML pura dentro da pasta `k8s/observability/configs/`.
   - O `kustomization.yaml` converte esses arquivos em ConfigMaps do Kubernetes automaticamente no deploy (`kubectl apply -k k8s/observability/`), prevenindo erros de sintaxe de escape no YAML do Kubernetes e facilitando testes locais.

---

## Consequências

### Positivas
- **Observabilidade Completa dos Três Pilares:** Visibilidade total de Métricas, Traces e Logs em um único ecossistema integrado.
- **Zero Acoplamento no Código de Negócio:** O código de domínio e aplicação permanece 100% livre de dependências de monitoramento.
- **Padrão Aberto e Flexibilidade Futura:** Como todo o tráfego utiliza o padrão OTLP, exportar esses mesmos dados para plataformas como Grafana Cloud, Datadog, New Relic ou AWS CloudWatch exige apenas a adição de um exportador no `otel-collector-config.yaml`, sem reescrever ou recompilar a aplicação.
- **Rastreabilidade de Ponta a Ponta:** Capacidade de isolar a causa raiz de lentidão em consultas de banco ou chamadas entre serviços em poucos cliques.

### Negativas / Trade-offs
- **Consumo Adicional de Recursos no Cluster:** A execução dos Pods do OTel Collector, Prometheus, Jaeger e Loki consome parcelas de CPU e memória dos nós do EKS.
- **Overhead Mínimo na JVM:** A instrumentação por bytecode do Java Agent introduz um acréscimo marginal no tempo de inicialização da JVM e no consumo de memória heap.

### Riscos e Mitigações
- **Risco:** Perda de telemetria em caso de indisponibilidade temporária do OTel Collector.
- **Mitigação:** Configuração de filas de retenção assíncronas em memória no Java Agent e dimensionamento com limites de recursos definidos no Kubernetes.
