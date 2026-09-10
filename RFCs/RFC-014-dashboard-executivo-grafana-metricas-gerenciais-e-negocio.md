# RFC – Criação de Dashboard Executivo e Exposição de Métricas de Negócio com Micrometer e Grafana

## DATA
09/09/2026

## STATUS
Encerrada – Aprovada

> **Nota de Encerramento:** Proposta aprovada e implementada na Demanda 11, consolidando a camada de observabilidade gerencial e de negócios do projeto.

## RESUMO
Proposta de implementação de um exportador de métricas de negócio e KPIs em tempo real na aplicação Spring Boot utilizando Micrometer `MeterBinder` integrado ao endpoint `/actuator/prometheus`, e criação de um Dashboard Executivo e Gerencial moderno no Grafana para visualização do funil de atendimento, tempos médios entre etapas e indicadores financeiros da oficina mecânica.

## PROBLEMA
A camada de observabilidade existente até a Fase 2 e início da Fase 3 cobria estritamente métricas de infraestrutura de TI (consumo de CPU, memória heap da JVM, latência de rede e códigos de status HTTP).

Para a tomada de decisão estratégica e gestão executiva da oficina mecânica (*RepairShop*), essa visão puramente técnica era insuficiente:
1. **Falta de Visibilidade do Funil Operacional:** A gerência não conseguia visualizar em tempo real quantos veículos estavam em diagnóstico, quantos orçamentos aguardavam aprovação de clientes ou quantas ordens estavam em execução nos elevadores da oficina.
2. **Ausência de Indicadores Financeiros e de Estoque:** Inexistência de painéis consolidados informando o faturamento total acumulado (em R$), ticket médio por ordem de serviço e patrimônio imobilizado em peças de estoque.
3. **Falta de Medição Precisa de SLAs Operacionais (Cycle Time):** Dificuldade de mensurar quanto tempo em média a oficina demora para elaborar um diagnóstico, quanto tempo o cliente leva para responder ao orçamento e o tempo médio de reparo e pagamento.

## PROPOSTA TÉCNICA
Propõe-se a criação do componente **`BusinessMetricsBinder.kt`** na aplicação Spring Boot para registro automático de métricas analíticas no Micrometer, coletadas periodicamente pelo Prometheus e apresentadas no dashboard **"RepairShop - Executive & Business Management Dashboard"** no Grafana:

```
+-----------------------------------------------------------------------------------------+
|                                  APLICAÇÃO SPRING BOOT                                  |
|                                                                                         |
| [ Repositórios de Dados ]            [ BusinessMetricsBinder (MeterBinder) ]            |
| - ServiceOrderRepository             - Registra Gauges dinâmicos no MeterRegistry       |
| - CustomerRepository / VehicleRepo   - Consulta contagens e médias em tempo de scrape   |
| - InvoiceRepository / InsumeRepo     - Tratamento defensivo com runCatching             |
+-----------------------------------------------------------------------------------------+
                                           |
                                           | Endpoint: /actuator/prometheus
                                           v
+-----------------------------------------------------------------------------------------+
|                                    PROMETHEUS SCRAPE                                    |
| - repairshop_business_customers, vehicles, service_orders, revenue_total_brl            |
| - repairshop_business_lead_time_minutes, quote_approval_rate_percent, inventory_items...|
+-----------------------------------------------------------------------------------------+
                                           |
                                           v
+-----------------------------------------------------------------------------------------+
|                                    GRAFANA DASHBOARD                                    |
| [ Painel 1: Indicadores Executivos ] -> Clientes, Veículos, Faturamento Total, Ticket   |
| [ Painel 2: Funil Operacional ]      -> Rosca percentual e Barras horizontais por etapa|
| [ Painel 3: Cycle Time & SLAs ]      -> Média de minutos em cada transição de status    |
| [ Painel 4: Conversão & Estoque ]    -> Gauges de aprovação, conclusão e estoque        |
| [ Painel 5: Links de Navegação ]     -> Atalhos cruzados entre Dashboards Técnico e Biz |
+-----------------------------------------------------------------------------------------+
```

### Componentes Técnicos da Proposta:

1. **`BusinessMetricsBinder.kt` (Core Metrics):**
   - Implementa a interface `MeterBinder` do Micrometer, desacoplando totalmente a telemetria dos casos de uso de negócio.
   - Executa consultas SQL otimizadas com agregações (`COUNT`, `SUM`, `AVG`, `EXTRACT(EPOCH FROM ...)` sobre `tb_service_order_history`).
   - Encapsulamento com `runCatching` para garantir retorno seguro (`0.0`) em caso de banco vazio ou em manutenção.
2. **Estrutura Visual do Dashboard Grafana (`repairshop-business-metrics.json`):**
   - **Executive KPIs:** Clientes cadastrados, veículos, total de OSs, faturamento em R$ (formatação monetária BRL) e ticket médio.
   - **Funil de Atendimento:** Gráfico Donut de distribuição percentual por status e gráfico de barras horizontal de pipeline operacional.
   - **Cycle Time & SLAs:** Gráfico comparativo de tempo médio em minutos por etapa (`RECEIVED` ➡️ `IN_DIAGNOSIS` ➡️ `WAITING_APPROVAL` ➡️ `APPROVED` ➡️ `IN_EXECUTION` ➡️ `FINALIZED` ➡️ `PAID`).
   - **Eficiência Comercial e Estoque:** Gauges circulares de taxa de conversão de orçamentos e cartões de patrimônio em estoque.
3. **Navegação Integrada (*Cross-Dashboard Navigation*):**
   - Links no cabeçalho superior conectando o Dashboard Técnico Geral, o Dashboard da Lambda Auth e o Dashboard Executivo.

## IMPACTO ESPERADO
- **Benefícios:**
  - Visão estratégica em tempo real do negócio para diretores e gerentes de oficina.
  - Identificação imediata de gargalos operacionais (ex: clientes demorando para aprovar orçamentos ou peças em falta no estoque).
  - Cálculo 100% matemático e confiável baseado no histórico auditável da base de dados.
  - Navegação intuitiva entre camadas técnicas e de negócios.
- **Riscos e Mitigações:**
  - *Risco:* Sobrecarga no banco de dados devido a consultas analíticas a cada ciclo de scrape do Prometheus.
  - *Mitigação:* Queries indexadas e simplificadas que executam em menos de 5 milissegundos.
- **Impacto Operacional:**
  - Decisões de negócio orientadas a dados (*Data-Driven Decision Making*).

## ALTERNATIVAS CONSIDERADAS
1. **Ferramenta Externa de BI (PowerBI / Metabase conectada diretamente ao RDS):**
   - *Prós:* Recursos avançados de relatórios estáticos para download em PDF.
   - *Contras:* Custo adicional de licenciamento, necessidade de abrir portas no banco de dados e falta de integração em tempo real na mesma tela do Grafana.
2. **Consultas Manuais via Scripts SQL pela Equipe de Suporte:**
   - *Prós:* Zero desenvolvimento inicial.
   - *Contras:* Processo manual, lento, passível de erros e inacessível para a diretoria.

## PONTOS EM ABERTO
- [x] Intervalo de atualização recomendado (Alinhado: Auto-refresh a cada 5 segundos no Grafana).
- [x] Formatação monetária (Alinhado: Custom unit em Reais `R$`).
