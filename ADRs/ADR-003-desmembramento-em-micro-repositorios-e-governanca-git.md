# ADR-003: Desmembramento da Arquitetura em Múltiplos Repositórios Especializados e Governança Git

**Data:** 2026-09-01  
**Status:** Aceito  
**Autor:** Grupo CAO (POSTECH 15SOAT)  

---

## Contexto

Nas fases iniciais do projeto (Fase 1 e Fase 2), grande parte do código-fonte da aplicação, arquivos de configuração de infraestrutura do Terraform e manifestos de Kubernetes residiam concentrados em um único repositório (*monorepo* simplificado).

Com a evolução da solução para uma arquitetura em nuvem desacoplada e distribuída (Fase 3), surgiram gargalos e riscos significativos:
1. **Acoplamento de Deploys e Blast Radius:** Uma pequena alteração no código Kotlin da aplicação acionava desnecessariamente validações e planos de infraestrutura de rede e banco de dados, aumentando o tempo das esteiras e o risco de indisponibilidade por impacto acidental em recursos estáveis.
2. **Ciclos de Vida Independentes:** Componentes de infraestrutura de base (como a VPC e subnets) raramente sofrem modificações após criados, enquanto a aplicação e a função Lambda de autenticação possuem frequências diárias de commit e deploy.
3. **Governança e Segurança de Código:** Ausência de políticas estritas de proteção de branch permitia commits diretos na branch principal (`main`), violando padrões corporativos de auditoria e integração contínua.

---

## Decisão

Desmembrar o ecossistema em **6 repositórios Git independentes e especializados** hospedados na organização do GitHub, e estabelecer regras estritas de governança com **Branch Protection Rules**:

### Repositórios Especializados:

1. **`tech-challenge-repairshop-app`:** Repositório do código-fonte da aplicação principal (Kotlin / Spring Boot / JVM), suíte de testes unitários/integrados, documentação OpenAPI/Swagger e manifestos declarativos do Kubernetes (`k8s/`).
2. **`tech-challenge-repairshop-infra-network`:** Repositório responsável exclusivamente pela infraestrutura de rede base (VPC, Subnets Públicas e Privadas, Internet Gateway, NAT Gateway, Route Tables e registro de imagens AWS ECR).
3. **`tech-challenge-repairshop-infra-eks`:** Repositório responsável pelo provisionamento do Cluster EKS gerenciado, Node Groups, Metrics Server e Security Group dos nós (`aws_security_group.eks_nodes`).
4. **`tech-challenge-repairshop-infra-db-rds`:** Repositório responsável pela instância gerenciada do banco de dados PostgreSQL no AWS RDS, Subnet Group e Security Group do banco (`aws_security_group.rds`).
5. **`tech-challenge-repairshop-lambda-auth`:** Repositório contendo o código Java 21 da função Lambda de autenticação (`POST /auth/login`), sua infraestrutura Terraform dedicada e Security Group (`aws_security_group.lambda_sg`).
6. **`tech-challenge-repairshop-infra-apigateway`:** Repositório do AWS API Gateway (HTTP API v2), roteamento para Lambda Auth e proxy para os nós do EKS.

### Governança e Proteção de Branches:
- **Proibição de Push Direto na `main`:** Nenhum desenvolvedor pode fazer push direto na branch `main` em nenhum dos repositórios.
- **Pull Requests (PR) Obrigatórios:** Toda e qualquer alteração de código ou infraestrutura deve ser submetida via Pull Request.
- **Status Checks e Quality Gates:** O merge só é autorizado após a execução bem-sucedida de todas as validações automatizadas de CI (compilação, testes, linters e análise estática do SonarCloud).

---

## Consequências

### Positivas
- **Redução Drástica do Blast Radius:** Falhas ou modificações em um repositório (ex: ajuste de rota no API Gateway) não afetam a infraestrutura de banco de dados ou a rede da VPC.
- **Esteiras de CI/CD Rápidas e Especializadas:** Cada repositório possui uma pipeline enxuta com tempo de execução otimizado, executando apenas o que é relevante para seu contexto.
- **Clareza de Responsabilidade e Separação de Papéis:** Separação clara entre responsabilidades de infraestrutura/plataforma (DevOps/SRE) e engenharia de software de aplicação.
- **Rastreabilidade e Conformidade:** Histórico de auditoria preservado via Pull Requests aprovados com checks de segurança.

### Negativas / Trade-offs
- **Gerenciamento de Múltiplos Repositórios:** Aumento da quantidade de repositórios a serem clonados e configurados localmente pelo time de desenvolvimento.
- **Coordenação de Dependências:** Alterações estruturais amplas que envolvam rede, banco e código exigem a execução e merge de PRs em múltiplos repositórios em sequência ordenada.

### Riscos e Mitigações
- **Risco:** Desalinhamento na ordem de execução de deploys manuais entre repositórios interdependentes.
