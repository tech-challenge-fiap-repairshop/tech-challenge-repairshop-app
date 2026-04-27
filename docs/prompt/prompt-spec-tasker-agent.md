# Prompt: Agente Spec Tasker para Sistema Cognitivo

Voce e um engenheiro de software senior, especialista em quebrar planos tecnicos em tarefas atomicas, ordenadas e executaveis. Voce tambem e especialista em criar documentos de referencia otimizados para consumo por LLMs.

Sua tarefa e gerar o agente `agents/spec-tasker/` dentro de `.claude/` no repositorio do projeto. Este agente fornece fundamentos de como transformar um plano tecnico (plan) em uma lista de tarefas executaveis (tasks).

**IMPORTANTE:** Todos os arquivos gerados (AGENT.md, principles/, capabilities/, OUTPUT.md, EXAMPLES.md) devem ser escritos em **ingles**. Somente este prompt esta em portugues.

---

## Metodologia alvo

- **Task breakdown no pipeline SDD:** O spec-tasker recebe um plan (COMO construir) e produz tasks (EM QUE ORDEM construir)
- O agente cobre: atomicidade de tasks, grafo de dependencias, paralelizacao, rastreabilidade para a spec, criterios de conclusao, organizacao em fases
- O tasker NAO toma decisoes tecnicas — essas ja foram tomadas no plan. Ele quebra e ordena.

---

## Contexto do sistema cognitivo

Este agente faz parte de um sistema cognitivo com multiplos agentes. Existem tres tipos:
- **Agentes genericos (agnosticos):** Conhecimento puro reutilizavel entre projetos. Este agente (`spec-tasker/`) e um deles, junto com `spec-writer/`, `spec-planner/`, `ddd/` e `spring-kotlin/`.
- **Agentes especificos:** Aplicam o conhecimento generico ao dominio concreto de cada projeto.
- **Adapters:** Ponte entre agentes genericos e ferramentas especificas.

O agente `spec-tasker/` e **generico e agnostico** — NAO deve conter referencias a nenhum dominio, tabela, enum, regra de negocio, framework, linguagem de programacao ou ferramenta SDD especifica. Ele sabe como quebrar plans em tasks. Os tipos de artefatos (entity, repository, service, controller, test, etc.) vem de agentes de implementacao via adapter. O dominio vem dos agentes especificos.

### Como se integra com os outros agentes

O spec-tasker opera **depois** do plan ser aprovado e **antes** da implementacao:

```
spec-writer → domain-expert + architect + modeler + reviewer
  → spec aprovada (docs/spec/MM-DD-YY-spec/spec.md)
  → spec-planner
  → plan aprovado (docs/plan/MM-DD-YY-plan/)
  → spec-tasker (EM QUE ORDEM — quebra plan em tasks)
    → output: docs/task/MM-DD-YY-task/task.md
  → implement (executa tasks)
```

O spec-tasker NAO toma decisoes tecnicas — essas ja estao no plan. Ele sabe a **mecanica de quebrar e ordenar**, nao o conteudo.

### O que o spec-tasker NAO sabe (e busca em outros agentes via adapter)

- **Tipos de artefatos** (entity, repository, service, controller, test, migration, dto, enum, config) — vem do agente de implementacao (ex: `spring-kotlin/capabilities/`). Cada capability e naturalmente um tipo de task.
- **Ordem das camadas** (domain → application → infrastructure → presentation) — vem do agente de modelagem (ex: `ddd/principles/ddd.md`)
- **Quais bounded contexts sao independentes** — vem do agente de arquitetura (ex: `architect/CONSTRAINTS.md`)
- **Quais agregados e dependencias existem** — vem do agente de modelagem (ex: `modeler/RULES.md`)

O adapter define QUAIS agentes consultar. O spec-tasker so define os PONTOS onde consulta e necessaria.

### Atualizacoes necessarias apos geracao

Apos gerar este agente, os seguintes arquivos precisam ser atualizados (NAO por este prompt — separadamente):

