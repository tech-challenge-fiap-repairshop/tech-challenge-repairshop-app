# RFC – Escolha do Banco de Dados Relacional PostgreSQL via AWS RDS e Versionamento com Flyway

## DATA
16/08/2026

## STATUS
Encerrada – Aprovada

> **Nota de Encerramento:** Proposta aprovada por unanimidade, resultando na formalização do [ADR-002](../ADRs/ADR-002-banco-de-dados-relacional-postgresql.md).

## RESUMO
Proposta de utilização do banco de dados relacional PostgreSQL 16 provisionado de forma gerenciada na nuvem através do AWS RDS (Relational Database Service) em sub-redes privadas da VPC, com versionamento e migração de schema controlados pelo Flyway.

## PROBLEMA
O sistema de gestão da oficina mecânica lida com dados altamente estruturados e relacionamentos estritos entre entidades centrais:
- Clientes possuem Veículos; Veículos geram Ordens de Serviço (OS);
- Uma Ordem de Serviço agrupa Itens de Serviço, Mecânicos alocados e Insumos/Peças consumidos do Estoque;
- O fechamento da OS gera uma Fatura (*Invoice*) e baixa física no estoque.

Principais desafios identificados na modelagem e persistência:
1. **Necessidade de Integridade Referencial Estrita:** Registros órfãos (ex: uma OS sem cliente ou uma baixa de estoque sem ordem correspondente) causariam graves inconsistências contábeis e fiscais.
2. **Concorrência e Isolamento Transacional:** Dois atendentes ou mecânicos manipulando orçamentos ou reservando as últimas unidades de uma peça de reposição simultaneamente precisam de controle transacional atômico e confiável (ACID) para evitar estoque negativo.
3. **Gerenciamento de Mudanças de Schema:** Mudanças manuais em tabelas de banco de dados diretamente pelo console (*drift*) causam quebras frequentes entre ambientes de desenvolvimento, homologação e produção.
4. **Segurança e Operação em Nuvem:** A base de dados não pode estar exposta à internet pública e deve contar com rotinas automáticas de backup e recuperação.

## PROPOSTA TÉCNICA
Propõe-se a adoção do **PostgreSQL 16** como banco de dados principal da aplicação, hospedado no serviço **AWS RDS** dentro da VPC privada, com migrações automatizadas via **Flyway**:

```
[ AWS VPC (Subnets Privadas) ]
 +---------------------------------------------------------+
 |                                                         |
 |   [ EKS Cluster ]               [ AWS Lambda Auth ]     |
 |   (Pods Spring Boot)            (Função de Login)       |
 |           |                              |              |
 |           | (Porta 5432)                 | (Porta 5432) |
 |           v                              v              |
 |   +-------------------------------------------------+   |
 |   |         AWS RDS PostgreSQL (Porta 5432)        |   |
 |   |         Security Group: aws_security_group.rds  |   |
 |   |         - Storage Auto-Scaling                  |   |
 |   |         - Automated Daily Backups               |   |
 |   +-------------------------------------------------+   |
 +---------------------------------------------------------+
```

### Detalhes da Proposta:

1. **Garantias ACID e Relacionamentos:**
   - Modelagem com chaves primárias UUID/BigInt, Foreign Keys com restrições `ON DELETE RESTRICT` e Unique Constraints em CPF/CNPJ, Placas e códigos de Insumo.
   - Nível de isolamento transacional `READ COMMITTED` nativo com suporte a locks pessimistas (`SELECT ... FOR UPDATE`) em cenários críticos de controle de estoque concorrente.
2. **Provisionamento Gerenciado com Terraform (`infra-db-rds`):**
   - Instância alocada em **Sub-redes Privadas** (sem IP público).
   - Segurança baseada em Security Group (`aws_security_group.rds`) aceitando tráfego de entrada na porta `5432` exclusivamente dos blocos CIDR das sub-redes privadas da VPC.
   - Retenção de backup configurável por ambiente (7 dias em produção).
3. **Versionamento e Migrações com Flyway:**
   - Scripts SQL declarativos e versionados (`V1__create_tables.sql`, `V2__seed_initial_data.sql`, etc.) armazenados no repositório `tech-challenge-repairshop-app`.
   - Execução automática das migrations no startup da aplicação Spring Boot, garantindo consistência determinística em containers locais, CI/CD e ambientes AWS.

## IMPACTO ESPERADO
- **Benefícios:**
  - 100% de garantia de integridade e consistência dos dados do negócio.
  - Zero risco de divergência de schema entre ambientes (Dev, Hml, Prd).
  - Gestão operacional simplificada (backups, patches e snapshots automatizados pelo RDS).
  - Testabilidade local perfeita via Testcontainers instanciando containers PostgreSQL reais.
- **Riscos e Mitigações:**
  - *Risco:* Custos de manter uma instância RDS provisionada continuamente durante períodos ociosos de desenvolvimento.
  - *Mitigação:* Integração com scripts de destruição automatizada (`destroy_all_infra.ps1/.sh`) para laboratórios.
- **Impacto Operacional:**
  - Baixa manutenção para o time de desenvolvimento com alta confiabilidade operacional.

## ALTERNATIVAS CONSIDERADAS
1. **Banco NoSQL Orientado a Documentos (MongoDB / Amazon DocumentDB):**
   - *Prós:* Esquema flexível e facilidade de manipulação de JSONs.
   - *Contras:* Falta de integridade referencial nativa (Foreign Keys), risco de inconsistência em transações multi-documentos e complexidade para queries analíticas e relatórios de estoque/faturamento.
2. **Banco Relacional MySQL / MariaDB:**
   - *Prós:* Popularidade e facilidade de uso.
   - *Contras:* Suporte a tipos JSONB, concorrência MVCC e extensões avançadas inferior ao PostgreSQL 16.
3. **Instância PostgreSQL Auto-Hospedada em EC2:**
   - *Prós:* Custo de licença direto menor.
   - *Contras:* Alto custo de manutenção humana (necessidade de configurar réplicas, backups, scripts de failover e atualizações de SO manualmente).

## PONTOS EM ABERTO
- [x] Definição do tamanho da instância RDS por ambiente (Alinhado: `db.t3.micro` em Dev/Hml para economia de custos e `db.t3.small`/`db.t3.medium` para Prd).
- [x] Estratégia de credenciais seguras (Alinhado: Injeção de usuário e senha via variáveis secretas do GitHub Actions e Secrets do Kubernetes/Terraform).
