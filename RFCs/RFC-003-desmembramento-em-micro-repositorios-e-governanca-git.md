# RFC – Desmembramento da Arquitetura em Múltiplos Repositórios Especializados e Governança Git

## DATA
18/08/2026

## STATUS
Encerrada – Aprovada

> **Nota de Encerramento:** Proposta aprovada por todo o time de arquitetura e DevOps, originando o [ADR-003](../ADRs/ADR-003-desmembramento-em-micro-repositorios-e-governanca-git.md).

## RESUMO
Proposta de desmembramento do monorepo inicial em 6 repositórios Git independentes e especializados na organização do GitHub, com estabelecimento de políticas estritas de governança com Branch Protection Rules, obrigatoriedade de Pull Requests e Quality Gates.

## PROBLEMA
Nas fases iniciais do projeto (Fases 1 e 2), a aplicação, manifestos Kubernetes, scripts de automação e módulos do Terraform estavam concentrados em um único repositório Git (*monorepo* simplificado).

Com o crescimento da solução e a introdução de múltiplos serviços em nuvem (Fase 3), surgiram problemas críticos:
1. **Acoplamento de Deploys e Alto Blast Radius:** Qualquer alteração no código Kotlin da aplicação acionava esteiras de CI/CD completas de infraestrutura, arriscando alterações não intencionais na rede da VPC ou no banco de dados.
2. **Ciclos de Vida Desiguais:** O código da aplicação sofre commits diários, enquanto a VPC e as subnets permanecem inalteradas por meses após a criação. Tratar tudo no mesmo repositório gerava desperdício de minutos de pipeline e riscos de instabilidade.
3. **Falta de Governança e Rastreabilidade:** Commits diretos na branch `main` sem revisão por pares permitiam que código não testado ou sem validação estática de segurança fosse publicado.

## PROPOSTA TÉCNICA
Propõe-se dividir a arquitetura em **6 repositórios especializados** e 1 repositório de documentação centralizada na organização do GitHub:

```
+-----------------------------------------------------------------------------------------+
|                                    ORGANIZAÇÃO GITHUB                                   |
+-----------------------------------------------------------------------------------------+
| 1. tech-challenge-repairshop-app              (Código Kotlin, K8s, OpenAPI, Testes)     |
| 2. tech-challenge-repairshop-infra-network    (VPC, Subnets, IGW, NAT, ECR, IaC)       |
| 3. tech-challenge-repairshop-infra-eks        (Cluster EKS, Node Groups, SG dos Nós)    |
| 4. tech-challenge-repairshop-infra-db-rds     (RDS PostgreSQL, Subnet Group, SG do RDS) |
| 5. tech-challenge-repairshop-lambda-auth      (Código Java 21 Lambda, IaC, SG da Lambda)|
| 6. tech-challenge-repairshop-infra-apigateway (AWS API Gateway HTTP v2, Rotas, Proxies) |
+-----------------------------------------------------------------------------------------+
```

### Regras de Governança Git Mandatórias:
- **Branch Protection Rules na branch `main`:**
  - Bloqueio total de push direto (`git push origin main` desabilitado para todos os membros).
  - Bloqueio de exclusão ou force-push na branch `main`.
- **Fluxo de Trabalho com Pull Requests (PR):**
  - Toda modificação deve ser criada a partir de uma branch de feature (`feat/...`, `fix/...`, `chore/...`).
  - Obrigatório abrir Pull Request apontando para `main`.
- **Status Checks e Quality Gates:**
  - O merge só é liberado se a pipeline de CI passar com sucesso em todos os passos (compilação, execução de testes unitários/integrados, checagem de linters e aprovação no SonarCloud Quality Gate).

## IMPACTO ESPERADO
- **Benefícios:**
  - Isolamento de falhas (*blast radius* reduzido a zero entre módulos independentes).
  - Esteiras de CI/CD muito mais rápidas e focadas estritamente no escopo alterado.
  - Clareza total de responsabilidades entre equipes de infraestrutura e engenharia de software.
  - Conformidade com os padrões de segurança e auditoria exigidos pelo mercado corporativo.
- **Riscos e Mitigações:**
  - *Risco:* Dificuldade de orquestrar a execução de alterações dependentes entre múltiplos repositórios.
  - *Mitigação:* Elaboração de scripts unificados de orquestração (`create_all_infra.ps1/.sh`) e manuais detalhados de provisionamento no repositório `tech-challenge-wiki-docs`.
- **Impacto Operacional:**
  - Melhor organização do time e paralelização do desenvolvimento de features e melhorias de infraestrutura.

## ALTERNATIVAS CONSIDERADAS
1. **Manutenção do Monorepo com Ferramentas Avançadas (Nx / Bazel / Turborepo):**
   - *Prós:* Manutenção de um único repositório Git local.
   - *Contras:* Alta complexidade de configuração de grafos de dependência e ferramentas de build que fogem do escopo do projeto, além de acoplamento de permissões e branches.
2. **Separação Apenas em Dois Repositórios (App e Infraestrutura Geral):**
   - *Prós:* Menos repositórios para gerenciar.
   - *Contras:* Mantém o acoplamento perigoso entre rede, banco de dados, cluster EKS e API Gateway no mesmo pipeline de Terraform.

## PONTOS EM ABERTO
- [x] Definição da convenção de nomenclatura dos repositórios (Alinhado: Prefixo padronizado `tech-challenge-repairshop-*`).
- [x] Configuração dos Rulesets no GitHub (Alinhado: Arquivos JSON de ruleset criados e aplicados para branch protection).