1. `adapters/spec-kit-constitution.md` — adicionar Task Breakdown Workflow com referencias ao spec-tasker e aos agentes que ele consulta
2. `docs/prompt/prompt-cognitive-system.md` — referenciar o agente spec-tasker (mesmo padrao usado para os outros agentes)
3. `CLAUDE.md` — adicionar o agente na tabela de agentes disponiveis

---

## Objetivo

Criar o agente `agents/spec-tasker/` com:
- Principios de task breakdown no pipeline SDD
- Capacidades para cada etapa do processo de quebra em tasks
- Pontos de consulta a outros agentes claramente definidos (sem hardcodar quais agentes)
- Formato de saida padrao
- Exemplos minimalistas (mecanica, nao dominio)

O agente sera consumido durante a fase de task breakdown dos workflows SDD para garantir que todo plan seja traduzido numa lista de tasks atomicas, ordenadas, rastreavels e prontas para execucao.

---

## Restricoes

- **NAO** incluir referencias a nenhum dominio especifico (tabelas, enums, regras de negocio de qualquer projeto)
- **NAO** incluir referencias a nenhuma ferramenta SDD especifica (o adapter cuida disso)
- **NAO** incluir referencias a nenhum framework ou linguagem de programacao (o agente de implementacao cuida disso)
- **NAO** tomar decisoes tecnicas — essas ja estao no plan
- **NAO** incluir Co-Authored-By ou qualquer atribuicao ao Claude/IA
- Todos os exemplos devem ser **minimalistas** — ilustrar a mecanica, nao construir um dominio
- Todas as saidas devem ser em **Markdown estruturado**, nao JSON
- Todos os arquivos gerados devem ser escritos em **ingles**
- Definir **pontos de consulta** onde outros agentes devem ser consultados, sem hardcodar quais agentes

---

## Estrutura de arquivos a gerar

```
.claude/agents/spec-tasker/
  AGENT.md
  principles/
    task-atomicity.md
    dependency-graph.md
    parallelization.md
    task-traceability.md
    completion-criteria.md
    phasing.md
  capabilities/
    create-tasks.md
    order-tasks.md
    identify-parallel.md
    map-to-stories.md
    validate-tasks.md
  OUTPUT.md
  EXAMPLES.md
```

**Total:** 14 arquivos.

---

## Conteudo detalhado de cada arquivo

### AGENT.md

Deve descrever:
- O que e este agente e qual seu proposito (quebrar plans em tasks executaveis)
- Que ele e generico e agnostico (nao sabe nada de nenhum dominio, linguagem ou ferramenta)
- Sua posicao no pipeline SDD (apos plan aprovado, antes de implementacao)
- Como se relaciona com outros agentes (consulta agente de implementacao para tipos de artefatos, agente de modelagem para ordem de camadas)
- Indice de todos os principles e capabilities disponiveis
- Que o input e um plan aprovado (em `docs/plan/MM-DD-YY-plan/plan.md` com seus artefatos: data-model.md, contracts/, research.md) e o output e uma lista de tasks (em `docs/task/MM-DD-YY-task/task.md`)

---

### principles/task-atomicity.md

O que e uma task atomica. Deve cobrir:

- **Definicao:** Uma task e uma unidade de trabalho que produz exatamente um artefato (um arquivo, uma migration, uma configuracao). Nao misturar artefatos.
- **Tamanho ideal:** Uma task deve ser pequena o suficiente para ser concluida em uma sessao de trabalho, mas grande o suficiente para ser significativa
- **Um artefato por task:** "Criar entity Customer" e uma task. "Criar entity Customer e CustomerRepository" sao duas tasks.
- **Independencia logica:** Cada task deve fazer sentido isoladamente — se voce ler so a descricao da task, deve entender o que precisa ser feito
- **Commitavel:** Cada task (ou grupo pequeno de tasks relacionadas) deve resultar em codigo que compila e nao quebra o que ja existe

---

### principles/dependency-graph.md

Como identificar e ordenar dependencias entre tasks. Deve cobrir:

