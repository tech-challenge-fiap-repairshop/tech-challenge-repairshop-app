# ADR-002: Escolha do Banco de Dados Relacional PostgreSQL via AWS RDS e Versionamento com Flyway

**Data:** 2026-09-01  
**Status:** Aceito  
**Autor:** Grupo CAO (POSTECH 15SOAT)  

---

## Contexto

O domínio da oficina mecânica é altamente transacional e possui forte interdependência referencial entre suas entidades:
- Uma **Ordem de Serviço (OS)** está estritamente associada a um **Cliente**, a um **Veículo** específico e a múltiplos itens de **Execução de Serviços** e **Insumos/Peças**.
- O processo de atendimento exige movimentação de estoque de insumos (baixa de peças utilizadas em reparos), cálculo de valores de orçamentos, alterações sequenciais de status e geração de faturas (**Invoices**).
- Concorrência de escrita: Se múltiplos mecânicos ou atendentes manipularem a mesma OS ou realizarem baixas de um mesmo item de estoque limitado simultaneamente, o sistema deve garantir isolamento transacional estrito e evitar inconsistências (ex: estoque negativo, orçamentos inconsistentes).

Foram avaliadas abordagens de persistência NoSQL (como MongoDB ou DynamoDB) versus Bancos de Dados Relacionais (RDBMS).

---

## Decisão

Optamos pelo **PostgreSQL 16**, provisionado como banco gerenciado através do **AWS RDS (Relational Database Service)** em sub-redes privadas da VPC, integrando o **Flyway** para gerenciamento e versionamento imperativo de schemas e migrações de dados.

### Detalhes da Decisão e Implementação:

1. **Garantias ACID e Integridade Referencial:**
   - O modelo relacional permite o uso de *Foreign Keys*, *Unique Constraints* e *Check Constraints* nativas no banco para garantir que veículos pertençam a clientes válidos e que execuções estejam vinculadas a ordens existentes.
   - Transações com isolamento *Read Committed* / *Repeatable Read* asseguram que a dedução de estoque e a aprovação de ordens ocorram de forma atômica e consistente.

2. **AWS RDS PostgreSQL Gerenciado:**
   - O banco de dados é provisionado via Terraform no repositório `tech-challenge-repairshop-infra-db-rds`.
   - Isolamento total de segurança: alocado nas **sub-redes privadas da VPC**, sem IP público associado, acessível estritamente a partir dos nós do cluster EKS e da Lambda Auth através de Security Group dedicado (`aws_security_group.rds` na porta 5432).
   - Suporte nativo a backups automatizados diários, janelas de manutenção gerenciadas e facilidade de ampliação de capacidade de armazenamento (*Storage Autoscaling*).

3. **Versionamento de Schema com Flyway:**
   - Todas as alterações estruturais do banco (tabelas, índices, constraints e seeds de catálogo de insumos) são descritas em scripts SQL versionados (`V1__...sql`, `V2__...sql`) localizados na pasta `src/main/resources/db/migration` da aplicação.
   - A aplicação executa as migrações automaticamente no momento da inicialização, garantindo que os ambientes de desenvolvimento local, testes automatizados (Testcontainers), dev, hml e prd compartilhem exatamente a mesma estrutura de tabelas de forma determinística.

---

## Consequências

### Positivas
- **Integridade de Dados Garantida:** Elimina riscos de registros órfãos ou inconsistência contábil e de estoque através de constraints e transações relacionais robustas.
- **Rastreabilidade e Versionamento de Banco:** Toda mudança de banco de dados passa por code review no repositório Git e é aplicada de forma imperativa pelo Flyway, prevenindo divergências (*drift*) entre ambientes.
- **Baixo Custo Operacional de Gestão:** O AWS RDS automatiza rotinas de backup, restauração point-in-time e patches de segurança do sistema operacional do banco.
- **Compatibilidade Excelente:** Integração nativa com Spring Data JPA, Hibernate e ferramentas de testes como Testcontainers (onde um container Postgres idêntico é instanciado em milissegundos para testes de integração).

### Negativas / Trade-offs
- **Escalabilidade Horizontal de Escrita Mais Complexa:** Ao contrário de bancos NoSQL distribuídos, o PostgreSQL escala primariamente de forma vertical (tamanho da instância RDS) ou através de Read Replicas para leitura. No volume projetado da oficina, o modelo relacional atende com folga as demandas.
- **Custo Fixo de Instância:** A instância RDS em nuvem possui custo fixo mensal contínuo enquanto estiver provisionada (mitigado pelo script de destruição controlada para laboratórios acadêmicos).

### Riscos e Mitigações
- **Risco:** Bloqueio e contenção de concorrência em tabelas de insumos com alta frequência de atualização.
- **Mitigação:** Criação de índices estratégicos em chaves estrangeiras e uso de controle de concorrência otimista (`@Version`) ou consultas transacionais atômicas com `SELECT ... FOR UPDATE` onde estritamente necessário.
