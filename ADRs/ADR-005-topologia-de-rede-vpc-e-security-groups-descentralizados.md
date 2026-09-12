# ADR-005: Topologia de Rede VPC Unificada e Descentralização do Ciclo de Vida de Security Groups

**Data:** 2026-09-01  
**Status:** Aceito  
**Autor:** Grupo CAO (POSTECH 15SOAT)  

---

## Contexto

A arquitetura em nuvem da aplicação é composta por múltiplos serviços com diferentes requisitos de exposição e segurança:
- **Componentes Públicos:** AWS API Gateway e Internet Gateway (IGW), que recebem tráfego externo de clientes e navegadores.
- **Componentes Privados e Críticos:** Worker Nodes do cluster AWS EKS, funções AWS Lambda e a instância do banco de dados relacional AWS RDS PostgreSQL.

Expor o banco de dados ou os nós de computação diretamente à internet pública com IPs públicos configuraria uma vulnerabilidade grave de segurança.

Além disso, em modelos tradicionais de IaC, é comum concentrar a criação de todos os Security Groups da empresa dentro do módulo central de rede (`infra-network`). No entanto, esse antipadrão cria dependências circulares e acoplamento rígido: para alterar uma porta do banco de dados ou do coletor OpenTelemetry, era necessário modificar o repositório de rede, arriscando indisponibilidade geral.

---

## Decisão

Adotar uma **Topologia de Rede VPC Unificada com Subnets Públicas e Privadas** associada ao padrão ouro de **Descentralização do Ciclo de Vida de Security Groups**, em estrita conformidade com o *AWS Well-Architected Framework*.

### Detalhes Técnicos da Topologia e Segurança:

1. **Topologia da VPC (`tech-challenge-repairshop-infra-network`):**
   - **VPC Unificada:** CIDR blocks parametrizados (`10.0.0.0/16` em dev, `10.1.0.0/16` em hml, `10.2.0.0/16` em prd) distribuídos em 2 Zonas de Disponibilidade (AZs `us-east-1a` e `us-east-1b`).
   - **2 Sub-redes Públicas:** Associadas a uma Route Table conectada ao **Internet Gateway (IGW)**. Hospedam o **NAT Gateway com Elastic IP (EIP)**.
   - **2 Sub-redes Privadas:** Associadas a Route Tables que direcionam o tráfego de saída (`0.0.0.0/0`) através do NAT Gateway. Hospedam os nós do EKS, a Lambda e o RDS PostgreSQL.
   - **Isolamento de Responsabilidade do Módulo de Rede:** O repositório `infra-network` é agnóstico aos workloads. Ele apenas provisiona a rede base e exporta os outputs fundamentais (`vpc_id`, `vpc_cidr_block`, `public_subnets`, `private_subnets`, `private_subnet_cidr_blocks`).

2. **Descentralização dos Security Groups (Padrão Ouro AWS):**
   Cada serviço gerencia exclusivamente seu próprio Security Group em seu respectivo repositório de infraestrutura:
   - **`tech-challenge-repairshop-infra-eks` (`aws_security_group.eks_nodes`):**
     - Entrada na porta `8080` (aplicação Spring Boot) autorizada a partir do CIDR da VPC.
     - Entrada nas portas `4317` e `4318` (OpenTelemetry Collector gRPC/HTTP) para receber telemetria dos nós e da Lambda.
     - Saída irrestrita (`0.0.0.0/0`) para permitir download de imagens do ECR e comunicação com o banco e APIs.
   - **`tech-challenge-repairshop-infra-db-rds` (`aws_security_group.rds`):**
     - Entrada restrita na porta `5432` (PostgreSQL) aceitando conexões **exclusivamente originadas dos blocos CIDR das sub-redes privadas da VPC** (`private_subnet_cidr_blocks`).
     - Nenhuma conexão direta da internet é permitida.
   - **`tech-challenge-repairshop-lambda-auth` (`aws_security_group.lambda_sg`):**
     - Security Group alocado na VPC com regras de saída para comunicação interna segura com a aplicação e serviços da AWS.

---

## Consequências

### Positivas
- **Segurança e Isolamento em Profundidade:** Workloads críticos (RDS e EKS) não possuem IPs públicos e estão completamente blindados contra varreduras e ataques originados diretamente da internet.
- **Desacoplamento do Ciclo de Vida (Well-Architected):** O time responsável pelo cluster EKS pode ajustar portas de serviços internos sem precisar de alterações ou permissões no repositório de rede VPC.
- **Eliminação de Dependências Circulares:** Não há necessidade de referenciar IDs de Security Groups cruzados no Terraform; o isolamento é feito de forma limpa via blocos CIDR de sub-redes privadas.
- **Conectividade de Saída Segura:** Nós privados conseguem realizar downloads de pacotes, imagens Docker no ECR e chamadas HTTPS externas através do NAT Gateway.

### Negativas / Trade-offs
- **Custo do NAT Gateway:** O NAT Gateway provisionado na sub-rede pública possui custo por hora e por volume de dados trafegado.
- **Tráfego de Saída Centralizado:** Todo o tráfego originado nas sub-redes privadas compartilha o mesmo gateway de saída para a internet.

### Riscos e Mitigações
- **Risco:** Latência ou gargalo de tráfego de saída no NAT Gateway em picos de transferência de dados.
- **Mitigação:** O NAT Gateway da AWS é um serviço elástico e totalmente gerenciado capaz de escalar automaticamente até 45 Gbps por gateway.