- **Dependencia direta:** Task B depende de Task A se B precisa do artefato que A produz (ex: repository depende da entity existir). Expressa no formato `(depends on T001, T003)` na descricao da task.
- **Dependencia transitiva:** Se A → B → C, entao C depende transitivamente de A. NAO listar dependencias transitivas — listar apenas as diretas.
- **Ordem generica dentro de um bounded context:** Consultar agente de implementacao para saber a ordem padrao de artefatos. Consultar agente de modelagem para saber a ordem de camadas (domain → application → infrastructure → presentation)
- **Dependencia entre bounded contexts:** Consultar agente de arquitetura para saber quais contextos dependem de quais. Contextos independentes podem ter tasks em paralelo.
- **Dependencia implicita:** Tasks sequenciais (sem [P]) dentro da mesma fase dependem implicitamente da task anterior. Mesmo assim, documentar dependencias explicitas quando a relacao nao e obvia.
- **Ciclos:** Nao devem existir ciclos de dependencia. Se encontrar, e um sinal de design errado no plan.

---

### principles/parallelization.md

Como identificar tasks que podem ser executadas em paralelo. Deve cobrir:

- **Marcador [P]:** Tasks paralelizaveis sao marcadas com `[P]` no ID
- **Criterio para [P]:** A task afeta arquivos DIFERENTES e nao tem dependencia bloqueante em tasks incompletas
- **Paralelismo entre contexts:** Bounded contexts independentes podem ser trabalhados em paralelo apos a fase foundational
- **Paralelismo dentro de um context:** Tasks que afetam arquivos diferentes dentro do mesmo context podem ser paralelas (ex: criar duas entities independentes)
- **Nao paralelizavel:** Tasks no mesmo arquivo, tasks com dependencia direta, tasks de configuracao global

---

### principles/task-traceability.md

Como rastrear cada task de volta para a spec e o plan. Deve cobrir:

- **Cada task deve referenciar:** Qual user story (US-N), qual acceptance criteria (AC-N) ou qual functional requirement (FR-XXX) ela satisfaz
- **Marcador [Story]:** Tasks que pertencem a uma user story sao marcadas com `[US-N]` no ID
- **Tasks sem story:** Tasks de setup, foundational e polish nao tem story — sao infra compartilhada
- **Cobertura completa:** Ao final, toda user story da spec deve ter pelo menos uma task que a satisfaz. Se uma story nao tem tasks, algo foi esquecido.
- **Verificacao inversa:** Toda task deve apontar para pelo menos um item da spec (story, AC ou FR). Tasks "orfas" sem rastreabilidade devem ser justificadas.

---

### principles/completion-criteria.md

Como definir quando uma task esta concluida. Deve cobrir:

- **Definicao de pronto:** Cada task deve ter um criterio claro e verificavel de conclusao
- **Tipos de criterio por artefato:** Consultar agente de implementacao para criterios especificos por tipo de artefato (ex: entity — compila e testes passam; migration — aplica sem erro; test — roda e falha antes de implementacao)
- **Verificacao automatica:** Preferencialmente, o criterio e automatizavel (teste passa, build compila, migration aplica)
- **Marcacao:** Quando concluida, a task e marcada alterando `- [ ]` para `- [X]` (uppercase X). A linha permanece inalterada exceto pelo checkbox.

---

### principles/phasing.md

Como organizar tasks em fases. Deve cobrir:

- **Phase 1 — Setup (infraestrutura compartilhada):**
  - Dependencias do projeto, configuracoes globais
  - Nao pertence a nenhuma user story
  - Bloqueia todas as fases seguintes

- **Phase 2 — Foundational (pre-requisitos bloqueantes):**
  - Schema de banco (migrations), framework de autenticacao, middleware, models base, error handling, configuracao de ambiente
  - CRITICO: Nenhum trabalho de user story pode comecar ate esta fase estar completa
  - Nao pertence a nenhuma user story

- **Phase 3+ — User Stories (uma fase por story, ordenadas por prioridade P1 → P2 → P3):**
  - Cada story tem sua propria fase
  - Dentro de cada fase, a ordem e: testes (se incluidos, devem FALHAR) → models [P] → services → endpoints → integracao → validacao/logging
  - Consultar agente de implementacao para a ordem de artefatos especifica da stack
  - Stories podem ser executadas em paralelo apos Phase 2 (se sao de bounded contexts independentes)
  - Cada story e independentemente testavel e completavel
  - Cada fase termina com um **Checkpoint** descrevendo o estado esperado

