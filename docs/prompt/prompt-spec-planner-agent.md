# Prompt: Agente Spec Planner para Sistema Cognitivo

Voce e um arquiteto de software senior, especialista em planejamento tecnico, traduzindo especificacoes de negocio em decisoes de arquitetura e implementacao. Voce tambem e especialista em criar documentos de referencia otimizados para consumo por LLMs.

Sua tarefa e gerar o agente `agents/spec-planner/` dentro de `.claude/` no repositorio do projeto. Este agente fornece fundamentos de como transformar uma especificacao de software (spec) em um plano tecnico de implementacao (plan).

**IMPORTANTE:** Todos os arquivos gerados (AGENT.md, principles/, capabilities/, OUTPUT.md, EXAMPLES.md) devem ser escritos em **ingles**. Somente este prompt esta em portugues.

---

## Metodologia alvo

- **Planejamento tecnico no pipeline SDD:** O spec-planner recebe uma spec (O QUE construir) e produz um plan (COMO construir)
- O agente cobre: estrutura do plan, decisoes tecnicas, ordenacao de dependencias, constitution checks, traducao de entities em data model, derivacao de API contracts
- O planner NAO implementa — ele planeja. A implementacao e feita por outra ferramenta/agente

---

## Contexto do sistema cognitivo

Este agente faz parte de um sistema cognitivo com multiplos agentes. Existem tres tipos:
- **Agentes genericos (agnosticos):** Conhecimento puro reutilizavel entre projetos. Este agente (`spec-planner/`) e um deles, junto com `spec-writer/`, `ddd/` e `spring-kotlin/`.
- **Agentes especificos:** Aplicam o conhecimento generico ao dominio concreto de cada projeto.
- **Adapters:** Ponte entre agentes genericos e ferramentas especificas. A camada de adapter conecta o spec-planner aos outros agentes e ferramentas.

O agente `spec-planner/` e **generico e agnostico** — NAO deve conter referencias a nenhum dominio, tabela, enum, regra de negocio, framework, linguagem de programacao ou ferramenta SDD especifica. Ele sabe como planejar. A tecnologia vem de agentes de implementacao (ex: `spring-kotlin/`). O dominio vem dos agentes especificos e dos arquivos de contexto. A integracao com ferramentas SDD vem dos `adapters/`.

### Como se integra com os outros agentes

O spec-planner opera **depois** da spec ser aprovada e **antes** do task breakdown:

```
spec-writer (COMO escrever a spec)
  → domain-expert + architect + modeler + reviewer (dominio e validacao)
  → spec aprovada (docs/spec/MM-DD-YY-spec/spec.md)
  → spec-planner (COMO construir — traduz spec em plan tecnico)
    → output: docs/plan/MM-DD-YY-plan/ (diretorio com plan.md + artefatos)
  → spec-tasker (EM QUE ORDEM — quebra plan em tasks)
    → output: docs/task/MM-DD-YY-task/ (diretorio com task.md)
  → implement (executa tasks)
```

O spec-planner NAO toma decisoes de dominio — essas ja foram tomadas na spec. Ele traduz requisitos de negocio em decisoes tecnicas consultando os agentes que sabem sobre a tecnologia alvo.

### O que o spec-planner NAO sabe (e busca em outros agentes via adapter)

O spec-planner sabe a **mecanica de planejar** mas NAO sabe o conteudo. Ele precisa consultar:

- **Agentes de modelagem** (ex: `agents/ddd/`) — como estruturar bounded contexts, agregados, camadas
- **Agentes de implementacao** (ex: `agents/spring-kotlin/`) — como implementar em uma stack especifica (entities, repositories, services, controllers, testes, migrations)
- **Agentes especificos do projeto** (ex: `agents/architect/`, `agents/modeler/`) — constraints e regras do projeto concreto
- **Context do projeto** (ex: `context/`) — dados do dominio
- **Memory** (ex: `memory/decisions.md`) — decisoes ja tomadas

O adapter e quem define QUAIS agentes consultar. O spec-planner so define os PONTOS onde consulta e necessaria.

### Atualizacoes necessarias apos geracao

Apos gerar este agente, os seguintes arquivos precisam ser atualizados (NAO por este prompt — separadamente):

1. `adapters/spec-kit-constitution.md` — adicionar Planning Workflow com referencias ao spec-planner e aos agentes que ele consulta
2. `docs/prompt/prompt-cognitive-system.md` — referenciar o agente spec-planner (mesmo padrao usado para spring-kotlin e spec-writer)

---

## Objetivo

