# ADR-006: Orquestração de Contêineres com AWS EKS e Escalonamento Automático via HPA

**Data:** 2026-09-01  
**Status:** Aceito  
**Autor:** Grupo CAO (POSTECH 15SOAT)  

---

## Contexto

A aplicação principal da oficina mecânica (*RepairShop API*) apresenta variações previsíveis e picos repentinos de demanda durante o horário comercial (abertura de ordens no início da manhã, aprovações de orçamentos e fechamento de ordens/emissão de faturas no final da tarde). 

Hospedar a aplicação em servidores virtuais tradicionais (como instâncias EC2 puras sem orquestrador) traria os seguintes problemas operacionais:
1. **Falta de Auto-Recuperação (*Self-Healing*):** Falhas em processos na JVM ou esgotamento de recursos exigiriam intervenção manual para reiniciar instâncias.
2. **Desperdício de Recursos ou Indisponibilidade:** Provisionar servidores fixos para o pico resulta em capacidade ociosa e custos elevados na maior parte do tempo; dimensionar para a média causaria lentidão e quedas durante os picos de atendimento.
3. **Indisponibilidade em Deploys:** Atualizações de versão exigiriam paradas do sistema (*downtime*) ou configurações manuais complexas de balanceamento de carga.

---

## Decisão

Adotar o **AWS EKS (Elastic Kubernetes Service)** para orquestração de contêineres e implementar o **Horizontal Pod Autoscaler (HPA)** com base no consumo de CPU, associado a manifestos declarativos e verificações de saúde (*Probes*) integradas ao Spring Boot Actuator.

### Detalhes Técnicos e Arquiteturais:

1. **Provisionamento do Cluster EKS (`tech-challenge-repairshop-infra-eks`):**
   - Cluster gerenciado provisionado via Terraform em versão estável do Kubernetes.
   - **Node Groups Gerenciados:** Alocados exclusivamente dentro das sub-redes privadas da VPC em múltiplas Zonas de Disponibilidade (AZs).
   - **Metrics Server:** Instalado no cluster para fornecer a API de métricas (`metrics.k8s.io`) necessária para o funcionamento do HPA.

2. **Manifestos Declarativos e Ciclo de Vida (`k8s/`):**
   - **`Deployment` (`deployment.yaml`):** Define a execução dos Pods baseados na imagem Docker do ECR, com limites declarativos de recursos (*requests* e *limits* de CPU e Memória) e estratégia de *RollingUpdate* (garantindo que novas réplicas estejam saudáveis antes de desativar as antigas, permitindo zero downtime).
   - **Verificações de Saúde (Health Checks / Probes):**
     - `startupProbe`: Aguarda o tempo de inicialização da JVM Kotlin/Spring Boot (até 60s) antes de iniciar as demais verificações.
     - `readinessProbe`: Consulta `/actuator/health/readiness` na porta 8080 para determinar se o Pod está apto a receber tráfego do Load Balancer.
     - `livenessProbe`: Consulta `/actuator/health/liveness` para reiniciar automaticamente Pods que entrarem em estado travado (*deadlock* ou pane de memória).
   - **`Service` (`service.yaml`):** Expõe a aplicação internamente e cria um Network Load Balancer (NLB/CLB) na AWS para balanceamento de carga do tráfego recebido.

3. **Escalonamento Automático (HPA - `hpa.yaml`):**
   - Configurado para monitorar a média de utilização de CPU dos Pods.
   - **Gatilho de Escalonamento:** Quando a utilização média de CPU ultrapassa o limiar parametrizado (ex: 70%), o HPA escala o número de réplicas de forma elástica entre o mínimo (2 réplicas para garantir alta disponibilidade entre AZs) e o limite máximo configurado para o ambiente.

---

## Consequências

### Positivas
- **Alta Disponibilidade e Resiliência (HA):** Distribuição de réplicas entre nós e AZs distintas e recuperação automática imediata de contêineres com falha via *liveness probes*.
- **Elasticidade Automática Sob Demanda:** A aplicação escala réplicas em segundos durante picos de abertura de ordens de serviço e desescala quando o tráfego normaliza, otimizando custos de computação.
- **Deploys com Zero Downtime:** A estratégia de *RollingUpdate* combinada com *readiness probes* garante que nenhuma requisição seja perdida durante atualizações de versão da API.
- **Padronização Cloud-Native:** Uso de manifestos declarativos versionados no repositório Git, facilitando deploys contínuos via GitHub Actions.

### Negativas / Trade-offs
- **Custo do Plano de Controle EKS:** O plano de controle gerenciado do AWS EKS possui um custo fixo mensal (cerca de $73/mês por cluster), justificável para ambientes de produção corporativos.
- **Complexidade Operacional do Kubernetes:** Exige domínio de conceitos de orquestração (Pods, Deployments, Services, ConfigMaps, Secrets, RBAC, Probes).

### Riscos e Mitigações
- **Risco:** Falha de escala caso o Node Group do EKS não tenha capacidade de nós suficiente para acomodar novos Pods criados pelo HPA.
- **Mitigação:** Dimensionamento adequado dos limites máximos de instâncias no Node Group do Terraform e limites conservadores de *requests* de CPU nos Pods.
