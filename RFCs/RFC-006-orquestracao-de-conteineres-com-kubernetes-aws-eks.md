# RFC – Orquestração de Contêineres com AWS EKS e Escalonamento Automático via HPA

## DATA
21/08/2026

## STATUS
Encerrada – Aprovada

> **Nota de Encerramento:** Proposta aprovada e implementada com sucesso, dando origem formal ao [ADR-006](../ADRs/ADR-006-orquestracao-de-conteineres-com-kubernetes-aws-eks.md).

## RESUMO
Proposta de utilização do Amazon Elastic Kubernetes Service (AWS EKS) como plataforma de orquestração de contêineres para a aplicação principal, integrando autoscaling horizontal via HPA (Horizontal Pod Autoscaler), probes de saúde com o Spring Boot Actuator e estratégias de deploy sem tempo de inatividade (*Zero Downtime*).

## PROBLEMA
A aplicação principal da oficina mecânica (*RepairShop API*) apresenta variações acentuadas de carga:
- Picos de requisições pela manhã (recepção de veículos, triagem e abertura de OS) e no final da tarde (conclusão de reparos, orçamentos aprovados e faturamento).
- Flutuações de tráfego de visualização e consultas durante o expediente.

Problemas ao executar a aplicação em instâncias convencionais sem orquestrador:
1. **Falta de Auto-Recuperação (*Self-Healing*):** Se o processo da JVM travar em uma máquina virtual (ex: esgotamento de memória ou erro não tratado), a aplicação fica fora do ar até intervenção manual humana.
2. **Dimensionamento Rígido:** Dimensionar servidores para a carga média causa lentidão nos picos; dimensionar para o pico máximo gera enorme desperdício financeiro de máquinas ociosas.
3. **Indisponibilidade em Atualizações (*Downtime*):** Publicar novas versões exige parar o servidor antigo antes de subir o novo, interrompendo o atendimento na oficina.

## PROPOSTA TÉCNICA
Propõe-se a orquestração via **AWS EKS** gerenciado por Terraform (`infra-eks`), com manifestos declarativos versionados (`k8s/`):

```
                        [ AWS API Gateway / NLB ]
                                    |
                    +---------------+---------------+
                    |                               |
                    v (Tráfego HTTP /port 8080)     v
            +---------------+               +---------------+
            |  Pod Réplica 1|               |  Pod Réplica 2|
            |  (Spring Boot)|               |  (Spring Boot)|
            +---------------+               +---------------+
                    ^                               ^
                    |                               |
      +-----------------------------------------------------------+
      |            Horizontal Pod Autoscaler (HPA)                |
      |   Métricas: metrics.k8s.io (Target: CPU Utilization 70%)  |
      |   Escalabilidade: minReplicas: 2 | maxReplicas: 10        |
      +-----------------------------------------------------------+
```

### Componentes Técnicos da Proposta:

1. **Infraestrutura EKS (`tech-challenge-repairshop-infra-eks`):**
   - Cluster Kubernetes gerenciado na AWS com Node Groups compostos por instâncias alocadas nas sub-redes privadas.
   - Instalação do componente **Metrics Server** para habilitar a coleta de métricas de CPU/Memória pelo HPA.
2. **Manifestos Kubernetes Declarativos (`tech-challenge-repairshop-app/k8s/`):**
   - **`Deployment` (`deployment.yaml`):**
     - Estratégia de deploy `RollingUpdate` (`maxSurge: 1`, `maxUnavailable: 0`), garantindo que novas réplicas passem nos testes de saúde antes das antigas serem desativadas.
     - Definição explícita de `requests` e `limits` de CPU (250m/500m) e Memória (512Mi/1024Mi).
   - **Health Checks e Probes com Spring Boot Actuator:**
     - `startupProbe`: Aguarda até 60s para o carregamento inicial da JVM sem reiniciar o Pod prematuramente.
     - `readinessProbe`: Checa `/actuator/health/readiness` antes de direcionar tráfego do balanceador para o Pod.
     - `livenessProbe`: Checa `/actuator/health/liveness` periodicamente para reiniciar Pods que eventualmente entrem em estado travado (*deadlock*).
   - **`Service` (`service.yaml`):**
     - Serviço do tipo `LoadBalancer` ou `ClusterIP` com roteamento balanceado de tráfego interno.
   - **`HPA` (`hpa.yaml`):**
     - Escalonamento automático baseado em limiar de 70% de utilização de CPU, mantendo no mínimo 2 réplicas (alta disponibilidade em múltiplas AZs) e expandindo dinamicamente em picos de demanda.

## IMPACTO ESPERADO
- **Benefícios:**
  - Alta disponibilidade garantida com distribuição em múltiplas Zonas de Disponibilidade.
  - Auto-recuperação automática de falhas em contêineres sem intervenção humana (*self-healing*).
  - Escalonamento elástico automático: o sistema ajusta a capacidade de computação em tempo real.
  - Atualizações contínuas de versão com zero downtime (*Rolling Updates*).
- **Riscos e Mitigações:**
  - *Risco:* Custos fixos do plano de controle do EKS na AWS.
  - *Mitigação:* Planejamento de uso e automação de destruição de ambientes efêmeros para otimização de custos em laboratórios.
- **Impacto Operacional:**
  - Padrão moderno da indústria com suporte a ferramentas do ecossistema Cloud-Native Computing Foundation (CNCF).

## ALTERNATIVAS CONSIDERADAS
1. **AWS ECS (Elastic Container Service) com Fargate:**
   - *Prós:* Menor complexidade operacional que o Kubernetes.
   - *Contras:* Acoplamento a ferramentas proprietárias da AWS, menor ecossistema de ferramentas abertas (Kustomize, HPA customizado, operadores OTel, Helm).
2. **Máquinas Virtuais Puras (AWS EC2) com Auto Scaling Group:**
   - *Prós:* Simplicidade inicial de instâncias isoladas.
   - *Contras:* Tempo de subida de novas instâncias lento (minutos vs segundos de um contêiner Pod), falta de abstração de microsserviços e maior custo de infraestrutura.

## PONTOS EM ABERTO
- [x] Versão estável do Kubernetes no EKS (Alinhado: Kubernetes v1.29+).
- [x] Mínimo de réplicas no HPA para produção (Alinhado: Mínimo de 2 réplicas para garantir tolerância a falhas).
