# ADR-010: Automação de CI/CD com GitHub Actions, Testes com Testcontainers, Trivy e Quality Gate SonarCloud

**Data:** 2026-09-01  
**Status:** Aceito  
**Autor:** Grupo CAO (POSTECH 15SOAT)  

---

## Contexto

A entrega contínua de software de alta qualidade em uma arquitetura de múltiplos repositórios exige que cada alteração de código ou infraestrutura seja validada de forma automatizada, determinística e rigorosa antes de qualquer deploy em ambientes compartilhados ou produtivos.

Desafios e requisitos de engenharia identificados:
1. **Falsos Positivos em Testes de Banco:** Testes de integração utilizando bancos em memória (como H2) frequentemente não reproduzem comportamentos, tipos de dados específicos (como UUID e JSONB), constraints ou concorrência transacional do PostgreSQL real.
2. **Qualidade Estática e Cobertura de Código:** Necessidade de garantir que nenhum código novo seja inserido na branch principal sem atingir o patamar mínimo de 80% de cobertura de testes automatizados e sem introduzir *code smells*, duplicações ou vulnerabilidades de segurança.
3. **Segurança de Imagens de Contêiner:** Detecção proativa de vulnerabilidades (*CVEs*) conhecidas no sistema operacional base e nas dependências de bibliotecas antes de publicar a imagem no Amazon ECR.
4. **Deploy Seguro e Controlado:** Evitar modificações não intencionais de infraestrutura durante a abertura de Pull Requests.

---

## Decisão

Implementar pipelines completas de **CI/CD automatizadas com GitHub Actions** integrando **Testcontainers** (para testes com PostgreSQL real), **JaCoCo** (coleta de cobertura), **SonarCloud** (Quality Gate estrito), escaneamento de contêineres com **Trivy**, *Smoke Tests* automáticos e portão de aprovação manual (*Approval Gate*) para deploys em produção.

### Detalhes Técnicos e Estágios da Pipeline:

1. **Paralelismo Inteligente:**
   - O provisionamento/planejamento de infraestrutura (`terraform plan`/`apply`) é executado em paralelo com o estágio de build e compilação da aplicação, reduzindo o tempo total da esteira em até 40%.
   - **Regra de Segurança para PRs:** O comando `terraform apply` e a publicação de imagens no ECR (`docker push`) são **completamente bloqueados em Pull Requests**. O PR apenas executa compilação, suíte de testes, análise estática e *dry-run* de segurança.

2. **Testes de Integração com Testcontainers:**
   - A suíte de testes automatizados sobe instâncias reais e efêmeras do PostgreSQL via contêineres Docker gerenciados pelo Testcontainers.
   - Garante 100% de fidelidade com o comportamento do banco de produção (PostgreSQL 16) sem necessidade de manter um banco de dados dedicado fixo para testes.

3. **Quality Gate Estrito no SonarCloud:**
   - O plugin **JaCoCo** gera relatórios de cobertura de código binários e XML durante a fase de `mvn verify`.
   - O **SonarCloud Scanner** analisa a base de código e bloqueia o merge caso o Quality Gate não seja atingido (exigência de cobertura mínima de 80% em código novo, 0 bugs e 0 vulnerabilidades de severidade alta/crítica).

4. **Escaneamento de Vulnerabilidades com Trivy e Smoke Test:**
   - Toda imagem Docker construída é submetida ao scanner de segurança **Trivy** para detecção de vulnerabilidades conhecidas no sistema operacional e bibliotecas.
   - **Smoke Test do Contêiner:** A esteira inicializa o contêiner gerado e consulta o endpoint `/actuator/health` via HTTP antes de autorizar o envio ao Amazon ECR.

5. **Deploy Contínuo e Rollout no EKS:**
   - A esteira atualiza dinamicamente as credenciais do Kubernetes e aplica os manifestos (`kubectl apply -f k8s/` e `kubectl apply -k k8s/observability/`), aguardando a conclusão do *rollout* (`kubectl rollout status`) com verificação automática de integridade.

---

## Consequências

### Positivas
- **Alta Confiabilidade nos Deploys:** Bugs e regressões são identificados antes que o código atinja o ambiente de homologação ou produção.
- **Fidelidade Transacional em Testes:** O uso de Testcontainers elimina divergências clássicas de comportamento entre bancos de memória (H2) e o banco real (Postgres).
- **Conformidade Contínua de Segurança:** Trivy e SonarCloud garantem que vulnerabilidades e dívidas técnicas sejam detectadas precocemente no ciclo de vida (*Shift-Left Security*).
- **Proteção do Ambiente de Produção:** Aprovações manuais e bloqueios em PRs impedem deploys acidentais.

### Negativas / Trade-offs
- **Tempo de Execução da Esteira:** A inicialização de contêineres do Testcontainers e a execução de análises de segurança acrescentam alguns minutos à execução do pipeline.
- **Dependência de Serviços Externos:** A esteira depende da disponibilidade da API do GitHub Actions e do SonarCloud.

### Riscos e Mitigações
- **Risco:** Lentidão no download de imagens e dependências Maven nos runners do GitHub Actions.
- **Mitigação:** Utilização de ações oficiais de cache (`actions/cache` e `actions/setup-java` com `cache: 'maven'`) para reter dependências entre execuções.