- **Final Phase — Polish (cross-cutting concerns):**
  - Documentacao, cleanup, otimizacao de performance, testes adicionais, seguranca
  - Depende de todas as stories estarem completas

---

### capabilities/create-tasks.md

Guia passo a passo para gerar a lista completa de tasks a partir de um plan. Deve incluir:

1. **Ler o plan aprovado** — entender project structure, data model, API contracts, dependency ordering, phases. Tambem ler a **spec original** referenciada pelo plan — e la que estao as user stories, acceptance criteria e functional requirements que as tasks devem rastrear.
2. **Identificar artefatos de setup** — dependencias, configuracoes globais → Phase 1
3. **Identificar artefatos foundational** — migrations, entities base, auth framework, error handling → Phase 2
4. **Para cada user story (P1 primeiro, depois P2, P3):**
   - Consultar agente de implementacao para identificar tipos de artefatos necessarios
   - Gerar uma task por artefato
   - Marcar com `[US-N]`
   - Referenciar ACs e FRs satisfeitos
5. **Identificar artefatos de polish** — docs, cleanup, testes extras → Final Phase
6. **Ordenar tasks** por dependencia (capability: order-tasks)
7. **Marcar tasks paralelas** com `[P]` (capability: identify-parallel)
8. **Mapear tasks para stories** (capability: map-to-stories)
9. **Validar a lista** (capability: validate-tasks)

Incluir o seguinte template da estrutura completa de tasks:

```markdown
# Task Breakdown: [FEATURE NAME]

**Date:** [DATE]
**Plan:** [path to plan file]
**Spec:** [path to spec file]
**Output directory:** [e.g., docs/task/MM-DD-YY-task/]
**Status:** complete

## Phase 1: Setup

- [ ] T001 [description with file path]
- [ ] T002 [P] [description with file path]

**Checkpoint**: Project structure created, dependencies installed, builds successfully.

## Phase 2: Foundational

- [ ] T003 [description with file path]
- [ ] T004 [P] [description with file path]
- [ ] T005 [description with file path] (depends on T003)

**Checkpoint**: Database schema applied, auth framework ready, error handling in place. All foundational infrastructure complete — user story work can begin.

## Phase 3: [User Story 1 Title] (P1)

- [ ] T006 [P] [US1] Create [Entity] model in [file path] — satisfies FR-001
- [ ] T007 [P] [US1] Create [Entity2] model in [file path] — satisfies FR-002
- [ ] T008 [US1] Implement [Service] in [file path] (depends on T006, T007) — satisfies AC-1, AC-2
- [ ] T009 [US1] Implement [Controller] in [file path] (depends on T008) — satisfies AC-3, AC-4, AC-5

**Checkpoint**: User Story 1 fully functional and independently testable.

## Phase 4: [User Story 2 Title] (P1)

- [ ] T010 [P] [US2] Create [Entity] model in [file path] — satisfies FR-004
- [ ] T011 [US2] Implement [Service] in [file path] (depends on T010) — satisfies AC-6

**Checkpoint**: User Story 2 fully functional and independently testable.

...

## Final Phase: Polish

- [ ] T050 [P] Documentation updates in [file path]
- [ ] T051 Code cleanup and final review

**Checkpoint**: All stories complete, documentation updated, ready for delivery.
```

**Formato de cada task:**
```
- [ ] T{NNN} [P?] [US-N?] Description with exact file path (depends on T{X}, T{Y}?) — satisfies AC-X, FR-XXX
```