Criar o agente `agents/spec-planner/` com:
- Principios de planejamento tecnico no pipeline SDD
- Capacidades para cada etapa do processo de planejamento
- Pontos de consulta a outros agentes claramente definidos (sem hardcodar quais agentes)
- Formato de saida padrao
- Exemplos minimalistas (mecanica, nao dominio)

O agente sera consumido durante a fase de planejamento dos workflows SDD para garantir que toda spec seja traduzida num plan tecnico completo, com decisoes documentadas, estrutura de projeto definida e artefatos de design gerados.

---

## Restricoes

- **NAO** incluir referencias a nenhum dominio especifico (tabelas, enums, regras de negocio de qualquer projeto)
- **NAO** incluir referencias a nenhuma ferramenta SDD especifica (o adapter cuida disso)
- **NAO** incluir referencias a nenhum framework ou linguagem de programacao (o agente de implementacao cuida disso)
- **NAO** tomar decisoes de dominio — essas vem da spec e dos agentes de dominio
- **NAO** incluir Co-Authored-By ou qualquer atribuicao ao Claude/IA
- Todos os exemplos devem ser **minimalistas** — ilustrar a mecanica, nao construir um dominio
- Todas as saidas devem ser em **Markdown estruturado**, nao JSON
- Todos os arquivos gerados devem ser escritos em **ingles**
- Definir **pontos de consulta** onde outros agentes devem ser consultados, sem hardcodar quais agentes (ex: "consult the implementation agent for project structure" em vez de "consult agents/spring-kotlin/principles/spring-boot.md")

---

## Estrutura de arquivos a gerar

```
.claude/agents/spec-planner/
  AGENT.md
  principles/
    plan-structure.md
    technical-decisions.md
    dependency-ordering.md
    constitution-checks.md
    data-model-translation.md
    api-contracts.md
  capabilities/
    create-plan.md
    define-project-structure.md
    define-data-model.md
    define-api-contracts.md
    validate-plan.md
  OUTPUT.md
  EXAMPLES.md
```

**Total:** 14 arquivos.

---

## Conteudo detalhado de cada arquivo

### AGENT.md

Deve descrever:
- O que e este agente e qual seu proposito (traduzir specs em plans tecnicos)
- Que ele e generico e agnostico (nao sabe nada de nenhum dominio, linguagem ou ferramenta)
- Sua posicao no pipeline SDD (apos spec aprovada, antes de task breakdown)
- Como se relaciona com outros agentes (define pontos de consulta, adapter resolve quais agentes)
- Indice de todos os principles e capabilities disponiveis
- Que o input e uma spec aprovada e o output e um plan com artefatos

---

### principles/plan-structure.md

Como estruturar um plan tecnico. Deve cobrir:

- **Formato do plan:** Documento com secoes obrigatorias que traduz "o que construir" (spec) em "como construir" (plan)
- **Secoes obrigatorias:**
  - **Summary:** Extraido da spec — requisito primario + abordagem tecnica
  - **Technical Context:** Campos que descrevem a stack e restricoes tecnicas. Campos obrigatorios: linguagem/versao, dependencias principais, storage, framework de testes, plataforma alvo, tipo de projeto, metas de performance, constraints, escala/escopo. Campos nao conhecidos devem ser marcados com [NEEDS RESEARCH]
  - **Constitution Check:** Gates que devem passar antes de implementar (simplicidade, anti-abstracao, testes primeiro). Se houver violacoes, justificar no Complexity Tracking
  - **Project Structure:** Layout concreto de pastas e arquivos do projeto
  - **Complexity Tracking:** Tabela preenchida SOMENTE se houver violacoes constitucionais. Colunas: violacao, por que necessaria, alternativa mais simples rejeitada porque
- **Estrutura de output:** O plan e seus artefatos sao salvos num diretorio com o padrao `docs/plan/MM-DD-YY-plan/`:
  ```
  docs/plan/MM-DD-YY-plan/
    plan.md              — o plan principal
    data-model.md        — entidades, atributos, relacionamentos traduzidos da spec
    research.md          — pesquisa tecnica, decisoes e rationale (quando ha [NEEDS RESEARCH])
    quickstart.md        — cenarios de validacao rapida para verificar o setup
    contracts/           — especificacoes de API derivadas das user stories
  ```
- **Input:** O plan recebe como input a spec aprovada em `docs/spec/MM-DD-YY-spec/spec.md`
- **O plan referencia a spec:** Sempre incluir link/referencia ao documento de spec que originou o plan
- **O plan nao substitui a spec:** A spec continua sendo a fonte de verdade do O QUE. O plan define o COMO.

---

### principles/technical-decisions.md

Como documentar decisoes tecnicas. Deve cobrir:

- **Formato de decisao:** Cada decisao deve conter: decisao, alternativas consideradas, trade-offs, rationale (por que essa e nao as outras)
- **Quando documentar:** Toda escolha tecnica que afeta a arquitetura, performance, seguranca ou manutencao
- **Consulta a agentes:** Indicar que decisoes tecnicas devem ser tomadas consultando o agente de implementacao (que sabe sobre a stack) e o agente de modelagem (que sabe sobre DDD). O planner NAO inventa decisoes — ele documenta as decisoes que emergem dos agentes consultados.
- **Decisoes ja tomadas:** Antes de tomar uma decisao, verificar se ela ja foi tomada em `memory/decisions.md`. Nao duplicar.
- **Novas decisoes:** Decisoes novas tomadas durante o planejamento devem ser registradas em `memory/decisions.md` alem de no plan.

---

### principles/dependency-ordering.md

Como identificar e ordenar dependencias. Deve cobrir:

- **Principio de dependencia:** Construir de dentro para fora — o nucleo (dominio/entities) primeiro, depois as camadas externas (services, controllers, API)
- **Ordem generica de camadas:** Modelo de dados → logica de negocio → interfaces (API) → testes de integracao
- **Dependencias entre bounded contexts:** Identificar quais contextos dependem de quais. Contextos independentes podem ser construidos em paralelo.
- **Consulta a agentes:** A ordem exata das camadas depende da stack (consultar agente de implementacao) e da modelagem (consultar agente de modelagem). O planner define a mecanica de ordenacao, nao a ordem especifica.
- **Foundational vs feature:** Separar o que e infra/base (deve ser construido primeiro e bloqueia tudo) do que e feature (pode ser construido apos a base)

---

### principles/constitution-checks.md

Como validar o plan contra principios constitucionais do projeto. Deve cobrir:

- **O que e uma constitution:** Conjunto de principios imutaveis que governam o projeto. O plan deve respeitar esses principios.
- **Gates (pontos de validacao):** Cada projeto define seus proprios gates na constitution. O planner nao sabe quais gates existem — ele sabe que gates existem e como aplica-los. Exemplos comuns de gates (nao exaustivo, depende do projeto):
  - Simplicidade (quantidade minima de modulos, sem future-proofing)
  - Anti-abstracao (usar framework diretamente, sem wrappers desnecessarios)
  - Testes primeiro (prever testes antes da implementacao)
  - Integracao real (usar servicos reais nos testes, nao mocks)
- **Como aplicar gates:** Ler a constitution do projeto (fornecida pelo adapter), verificar cada gate contra o plan, documentar resultado (pass/fail com justificativa)
- **Se um gate falha:** Documentar a violacao no Complexity Tracking com justificativa
- **Constitution e project-specific:** A constitution real vem do adapter. O planner define a mecanica de validacao, nao os gates em si.

---

### principles/data-model-translation.md

Como traduzir key entities da spec em modelo de dados concreto. Deve cobrir:

- **Input:** Key entities definidas na spec (nome, atributos chave, relacionamentos descritos em linguagem natural)
- **Output:** `data-model.md` com entidades formais, campos tipados, constraints, relacionamentos explicitados
- **Consulta a agentes:** O formato exato do data model (tabelas SQL vs classes vs schemas) depende da stack — consultar agente de implementacao. A modelagem DDD (agregados, value objects, invariantes) — consultar agente de modelagem.
- **Processo:**
  1. Extrair entities da spec
  2. Consultar agente de modelagem para definir agregados e boundaries
  3. Consultar agente de implementacao para definir formato de persistencia
  4. Documentar relacionamentos (1:N, N:N, 1:1) com cardinalidade
  5. Documentar constraints (unique, not null, foreign keys)
  6. Documentar enums com todos os valores

---

### principles/api-contracts.md

Como derivar endpoints e schemas das user stories. Deve cobrir:

- **Input:** User stories com acceptance criteria da spec
- **Output:** `contracts/` com especificacoes de API (endpoints, metodos HTTP, request/response schemas, status codes)
- **Derivacao de endpoints:** Cada CRUD implica endpoints (GET list, GET by id, POST, PUT, DELETE). Cada acao de dominio pode implicar endpoints adicionais (PATCH status, POST approve, etc.)
- **Request/response schemas:** Derivados das key entities e dos acceptance criteria. Separar request (input) de response (output).
- **Status codes:** Derivados dos acceptance criteria — cada "Then returns XXX" define um status code
- **Documentacao:** Cada endpoint deve ter: metodo HTTP, path, descricao, request body schema, response schemas por status code
- **Consulta a agentes:** O formato exato das APIs (REST, GraphQL, gRPC) e o padrao de documentacao (OpenAPI, etc.) dependem da stack — consultar agente de implementacao.

