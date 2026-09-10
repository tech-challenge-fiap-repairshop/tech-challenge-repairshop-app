# RFC – Topologia de Rede VPC Unificada e Descentralização do Ciclo de Vida de Security Groups

## DATA
20/08/2026

## STATUS
Encerrada – Aprovada

> **Nota de Encerramento:** Proposta aprovada e alinhada com as recomendações do *AWS Well-Architected Framework*, gerando o [ADR-005](../ADRs/ADR-005-topologia-de-rede-vpc-e-security-groups-descentralizados.md).

## RESUMO
Proposta de criação de uma topologia de rede segura em VPC unificada com sub-redes públicas e privadas em múltiplas Zonas de Disponibilidade, adotando a descentralização do ciclo de vida dos Security Groups para os seus respectivos módulos de serviço.

## PROBLEMA
Em uma infraestrutura em nuvem composta por camadas heterogêneas (API Gateway público, contêineres de aplicação no Kubernetes EKS, função Serverless Lambda e banco de dados relacional RDS PostgreSQL), a gestão de redes e segurança impõe dois grandes desafios:
1. **Risco de Exposição de Recursos Críticos:** A alocação indevida de instâncias de banco de dados ou nós de computação em subnets públicas com IPs públicos criaria vulnerabilidades graves contra varreduras de portas e ataques externos da internet.
2. **Antipadrão de Security Groups Centralizados:** Em muitas arquiteturas legadas, todos os Security Groups (banco, nós, balanceadores, lambdas) são criados dentro do repositório de rede (`infra-network`). Isso gera dependências circulares e acoplamento rígido: qualquer ajuste fino de porta da aplicação exige alterações e deploys no repositório de rede da empresa, aumentando o risco de indisponibilidade geral.

## PROPOSTA TÉCNICA
Propõe-se uma arquitetura de rede em conformidade com o **AWS Well-Architected Framework (Security Pillar)**, segregando a rede base da gestão dos firewalls de workloads:

```
                            [ INTERNET ]
                                 |
                        [ Internet Gateway ]
                                 |
+--------------------------------+--------------------------------+
| AWS VPC (dev: 10.0.0.0/16 | hml: 10.1.0.0/16 | prd: 10.2.0.0/16)|
|                                                                 |
| [ SUB-REDES PÚBLICAS (us-east-1a / us-east-1b) ]                |
|  - NAT Gateway (Elastic IP)                                     |
|  - Internet Gateway Routing (0.0.0.0/0 -> IGW)                  |
|                                |                                |
| ------------------------------ | ------------------------------ |
|                                v                                |
| [ SUB-REDES PRIVADAS (us-east-1a / us-east-1b) ]                |
|  - Saída para Internet exclusivamente via NAT Gateway           |
|                                                                 |
|    +--------------------+             +--------------------+    |
|    |    EKS Cluster     |             |  AWS Lambda Auth   |    |
|    | aws_security_group |             | aws_security_group |    |
|    |     .eks_nodes     |             |     .lambda_sg     |    |
|    +--------------------+             +--------------------+    |
|               \                                 /               |
|                \ (Porta 5432 - Restrita ao CIDR)/               |
|                 v                              v                |
|               +----------------------------------+              |
|               |        AWS RDS PostgreSQL        |              |
|               |       aws_security_group.rds     |              |
|               +----------------------------------+              |
+-----------------------------------------------------------------+
```

### Detalhes Técnicos da Topologia e Segurança:

1. **Repositório de Rede Base (`infra-network`):**
   - Provisiona 1 VPC unificada com blocos CIDR específicos por ambiente (`10.0.0.0/16` para `dev`, `10.1.0.0/16` para `hml`, `10.2.0.0/16` para `prd`).
   - Cria **2 Sub-redes Públicas** e **2 Sub-redes Privadas** distribuídas entre `us-east-1a` e `us-east-1b`.
   - Provisiona 1 **Internet Gateway (IGW)** para a rota pública e 1 **NAT Gateway com Elastic IP (EIP)** para roteamento seguro de saída da rede privada.
   - **Zero Security Groups de Workloads:** O módulo apenas exporta os outputs (`vpc_id`, `public_subnets`, `private_subnets`, `private_subnet_cidr_blocks`).
2. **Descentralização dos Security Groups nos Módulos Consumidores:**
   - **`infra-eks`:** Gerencia `aws_security_group.eks_nodes` (porta 8080 para aplicação, portas 4317/4318 para OTel Collector e saída irrestrita para downloads do ECR e internet via NAT).
   - **`infra-db-rds`:** Gerencia `aws_security_group.rds`, permitindo conexões de entrada na porta 5432 **estritamente originadas dos blocos CIDR das sub-redes privadas da VPC**, bloqueando qualquer IP externo.
   - **`lambda-auth`:** Gerencia `aws_security_group.lambda_sg`, com permissões para conectar na VPC e se comunicar com o banco de dados.

## IMPACTO ESPERADO
- **Benefícios:**
  - Isolamento de rede em profundidade (*Defense in Depth*): banco e contêineres sem IPs públicos.
  - Eliminação de acoplamento e dependências circulares entre repositórios de IaC.
  - Autonomia para cada time de serviço manter suas próprias regras de portas e firewalls.
- **Riscos e Mitigações:**
  - *Risco:* Custo contínuo do NAT Gateway na sub-rede pública.
  - *Mitigação:* O uso de um único NAT Gateway compartilhado por ambiente atende a demanda de laboratório e desenvolvimento com custo controlado.
- **Impacto Operacional:**
  - Facilidade de manutenção e alta estabilidade de rede.

## ALTERNATIVAS CONSIDERADAS
1. **Centralização de todos os Security Groups no repositório `infra-network`:**
   - *Prós:* Todos os firewalls em um único arquivo de Terraform.
   - *Contras:* Violação do princípio de menor privilégio e criação de acoplamento rígido entre a infraestrutura de rede e as portas de negócio de cada aplicação.
2. **Subnets Públicas para Todos os Serviços com Restrição por Firewall:**
   - *Prós:* Não exige NAT Gateway.
   - *Contras:* Risco gravíssimo de exposição de dados e violação direta de padrões de segurança da AWS e da LGPD.

## PONTOS EM ABERTO
- [x] Regra de liberação de porta do RDS (Alinhado: Liberar exclusivamente para o bloco CIDR das sub-redes privadas).
- [x] Alocação dos pods do EKS (Alinhado: Alocados estritamente nas subnets privadas).