- Checkbox `- [ ]` obrigatorio (marcada como `- [X]` quando concluida — uppercase X)
- ID sequencial `T001`, `T002`, `T003` ... em ordem de execucao. Formato: T + 3 digitos minimo, expandindo se necessario (T001...T999, T1000+). IDs unicos, sem pulos.
- `[P]` somente se paralelizavel (arquivos diferentes E sem dependencias bloqueantes)
- `[US-N]` somente se pertence a uma user story (NUNCA em Setup, Foundational ou Polish)
- Descricao com path **exato** do arquivo a ser criado/modificado (obrigatorio — ex: `src/main/kotlin/com/example/customer/Customer.kt`)
- Dependencias explicitas quando existirem: `(depends on T003, T005)` — no final da descricao, antes do "satisfies"
- Referencia a quais ACs e FRs a task satisfaz

**Checkpoints apos cada fase:**
```
**Checkpoint**: [descricao do que deve estar funcionando ao final desta fase]
```
Cada fase deve terminar com um checkpoint que descreve o estado esperado do projeto. O implementador valida o checkpoint antes de avancar para a proxima fase.

**Tratamento de TDD (quando testes sao incluidos):**
- Tasks de teste aparecem ANTES das tasks de implementacao dentro de cada user story
- Testes devem ser escritos e devem FALHAR antes da implementacao (Red phase)
- Apos implementacao, testes devem PASSAR (Green phase)
- Ordem dentro de cada story: testes [P] → models [P] → services → endpoints → integracao

**Tratamento de falhas:**
- Task sequencial falha → halt, reportar erro, pedir decisao ao usuario
- Task paralela [P] falha → continuar com outras [P], reportar falha ao final do grupo
- Task dependente de task falha → pular automaticamente, reportar como skipped
- Task de Phase 2 (Foundational) falha → HALT total (bloqueia tudo)

---

### capabilities/order-tasks.md

Como ordenar tasks respeitando dependencias. Deve incluir:

1. **Construir o grafo de dependencias** — para cada task, listar quais tasks devem estar completas antes
2. **Ordenacao topologica** — ordenar tasks de forma que nenhuma task apareca antes de suas dependencias
3. **Dentro de cada fase,** respeitar a ordem de camadas do agente de modelagem (domain → application → infrastructure → presentation)
4. **Dentro de cada camada,** respeitar a ordem de artefatos do agente de implementacao
5. **Entre fases,** respeitar a regra: Setup → Foundational → User Stories → Polish
6. **Verificar ciclos** — se encontrar dependencia circular, reportar como erro de design

---

### capabilities/identify-parallel.md

Como marcar tasks paralelizaveis. Deve incluir:

1. **Para cada task,** verificar se ela afeta arquivos diferentes de todas as outras tasks na mesma fase
2. **Verificar dependencias** — a task nao tem dependencia bloqueante em tasks incompletas na mesma fase?
3. **Se ambos criterios passam,** marcar com `[P]`
4. **Bounded contexts independentes** — tasks de contextos diferentes na mesma fase sao paralelizaveis (consultar agente de arquitetura)
5. **Nunca marcar como [P]:** tasks que modificam o mesmo arquivo, tasks com dependencia direta na mesma fase

---

### capabilities/map-to-stories.md

Como vincular tasks a user stories e acceptance criteria. Deve incluir:

1. **Para cada user story na spec,** listar todos os artefatos necessarios
2. **Para cada artefato,** encontrar a task correspondente
3. **Anotar a task** com `[US-N]` e `satisfies AC-X, FR-XXX`
4. **Verificar cobertura** — toda story tem pelo menos uma task? Todo AC e satisfeito por pelo menos uma task?
5. **Tasks compartilhadas** — se uma task serve multiplas stories (ex: migration que cria tabelas para 2 contextos), anotar com a story de prioridade mais alta

---

### capabilities/validate-tasks.md

Checklist de qualidade para validacao da lista de tasks. Deve incluir:

**Completude:**
- Toda user story da spec tem pelo menos uma task
- Todo AC da spec e satisfeito por pelo menos uma task
- Todo FR da spec e satisfeito por pelo menos uma task
- Todo artefato do plan (project structure) tem uma task que o cria

**Ordenacao:**
- Nenhuma task aparece antes de suas dependencias
- Nenhum ciclo de dependencia
- Fases respeitam a ordem: Setup → Foundational → Stories → Polish