---

### capabilities/create-plan.md

Guia passo a passo para gerar um plan completo a partir de uma spec. Deve incluir:

1. **Ler a spec aprovada** — entender todas as user stories, requirements, success criteria, edge cases, assumptions, out of scope
2. **Definir Technical Context** — preencher os 9 campos obrigatorios. Marcar [NEEDS RESEARCH] para o que nao e conhecido.
3. **Resolver [NEEDS RESEARCH]** — pesquisar e documentar em `research.md`
4. **Rodar constitution checks** — validar contra os gates. Documentar violacoes no Complexity Tracking.
5. **Definir project structure** — consultar agente de implementacao para layout de pastas (capability: define-project-structure)
6. **Definir data model** — traduzir key entities da spec, consultar agentes de modelagem e implementacao (capability: define-data-model)
7. **Definir API contracts** — derivar endpoints das user stories (capability: define-api-contracts)
8. **Gerar quickstart.md** — cenarios de validacao rapida derivados dos acceptance criteria mais criticos
9. **Registrar decisoes novas** — adicionar em `memory/decisions.md`
10. **Validar o plan** — rodar checklist de qualidade (capability: validate-plan)

Incluir o seguinte template da estrutura completa do plan:

```markdown
# Implementation Plan: [FEATURE NAME]

**Date:** [DATE]
**Spec:** [path to spec file, e.g., docs/spec/MM-DD-YY-spec/spec.md]
**Output directory:** [e.g., docs/plan/MM-DD-YY-plan/]
**Status:** complete

## Summary
[Primary requirement from spec + technical approach]

## Technical Context
- **Language/Version:** [or NEEDS RESEARCH]
- **Primary Dependencies:** [or NEEDS RESEARCH]
- **Storage:** [or N/A]
- **Testing:** [or NEEDS RESEARCH]
- **Target Platform:** [or NEEDS RESEARCH]
- **Project Type:** [library/cli/web-service/mobile-app]
- **Performance Goals:** [or NEEDS RESEARCH]
- **Constraints:** [or NEEDS RESEARCH]
- **Scale/Scope:** [or NEEDS RESEARCH]

## Constitution Check
[Gate results — pass or fail with justification]

## Project Structure
[Concrete folder/file layout — consult implementation agent]

## Complexity Tracking
[ONLY if constitution violations exist]
| Violation | Why Needed | Simpler Alternative Rejected Because |
```

---

### capabilities/define-project-structure.md

Como definir a estrutura de pastas e arquivos do projeto. Deve incluir:

1. **Ler o Technical Context** — tipo de projeto, plataforma, dependencias
2. **Consultar agente de implementacao** — solicitar o layout padrao para a stack
3. **Consultar agente de modelagem** — solicitar a estrutura de bounded contexts/modulos
4. **Combinar:** Layout tecnico da stack + organizacao por dominio
5. **Documentar:** Arvore de pastas com descricao de cada diretorio
6. **Incluir:** Pastas para testes (unit, integration, contract), configuracao, migrations, docs

---

### capabilities/define-data-model.md

Como traduzir key entities da spec em modelo de dados. Deve incluir:

1. **Extrair entities da spec** — cada Key Entity da secao Requirements
2. **Consultar agente de modelagem** — quais sao agregados, quais sao value objects, quais invariantes protegem
3. **Consultar agente de implementacao** — formato de persistencia (entity class, migration SQL, etc.)
4. **Para cada entity, documentar:** nome, campos com tipos e constraints, relacionamentos com cardinalidade, aggregate root (sim/nao), invariantes que protege
5. **Documentar enums** com todos os valores
6. **Gerar `data-model.md`** com todas as entities formalizadas

---

### capabilities/define-api-contracts.md

Como derivar API contracts das user stories. Deve incluir:

1. **Para cada user story,** identificar quais endpoints sao necessarios
2. **Para cada endpoint,** definir: metodo HTTP, path, descricao, request body, response por status code
3. **Derivar schemas** dos acceptance criteria — cada "Given/When/Then" define o que entra e o que sai
4. **Consultar agente de implementacao** — formato de documentacao de API (OpenAPI, etc.)
5. **Gerar `contracts/`** com arquivos de especificacao

---

### capabilities/validate-plan.md

Checklist de qualidade para validacao do plan. Deve incluir:

**Completude:**
- Todos os 9 campos do Technical Context preenchidos (nenhum [NEEDS RESEARCH] restante)
- Project structure definida com layout concreto
- Data model com todas as entities da spec
- API contracts com todos os endpoints derivados das stories
- Constitution checks executados

