# RFC – Automação de CI/CD com GitHub Actions, Testes com Testcontainers, Trivy e Quality Gate SonarCloud

## DATA
25/08/2026

## STATUS
Encerrada – Aprovada

> **Nota de Encerramento:** Proposta aprovada e padronizada em todos os repositórios da organização, resultando no [ADR-010](../ADRs/ADR-010-esteira-ci-cd-automacao-de-testes-e-quality-gate.md).

## RESUMO
Proposta de implementação de esteiras de Integração Contínua e Entrega Contínua (CI/CD) automatizadas utilizando GitHub Actions, incorporando testes de integração com banco real via Testcontainers, análise estática de código e cobertura mínima de 80% no SonarCloud, verificação de vulnerabilidades em contêineres com Trivy, testes de fumaça (*Smoke Tests*) e portões de segurança (*Safety Gates*).

## PROBLEMA
No desenvolvimento de aplicações distribuídas em múltiplos repositórios e ambientes em nuvem, a ausência de uma esteira rigorosa de validação automatizada acarreta sérios riscos:
1. **Falsos Positivos com Bancos em Memória (H2):** Testes de integração rodando em bancos H2 passam com sucesso localmente, mas quebram em produção devido a diferenças sutis de tipos de dados (UUID, JSONB), dialetos SQL, triggers ou locks concorrentes do PostgreSQL real.
2. **Degradação de Qualidade e Dívida Técnica:** Sem uma verificação estrita de cobertura de testes e análise estática, novos códigos podem introduzir vulnerabilidades de segurança (OWASP Top 10), duplicações e bugs silenciosos na branch principal.
3. **Imagens Docker Vulneráveis no ECR:** Publicar contêineres sem escaneamento prévio pode levar pacotes com vulnerabilidades críticas conhecidas (*CVEs*) para o ambiente de produção.
4. **Deploys Acidentais em Produção:** Abertura de Pull Requests não pode provocar alterações em infraestrutura real (`terraform apply`) antes de aprovação por pares.

## PROPOSTA TÉCNICA
Propõe-se a padronização de workflows robustos no **GitHub Actions** em todos os repositórios, integrando as seguintes etapas obrigatórias:

```
[ Git Push / Pull Request ]
             |
             +-----------------------+-----------------------+
             |                                               |
             v (Job de Infraestrutura)                       v (Job de Aplicação / Backend)
+-------------------------+                     +---------------------------------------+
| Terraform Lint & Validate|                     | Compilação Java 21 / Maven            |
| Terraform Plan (Dry-Run)|                     | Testes Unitários com MockK            |
+-------------------------+                     | Testes Integrados com Testcontainers  |
                                                |  (Instância Real PostgreSQL 16)       |
                                                | JaCoCo Coverage Report                |
                                                +---------------------------------------+
                                                                     |
                                                                     v
                                                +---------------------------------------+
                                                | SonarCloud Scanner Quality Gate       |
                                                | (Bloqueia se Cobertura < 80% ou Bugs) |
                                                +---------------------------------------+
                                                                     |
                                                                     v
                                                +---------------------------------------+
                                                | Docker Build & Trivy Security Scan    |
                                                | Smoke Test de Inicialização do Pod    |
                                                +---------------------------------------+
                                                                     |
                                                                     v
                                                +---------------------------------------+
                                                | Push AWS ECR & Rollout no AWS EKS     |
                                                +---------------------------------------+
```

### Componentes Chave da Proposta:

1. **Paralelismo Inteligente:** Execução concorrente entre o job de validação de Terraform e o job de compilação/testes da aplicação, otimizando o tempo total de execução da esteira.
2. **Testcontainers com PostgreSQL 16:**
   - A suíte de testes de integração sobe contêineres Docker efêmeros e idênticos ao banco de produção.
   - Aplicação automática das migrations do Flyway durante a execução dos testes.
3. **Quality Gate Estrito no SonarCloud:**
   - Métricas mínimas exigidas: **80% de cobertura em código novo**, 0 bugs, 0 vulnerabilidades e *Rating A* em segurança e manutenibilidade.
4. **Escaneamento de Imagens com Trivy e Smoke Test:**
   - O Trivy analisa a imagem Docker gerada e falha o build caso encontre vulnerabilidades de severidade Alta ou Crítica sem patch.
   - Um contêiner temporário é iniciado e seu endpoint `/actuator/health` é testado via curl antes do upload para o Amazon ECR.
5. **Segurança em Pull Requests:**
   - Em PRs, a esteira executa estritamente validações (`test`, `lint`, `plan`, `sonar`). Apenas merges na branch `main` possuem permissão para executar `terraform apply` ou publicar imagens no ECR.

## IMPACTO ESPERADO
- **Benefícios:**
  - Garantia de que 100% do código na branch principal atenda aos padrões de segurança, cobertura e qualidade.
  - Eliminação de falhas causadas por diferenças entre ambientes de teste e produção.
  - Deploys previsíveis, automatizados e rastreáveis no Kubernetes EKS.
- **Riscos e Mitigações:**
  - *Risco:* Tempo de execução da pipeline aumentar com múltiplos passos e downloads de imagens Docker.
  - *Mitigação:* Configuração de cache nativo do Maven (`actions/setup-java` com `cache: 'maven'`) e cache de camadas do Docker.
- **Impacto Operacional:**
  - Confiança total do time de desenvolvimento para realizar entregas contínuas com segurança.

## ALTERNATIVAS CONSIDERADAS
1. **Bancos em Memória (H2) nos Testes de Integração:**
   - *Prós:* Execução alguns segundos mais rápida.
   - *Contras:* Falsos positivos constantes por incompatibilidades de dialeto SQL e tipos de dados com o PostgreSQL real.
2. **Deploys Manuais com Scripts Locais:**
   - *Prós:* Flexibilidade inicial no terminal.
   - *Contras:* Total falta de rastreabilidade, dependência da máquina do desenvolvedor e risco de vazamento de credenciais locais.

## PONTOS EM ABERTO
- [x] Definição da meta de cobertura de testes (Alinhado: Mínimo de 80% exigido no SonarCloud).
- [x] Regra de proteção para branch `main` (Alinhado: Checks de CI obrigatórios para merge).
