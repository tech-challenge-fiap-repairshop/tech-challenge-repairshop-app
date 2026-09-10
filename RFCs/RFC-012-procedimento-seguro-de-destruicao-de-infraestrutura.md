# RFC – Procedimento Controlado de Destruição de Infraestrutura em Nuvem com Safety Gate

## DATA
27/08/2026

## STATUS
Encerrada – Aprovada

> **Nota de Encerramento:** Proposta aprovada e padronizada em todos os repositórios da infraestrutura em nuvem, originando o [ADR-012](../ADRs/ADR-012-procedimento-seguro-de-destruicao-de-infraestrutura.md).

## RESUMO
Proposta de criação de um procedimento automatizado e padronizado de destruição controlada de infraestrutura em nuvem (AWS) através de workflows manuais no GitHub Actions (`destroy.yml`) e scripts de orquestração (`destroy_all_infra.ps1/.sh`), incorporando Safety Gate com palavra-chave de confirmação, limpeza prévia de workloads do Kubernetes e pausas temporais para desalocação de ENIs e Load Balancers.

## PROBLEMA
Em contas acadêmicas (AWS Academy) e ambientes de laboratório ou validação contínua, recursos como Clusters EKS, nós de computação, instâncias RDS e NAT Gateways geram custos contínuos e consomem cotas financeiras limitadas.

Contudo, a destruição desses recursos na nuvem pública envolve riscos e complexidades severas:
1. **Risco de Destruição Acidental:** Um clique equivocado em um pipeline automático ou a execução de comandos de exclusão sem travas em ambientes compartilhados ou produtivos resultaria em perda irreparável de dados e indisponibilidade.
2. **Travamento de Dependências de Rede na VPC (*Dependency Lock*):** A execução direta de `terraform destroy` na VPC quase sempre falha com mensagens de erro de recursos ocupados (`DependencyViolation: The subnet has active interfaces`). Isso acontece porque os nós do Kubernetes e os Load Balancers criados pelos Services do K8s mantêm Interfaces de Rede Elásticas (ENIs) ativas na AWS que não foram criadas diretamente pelo Terraform da rede.
3. **Ordem Inversa de Dependência Mandatória:** Tentar destruir a VPC antes do banco RDS ou do EKS resulta em falhas em cascata de infraestrutura.

## PROPOSTA TÉCNICA
Propõe-se a criação de um fluxo de destruição estritamente ordenado e protegido em todas as camadas de automação:

```
[ Gatilho Manual: workflow_dispatch ou destroy_all_infra ]
                           |
                           v
+---------------------------------------------------------+
|                  SAFETY GATE OBRIGATÓRIO                |
|  Parâmetro de Confirmação Literal == "DESTRUIR"         |
|  (Qualquer outro valor aborta a execução na hora)       |
+---------------------------------------------------------+
                           |
                           v
+---------------------------------------------------------+
| PASSO 1: Exclusão Limpa do Kubernetes (Kubectl Clean)   |
| kubectl delete -f k8s/ --ignore-not-found=true          |
| -> Solicita desmontagem de Load Balancers da AWS        |
+---------------------------------------------------------+
                           |
                           v
+---------------------------------------------------------+
| PASSO 2: Pausa Controlada de 60 Segundos (Sleep 60s)    |
| -> Tempo hábil para a AWS desalocar ENIs e ELBs da VPC  |
+---------------------------------------------------------+
                           |
                           v
+---------------------------------------------------------+
| PASSO 3: Ordem Inversa de Destruição via Terraform      |
| 1. infra-apigateway  -> terraform destroy               |
| 2. lambda-auth       -> terraform destroy               |
| 3. infra-eks         -> terraform destroy               |
| 4. infra-db-rds      -> terraform destroy               |
| 5. infra-network     -> terraform destroy               |
+---------------------------------------------------------+
```

### Componentes Chave da Proposta:

1. **Safety Gate Mandatório:**
   - O workflow `destroy.yml` exige como entrada um campo de texto obrigatório.
   - Apenas o valor literal `DESTRUIR` permite o avanço do job. Qualquer outro valor cancela a execução imediatamente com log informativo.
2. **Limpeza Prévia do Kubernetes e Pausa de Desalocação:**
   - Antes de destruir o cluster EKS ou a VPC, executa-se a remoção de todos os manifestos de aplicação para liberar IPs e interfaces de rede.
   - Aplicação de `sleep 60` para garantir a liberação completa das interfaces elásticas pela AWS.
3. **Scripts de Orquestração Unificada:**
   - Criação de `destroy_all_infra.ps1` (PowerShell para Windows) e `destroy_all_infra.sh` (Bash para Linux/macOS) no repositório `tech-challenge-wiki-docs`, permitindo a execução sequencial completa de todos os módulos.
4. **Isolamento de Ambientes:**
   - A destruição é estritamente parametrizada por ambiente (`dev`, `hml`, `prd`), garantindo que a remoção do ambiente de desenvolvimento nunca afete os arquivos de estado de outros ambientes.

## IMPACTO ESPERADO
- **Benefícios:**
  - Capacidade de desligar e destruir 100% dos recursos em nuvem em 15 minutos, economizando 100% dos custos em momentos ociosos.
  - Eliminação definitiva de erros de dependências de rede presas na VPC (*DependencyViolation*).
  - Blindagem contra acionamentos acidentais via Safety Gate.
  - Total reprodutibilidade para qualquer engenheiro do time.
- **Riscos e Mitigações:**
  - *Risco:* Destruição de dados do banco de dados RDS sem snapshot.
  - *Mitigação:* Alinhamento explícito de que ambientes efêmeros de teste utilizam seeds automatizados e dados voláteis.
- **Impacto Operacional:**
  - Eficiência financeira máxima e facilidade de manutenção de infraestrutura efêmera.

## ALTERNATIVAS CONSIDERADAS
1. **Exclusão Manual pelo Console da AWS (ClickOps):**
   - *Prós:* Não precisa de código.
   - *Contras:* Processo demorado (horas), propenso a esquecer recursos órfãos cobrando na conta (ex: Volumes EBS ou Elastic IPs não desassociados).
2. **Execução de `terraform destroy` Desordenada:**
   - *Prós:* Tentativa direta em um único comando.
   - *Contras:* Falhas constantes por bloqueio de portas e dependências de rede não desalocadas.

## PONTOS EM ABERTO
- [x] Palavra-chave do Safety Gate (Alinhado: `DESTRUIR`).
- [x] Tempo de espera para desalocação de ENIs (Alinhado: 60 segundos).