**Consistencia:**
- Plan nao contradiz a spec (nao adiciona nem remove requirements)
- Data model mapeia todas as key entities da spec
- API contracts cobrem todas as user stories
- Decisoes tecnicas nao violam constitution (ou violacoes justificadas)

**Rastreabilidade:**
- Plan referencia a spec de origem
- Cada decisao tecnica tem rationale documentado
- Novas decisoes registradas em memory/decisions.md

**Prontidao:**
- Plan tem informacao suficiente para gerar tasks (proxima fase)
- Nenhuma ambiguidade tecnica restante
- Ordem de dependencias e clara

Incluir regras de scoring (tudo passa = pronto, 1-3 falhas menores = corrigir, 4+ falhas = revisao maior).

---

### OUTPUT.md

Formato padrao de saida quando o agente spec-planner e referenciado durante geracao de plans. Deve incluir secoes para:

- **Plan section:** nome da secao do plan sendo gerada
- **Status:** draft | needs-research | complete
- **Content:** conteudo da secao
- **Consultation points:** quais agentes foram consultados e o que retornaram
- **Quality check:** checklist inline para a secao
- **Notes:** decisoes e trade-offs

Regras:
- Todas as saidas em Markdown — nunca JSON
- Headers de secao claros
- Consultation points explicitos (para rastreabilidade)
- Status field para rastrear completude

---

### EXAMPLES.md

Exemplos **minimalistas** que ilustram a mecanica de planejamento tecnico. NAO construir dominios completos. Usar features abstratas como "Create Item", "Process Order" para demonstrar padroes.

Incluir um exemplo curto para cada capability:
- Plan completo minimo (create-plan)
- Arvore de project structure (define-project-structure)
- Data model com 2-3 entities (define-data-model)
- API contracts com 2-3 endpoints (define-api-contracts)
- Checklist de validacao pass/fail (validate-plan)

Cada exemplo deve ter no maximo 15-25 linhas.

---

## Ordem de implementacao

Gerar os arquivos na seguinte ordem:

1. **AGENT.md** (primeiro — define o agente e indexa tudo)
2. **principles/** (segundo — conhecimento fundamental)
   - plan-structure.md
   - technical-decisions.md
   - dependency-ordering.md
   - constitution-checks.md
   - data-model-translation.md
   - api-contracts.md
3. **capabilities/** (terceiro — consomem os principios)
   - create-plan.md
   - define-project-structure.md
   - define-data-model.md
   - define-api-contracts.md
   - validate-plan.md
4. **OUTPUT.md** (quarto — formato de saida)
5. **EXAMPLES.md** (quinto — exemplos minimalistas)

---

## Base de conhecimento para geracao

O conteudo dos arquivos deve ser baseado em praticas estabelecidas de planejamento tecnico e arquitetura:

- Planejamento tecnico no pipeline SDD (traduzir spec em plan, plan em tasks)
- Documentacao de decisoes tecnicas (ADR-style: decisao, alternativas, rationale)
- Dependency ordering (inside-out: dominio primeiro, interfaces depois)
- Constitution/governance (gates de qualidade, simplicidade, anti-abstracao)
- Data model design (entities, relacionamentos, constraints, enums)
- API contract design (REST, endpoints, request/response, status codes)

Pontos criticos que devem ser refletidos nos principios:

### Agnosticismo
- O spec-planner NAO sabe a tecnologia — define pontos de consulta onde o agente de implementacao e consultado
- O spec-planner NAO sabe o dominio — define pontos de consulta onde o agente de modelagem e consultado
- O spec-planner sabe a MECANICA de planejar — como estruturar um plan, como documentar decisoes, como ordenar dependencias

### Rastreabilidade
- O plan sempre referencia a spec de origem
- Cada decisao tecnica tem rationale
- Pontos de consulta a outros agentes sao explicitados no output (para saber de onde veio cada decisao)

### Nao duplicar
- Se uma decisao ja esta em memory/decisions.md, referenciar em vez de redocumentar
- Se uma regra esta na constitution, referenciar em vez de reescrever
- Se um modelo esta na spec, traduzir em vez de reinventar

### Pipeline integrity
- O plan NAO contradiz a spec — traduz, nao muda
- O plan tem informacao suficiente para a proxima fase (task breakdown)
- Ambiguidades tecnicas sao resolvidas no plan (nao empurradas para tasks)

---

Agora gere o agente completo. Lembre-se: todos os arquivos gerados em **ingles**, somente este prompt esta em portugues.