**Formato:**
- Toda task tem checkbox `- [ ]`
- Toda task tem ID sequencial (T001, T002, ...)
- Toda task de story tem `[US-N]`
- Tasks paralelas marcadas com `[P]`
- Toda task tem descricao com file path

**Rastreabilidade:**
- Toda task de story referencia ACs e/ou FRs
- Nenhuma task orfã (sem referencia a spec) — exceto Setup, Foundational e Polish

Incluir regras de scoring (tudo passa = pronto, 1-3 falhas menores = corrigir, 4+ falhas = revisao maior).

---

### OUTPUT.md

Formato padrao de saida quando o agente spec-tasker e referenciado durante geracao de tasks. Deve incluir secoes para:

- **Phase:** nome da fase sendo gerada
- **Status:** draft | complete
- **Tasks:** lista de tasks no formato padrao
- **Dependencies:** grafo de dependencias para a fase
- **Coverage check:** quais stories/ACs sao cobertas por esta fase
- **Notes:** observacoes sobre ordenacao ou paralelizacao

Regras:
- Todas as saidas em Markdown — nunca JSON
- Checkbox format obrigatorio
- IDs sequenciais
- File paths exatos em cada task

---

### EXAMPLES.md

Exemplos **minimalistas** que ilustram a mecanica de task breakdown. NAO construir dominios completos. Usar features abstratas como "Create Item", "Process Order" para demonstrar padroes.

Incluir um exemplo curto para cada capability:
- Lista de tasks com fases (create-tasks)
- Grafo de dependencias (order-tasks)
- Marcacao de tasks paralelas (identify-parallel)
- Mapeamento task → story (map-to-stories)
- Checklist de validacao pass/fail (validate-tasks)

Cada exemplo deve ter no maximo 15-25 linhas.

---

## Ordem de implementacao

Gerar os arquivos na seguinte ordem:

1. **AGENT.md** (primeiro — define o agente e indexa tudo)
2. **principles/** (segundo — conhecimento fundamental)
   - task-atomicity.md
   - dependency-graph.md
   - parallelization.md
   - task-traceability.md
   - completion-criteria.md
   - phasing.md
3. **capabilities/** (terceiro — consomem os principios)
   - create-tasks.md
   - order-tasks.md
   - identify-parallel.md
   - map-to-stories.md
   - validate-tasks.md
4. **OUTPUT.md** (quarto — formato de saida)
5. **EXAMPLES.md** (quinto — exemplos minimalistas)

---

## Base de conhecimento para geracao

O conteudo dos arquivos deve ser baseado em praticas estabelecidas de planejamento de tarefas e gerenciamento de projetos:

- Task breakdown no pipeline SDD (plan → tasks → implement)
- Grafos de dependencia e ordenacao topologica
- Paralelizacao de trabalho independente
- Rastreabilidade de requisitos (task → story → acceptance criteria)
- Criterios de conclusao verificaveis
- Phasing de projetos (setup, foundational, features, polish)

Pontos criticos que devem ser refletidos nos principios:

### Agnosticismo
- O spec-tasker NAO sabe a tecnologia — consulta o agente de implementacao para tipos de artefatos e ordem de criacao
- O spec-tasker NAO sabe o dominio — consulta o agente de modelagem para camadas e agregados
- O spec-tasker sabe a MECANICA de quebrar e ordenar — como fazer tasks atomicas, como identificar dependencias, como marcar [P]

### Rastreabilidade
- Toda task referencia ACs e FRs da spec
- Toda story tem cobertura completa de tasks
- Tasks orfas sao justificadas ou removidas

### Ordem importa
- Phase 2 (Foundational) bloqueia tudo — nenhum trabalho de story antes da infra estar pronta
- Dentro de cada story, a ordem segue domain → application → infrastructure → presentation
- Stories podem ser paralelas se sao de bounded contexts independentes

### Atomicidade
- Uma task = um artefato = um file path
- Tasks sao commitaveis — o projeto compila apos cada task
- Nao misturar artefatos de camadas diferentes na mesma task

---

Agora gere o agente completo. Lembre-se: todos os arquivos gerados em **ingles**, somente este prompt esta em portugues.
