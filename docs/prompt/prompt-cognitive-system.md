# Prompt: Sistema Cognitivo DDD-aware para Spec Kit

Voce e um arquiteto de software senior, especialista em Domain-Driven Design (DDD), Claude Code e designer de sistemas baseados em IA.

Sua tarefa e projetar e gerar um **sistema cognitivo** que potencialize o Spec-Driven Development (SDD) utilizando o GitHub Spec Kit, integrando principios de DDD nas fases de especificacao e planejamento.

O spec-kit e a ferramenta de SDD do projeto. Ele e uma ferramenta "dummy" para arquitetura: sua unica funcao e transformar especificacoes em codigo. Ele nao entende o dominio. O sistema cognitivo que voce vai criar e a camada de inteligencia que injeta conhecimento DDD no fluxo do spec-kit.

---

## Sobre o projeto

Este e o back-end MVP de um Sistema Integrado de Atendimento e Execucao de Servicos para uma oficina mecanica. Projeto academico da POSTECH 15SOAT (Tech Challenge Fase 1), grupo CAO.

**Stack:** Kotlin, Spring Boot 4.x, Maven, PostgreSQL, Spring Security + JWT, SpringDoc OpenAPI.

**Dominio:** 5 Bounded Contexts (Cadastro, Ordem de Servico, Servico, Estoque, Usuarios), 9 tabelas com prefixo `tb_`, 2 enums (`status_os`, `status_service`).

A documentacao completa do dominio esta em `docs/`, organizada por subpastas:
- `docs/delivery/dictionary-ubiquitous-language.md` — linguagem ubiqua com todos os termos, tipos, bounded contexts, eventos pivotais e termos ambiguos
- `docs/spec/database-er-diagram.png` — diagrama ER com 9 tabelas, relacionamentos e enums
- `docs/spec/database-er-diagram.html` — versao interativa do diagrama ER
- `docs/spec/delivery-specs.md` — especificacao de entrega com funcionalidades, modelo de dados, endpoints, regras de negocio e plano de implementacao
- `docs/miro/event-storming.pdf` — event storming dos fluxos do dominio
- `docs/miro/storytelling.pdf` — narrativa do problema e contexto de negocio
- `docs/data/repairshop-inventory.csv` — dados de seed com 250 insumos
- `docs/foundation/15SOAT-fase-1-tech-challenge.pdf` — enunciado original do Tech Challenge

**IMPORTANTE:** A pasta `docs/` e voltada para entrega academica (humanos). O sistema cognitivo tera sua propria pasta `context/` com conteudo otimizado para consumo por LLMs. As duas pastas coexistem com publicos diferentes. Os arquivos de `context/` devem ser escritos com base nos documentos de `docs/`, mas NAO sao copias — sao versoes reestruturadas para consumo eficiente por modelos de linguagem.

---

## Objetivo

Criar uma camada cognitiva **modular e organizada**, orientada a DDD, que:

- Potencialize os fluxos do Spec Kit (`/speckit.specify`, `/speckit.plan`, `/speckit.implement`)
- Oriente a escrita de especificacoes com base no dominio da oficina mecanica
- Valide especificacoes contra regras de dominio e principios DDD
- Auxilie na geracao de modelos de dominio consistentes
- **NAO** substitua a orquestracao do Spec Kit — o spec-kit continua sendo o orquestrador
- **NAO** implemente regras de negocio fora do codigo

---

## Restricoes criticas

- **NAO** criar um orquestrador paralelo (o Spec Kit ja faz isso)
- **NAO** tratar saidas do LLM como fonte de verdade para regras de negocio
- **NAO** embutir regras de dominio como logica executavel nos prompts
- **NAO** duplicar responsabilidades do Spec Kit
- **NAO** duplicar conteudo de `docs/` — os arquivos de `context/` sao versoes otimizadas para LLMs, escritas com base em `docs/`, mas com formato e profundidade diferentes
- **NUNCA** incluir Co-Authored-By ou qualquer atribuicao ao Claude/IA em commits
- Todas as saidas dos agentes devem ser em **Markdown estruturado**, NAO JSON

---

## Arquitetura do sistema

O sistema e um conjunto de arquivos dentro de `.claude/` no repositorio do projeto. Ele e composto por 5 camadas, cada uma com uma responsabilidade clara:

```
.claude/
  context/              → grounding do dominio (fonte de verdade para LLMs)
  agents/               → documentos de referencia que definem "personas" especializadas
    ddd/                → agente generico de DDD (agnostico, reutilizavel entre projetos)
      principles/       → diretrizes e restricoes de DDD
      capabilities/     → capacidades atomicas de DDD reutilizaveis
    spec-writer/        → agente generico de escrita de specs SDD (agnostico, JA EXISTE)
      principles/       → metodologia SDD, user stories, acceptance criteria, requirements, etc.
      capabilities/     → como escrever specs, user stories, edge cases, validar, adaptar brownfield
    spring-kotlin/      → agente generico de Spring Boot + Kotlin (agnostico, JA EXISTE)
      principles/       → idiomas Kotlin, Spring Boot, JPA, Security, Testing, Validation, etc.
      capabilities/     → como criar entities, repositories, services, controllers, testes, etc.
    domain-expert/      → agente especifico: extrai conceitos do dominio do projeto
    architect/          → agente especifico: valida boundaries do projeto
    modeler/            → agente especifico: modela agregados do projeto
    reviewer/           → agente especifico: valida modelo do projeto
  adapters/             → integracao com o spec-kit (constitution + templates)
  memory/               → registro de decisoes e padroes do projeto
  skills/               → comandos invocaveis pelo usuario (/nome)
```

### Separacao entre camadas genericas e dominio especifico

O sistema tem quatro camadas de conhecimento claramente separadas:

**Camada generica Spec Writer (`agents/spec-writer/`) — JA EXISTE:**
- Conhecimento puro de escrita de especificacoes SDD, agnostico a qualquer projeto
- Sabe como escrever user stories, acceptance criteria, requirements, edge cases, validar specs
- Contem principios (principles/) e capacidades atomicas (capabilities/)
- Pode ser reutilizado em qualquer projeto SDD sem modificacao
- Suporta greenfield e brownfield
- NAO sabe nada sobre nenhum dominio, framework ou ferramenta SDD
- Consumido durante a fase de especificacao, ANTES dos agentes de dominio
- **Este agente JA FOI GERADO e esta em `.claude/agents/spec-writer/` — NAO gerar novamente**

**Camada generica DDD (`agents/ddd/`):**
- Conhecimento puro de DDD, agnostico a qualquer projeto
- Sabe o que e um Aggregate Root, como definir boundaries, como identificar invariantes
- Contem principios (principles/) e capacidades atomicas (capabilities/)
- Pode ser reutilizado em qualquer projeto DDD sem modificacao
- NAO sabe nada sobre oficina mecanica, tb_customer, status_os, etc.
- Consumido durante a fase de especificacao (`/speckit.specify`, `/speckit.plan`)

**Camada generica Spring+Kotlin (`agents/spring-kotlin/`) — JA EXISTE:**
- Conhecimento puro de implementacao em Kotlin 2.x + Spring Boot 4.x, agnostico a qualquer projeto
- Sabe como criar entities JPA, repositories, services, controllers, testes, migrations
- Contem principios (principles/) e capacidades atomicas (capabilities/)
- Pode ser reutilizado em qualquer projeto Kotlin + Spring Boot sem modificacao
- NAO sabe nada sobre oficina mecanica, tb_customer, status_os, etc.
- Consumido durante a fase de implementacao (`/speckit.implement`)
- **Este agente JA FOI GERADO e esta em `.claude/agents/spring-kotlin/` — NAO gerar novamente**

**Camada especifica (`agents/domain-expert/`, `architect/`, `modeler/`, `reviewer/` + `context/`):**
- Conhecimento do dominio repair shop
- Sabe que existem 5 bounded contexts com nomes especificos
- Sabe que tb_service_order e aggregate root
- Referencia as camadas genericas para aplicar SDD, DDD e Spring+Kotlin ao dominio concreto
- Os agentes especificos consomem `context/` para dados do dominio, `agents/spec-writer/` para estrutura de specs, `agents/ddd/` para fundamentos DDD e `agents/spring-kotlin/` para padroes de implementacao

### Como as camadas se conectam

```
FASE DE ESPECIFICACAO:
Usuario roda /speckit.specify "Criar gestao de Ordens de Servico"

spec-kit le a constitution (adapters/spec-kit-constitution.md)
  → constitution referencia agents/spec-writer/AGENT.md
  → spec-writer define a estrutura da spec (user stories, acceptance criteria, requirements, edge cases)
  → spec-writer aplica principios de qualidade (INVEST, ZOMBIES, Given/When/Then)
  → constitution referencia agents/domain-expert/AGENT.md
  → domain-expert referencia agents/ddd/capabilities/extract-domain.md (como extrair)
  → domain-expert consome context/ubiquitous-language.md (o que extrair)
  → constitution referencia agents/architect/AGENT.md
  → architect referencia agents/ddd/principles/boundaries.md (regras genericas)
  → architect consome context/er-diagram.md (modelo concreto)
  → constitution referencia agents/modeler/AGENT.md
  → modeler referencia agents/ddd/capabilities/define-aggregate.md (como modelar)
  → modeler consome context/business-rules.md (regras concretas)
  → spec.md e gerada com estrutura SDD + visao DDD embutida

FASE DE IMPLEMENTACAO:
Usuario roda /speckit.implement

spec-kit le a constitution (adapters/spec-kit-constitution.md)
  → constitution referencia agents/spring-kotlin/AGENT.md
  → spring-kotlin fornece principles/ (como escrever Kotlin idiomatico, JPA, Security)
  → spring-kotlin fornece capabilities/ (como criar entity, repository, service, controller)
  → spec-kit gera codigo Kotlin/Spring Boot seguindo os padroes do agente
  → codigo gerado e idiomatico, com imports corretos do Spring Boot 4.x

Tudo acontece dentro do mesmo fluxo do spec-kit, mesma sessao do LLM.
Os agentes NAO sao processos separados — sao documentos de referencia
que o spec-kit consome sequencialmente, mantendo o contexto completo.
```

---

## Estrutura completa de arquivos

### 1. context/

Contem documentos de grounding do dominio, otimizados para consumo por LLMs. Sao a **fonte de verdade semantica** que os agentes especificos referenciam. Esses arquivos sao **especificos do projeto repair shop**.

```
context/
  event-storming.md
  ubiquitous-language.md
  er-diagram.md
  business-rules.md
```

#### context/event-storming.md

Descreve os fluxos do dominio em formato estruturado. Deve conter:
- Comandos (acoes que iniciam processos)
- Eventos (fatos que aconteceram no dominio)
- Politicas (regras que conectam eventos a comandos)
- Atores (quem executa cada acao)
- Eventos pivotais (que marcam mudancas de fase)
- Fluxos por bounded context

**Fonte:** `docs/miro/event-storming.pdf` + `docs/delivery/dictionary-ubiquitous-language.md` (secoes de eventos, comandos e politicas)

#### context/ubiquitous-language.md

Glossario completo do dominio, organizado por bounded context. Cada termo deve ter:
- Nome
- Tipo (Agregado, Entidade, Value Object, Ator, Evento, Comando, Politica, Modelo de Leitura, Sistema Externo)
- Definicao clara e sem ambiguidade
- Contexto em que se aplica

Deve incluir secao de **termos ambiguos** — termos que aparecem em mais de um contexto com significados diferentes (ex: "Finaliza OS" significa coisas diferentes no contexto Ordem de Servico e no contexto Pagamento).

**Fonte:** `docs/delivery/dictionary-ubiquitous-language.md`

#### context/er-diagram.md

Descricao textual do modelo de dados, otimizada para LLMs. Deve conter:
- Todas as 9 tabelas com seus campos, tipos e constraints
- Relacionamentos entre tabelas (1:N, N:N com tabela pivot)
- Enums com todos os valores
- Agrupamento por bounded context

**Tabelas:** tb_customer, tb_vehicle, tb_service_order, tb_service_order_history, tb_service, tb_service_insume (pivot N:N), tb_service_history, tb_insume, tb_user

**Enums:**
- `status_os`: RECEIVED, IN_DIAGNOSIS, WAITING_APPROVAL, APPROVED, REFUSED, IN_EXECUTION, FINALIZED, PAID, CANCELED
- `status_service`: INITIATED, PENDING, FINALIZED

**Fonte:** `docs/spec/database-er-diagram.png` + `docs/spec/database-er-diagram.html`

#### context/business-rules.md

Regras de negocio do dominio. Deve conter:
- Calculo de orcamento automatico (total_price = soma dos precos dos servicos)
- Transicoes validas de status_os (maquina de estados completa)
- Transicoes validas de status_service
- Regras de estoque (quantidade nunca negativa, baixa automatica ao aprovar OS)
- Validacoes (CPF/CNPJ, placa, email)
- Regras de seguranca (JWT, roles/functions)
- Historico automatico (tb_service_order_history e tb_service_history registram cada transicao)

**Fonte:** `docs/spec/delivery-specs.md` (secao 6 — Regras de negocio)

---

### 2. agents/

Agentes sao **documentos de referencia** que definem "personas" especializadas. Eles NAO sao processos separados nem skills invocaveis. Sao arquivos Markdown que o spec-kit consome via constitution, dentro do mesmo fluxo e mesma sessao do LLM. Isso garante que o contexto nao se fragmenta entre etapas.

Ha tres tipos de agentes:
- **Agentes genericos (agnosticos, reutilizaveis entre projetos):**
  - `spec-writer/` — fundamentos de escrita de specs SDD (**JA EXISTE em `.claude/agents/spec-writer/` — NAO gerar**)
  - `ddd/` — fundamentos de Domain-Driven Design (a ser gerado por este prompt)
  - `spring-kotlin/` — fundamentos de Spring Boot 4.x + Kotlin 2.x (**JA EXISTE em `.claude/agents/spring-kotlin/` — NAO gerar**)
- **Agentes especificos (`domain-expert/`, `architect/`, `modeler/`, `reviewer/`):** aplicam o conhecimento generico ao dominio concreto do projeto. Referenciam `agents/spec-writer/` para estrutura de specs, `agents/ddd/` e `agents/spring-kotlin/` para fundamentos, e `context/` para dados do dominio.

```
agents/
  spec-writer/                ← JA EXISTE — NAO gerar
    AGENT.md
    principles/
      sdd-methodology.md
      user-stories.md
      acceptance-criteria.md
      requirements.md
      success-criteria.md
      edge-cases.md
      clarification.md
    capabilities/
      write-spec.md
      write-user-story.md
      write-acceptance-criteria.md
      write-edge-cases.md
      refine-spec.md
      validate-spec.md
      adapt-brownfield.md
    OUTPUT.md
    EXAMPLES.md

  ddd/
    AGENT.md
    principles/
      ddd.md
      aggregates.md
      boundaries.md
      anti-patterns.md
      naming.md
    capabilities/
      extract-domain.md
      map-event-storming.md
      define-aggregate.md
      enforce-invariants.md
      validate-model.md
    OUTPUT.md
    EXAMPLES.md

  spring-kotlin/              ← JA EXISTE — NAO gerar
    AGENT.md
    principles/
      kotlin-idioms.md
      spring-boot.md
      spring-data-jpa.md
      spring-security.md
      testing.md
      openapi.md
      error-handling.md
      validation.md
    capabilities/
      create-entity.md
      create-repository.md
      create-service.md
      create-controller.md
      create-test.md
      create-dto.md
      create-migration.md
      create-enum.md
      create-config.md
    OUTPUT.md
    EXAMPLES.md

  domain-expert/
    AGENT.md
    CONTEXT.md
    OUTPUT.md
    EXAMPLES.md

  architect/
    AGENT.md
    CONSTRAINTS.md
    OUTPUT.md
    EXAMPLES.md

  modeler/
    AGENT.md
    RULES.md
    OUTPUT.md
    EXAMPLES.md

  reviewer/
    AGENT.md
    CHECKS.md
    OUTPUT.md
    EXAMPLES.md
```

---

#### Agente: ddd (generico, agnostico)

**Papel:** Fornecer fundamentos de Domain-Driven Design que qualquer agente especifico pode consumir. Este agente NAO e invocado diretamente — ele e uma base de conhecimento referenciada pelos outros agentes.

**AGENT.md** deve descrever:
- O que e DDD e qual seu proposito
- Como o agente se relaciona com os agentes especificos (e referenciado, nao invocado)
- Indice dos principios e capacidades disponiveis

**Este agente e agnostico — NAO deve conter referencias ao repair shop, tb_customer, status_os ou qualquer elemento especifico do projeto.**

##### ddd/principles/

Principios estaticos de DDD que nao mudam entre projetos nem entre features.

**ddd/principles/ddd.md** — Principios centrais:
- Dominio como nucleo isolado de frameworks
- Separacao entre camadas (domain, application, infrastructure, presentation)
- Ubiquitous Language como vocabulario obrigatorio em todo o codigo
- Entities vs Value Objects vs Aggregates — quando usar cada um
- Domain Events e quando usa-los
- Repositories como contratos do dominio (interface no dominio, implementacao na infra)
- Application Services como orquestradores de use cases
- Domain Services para logica que nao pertence a nenhuma entidade

**ddd/principles/aggregates.md** — Regras de agregados:
- Cada agregado tem exatamente um Aggregate Root
- Acesso externo ao agregado so via Root
- Transacoes nao devem cruzar boundaries de agregados
- Agregados devem ser pequenos (preferir referencias por ID a objetos aninhados)
- Invariantes devem ser protegidas dentro do agregado
- Um agregado e uma unidade de consistencia transacional

**ddd/principles/boundaries.md** — Bounded contexts:
- Cada bounded context tem sua propria linguagem ubiqua
- O mesmo termo pode ter significados diferentes em contextos diferentes
- Comunicacao entre contextos via eventos de dominio ou referencias por ID
- Nunca compartilhar entidades entre contextos
- Context Mapping: como contextos se relacionam (upstream/downstream, conformist, anticorruption layer)

**ddd/principles/anti-patterns.md** — O que evitar:
- Anemic Domain Model (entidades que sao apenas getters/setters sem comportamento)
- God Aggregate (um unico agregado que concentra toda a logica)
- Shared Kernel desnecessario (compartilhar codigo entre contextos sem justificativa)
- Vazamento de logica de dominio para controllers ou camada de apresentacao
- Acoplamento direto entre bounded contexts (importar entidades de outro contexto)
- CRUD thinking (pensar em tabelas e operacoes CRUD em vez de dominio e comportamento)
- Primitive Obsession (usar tipos primitivos em vez de Value Objects para conceitos do dominio)

**ddd/principles/naming.md** — Convencoes genericas de DDD para nomenclatura:
- Aggregates, Entities, Value Objects: substantivos do dominio
- Domain Events: verbo no passado (OrderCreated, PaymentReceived)
- Commands: verbo no imperativo (CreateOrder, ApproveQuote)
- Repositories: interface com prefixo do agregado (CustomerRepository, ServiceOrderRepository)
- Application Services: sufixo Service ou UseCase (CreateServiceOrderUseCase)
- Packages/modules: nomeados pelo bounded context

**Nota sobre naming.md:** Este arquivo contem convencoes **genericas** de DDD. Convencoes **especificas** do projeto (prefixo `tb_`, `id_tb_`, PascalCase para enums Kotlin, etc.) estao no `CLAUDE.md` na raiz do repositorio, secao "Code conventions".

##### ddd/capabilities/

Capacidades atomicas de DDD — instrucoes de "como fazer" que os agentes especificos referenciam. Cada capability e um guia pratico para uma atividade de modelagem DDD.

**ddd/capabilities/extract-domain.md** — Como extrair conceitos de dominio:
- Ler a descricao da feature/requisito
- Identificar substantivos (candidatos a entidades/VOs)
- Identificar verbos (candidatos a comandos/eventos)
- Identificar regras condicionais (candidatos a politicas)
- Identificar atores (quem executa cada acao)
- Cruzar com a linguagem ubiqua existente
- Sinalizar termos novos nao presentes no glossario

**ddd/capabilities/map-event-storming.md** — Como mapear event storming para modelo:
- Identificar eventos de dominio (fatos no passado)
- Identificar comandos que disparam eventos
- Identificar politicas (regras automaticas evento → comando)
- Identificar read models (dados consultados para tomar decisoes)
- Identificar sistemas externos (fora do boundary do dominio)
- Agrupar eventos por bounded context
- Identificar eventos pivotais (mudancas de fase)

**ddd/capabilities/define-aggregate.md** — Como definir agregados:
- Identificar a entidade raiz (Aggregate Root)
- Definir o boundary do agregado (quais entidades estao dentro)
- Listar invariantes que o agregado protege
- Verificar que o agregado e uma unidade de consistencia transacional
- Preferir agregados pequenos com referencias por ID
- Definir comportamentos (metodos) que protegem as invariantes

**ddd/capabilities/enforce-invariants.md** — Como identificar e proteger invariantes:
- Invariante = regra que DEVE ser verdadeira em todo momento
- Listar todas as regras de negocio que sao invariantes
- Determinar qual agregado protege cada invariante
- Verificar que invariantes nao cruzam boundaries de agregados
- Se uma invariante cruza boundaries, considerar eventual consistency

**ddd/capabilities/validate-model.md** — Como validar um modelo DDD:
- Verificar que todo agregado tem exatamente um Aggregate Root
- Verificar que invariantes estao dentro dos boundaries corretos
- Verificar que nao ha anti-patterns (comparar com principles/anti-patterns.md)
- Verificar que a linguagem ubiqua esta sendo respeitada
- Verificar que bounded contexts estao bem definidos e isolados
- Atribuir score de 0.0 a 1.0 com base nos checks

**ddd/OUTPUT.md** — Formato padrao de saida para capacidades DDD (Markdown estruturado). Todos os outputs devem seguir secoes claras com headers, listas e checklists.

**ddd/EXAMPLES.md** — Exemplos **minimalistas** que ilustram apenas a mecanica de DDD (como se define um agregado, como se declara uma invariante, como se documenta um bounded context). Usar exemplos curtos e abstratos — NAO construir dominios completos (e-commerce, biblioteca, etc.) que possam ancorar o raciocinio do LLM em padroes irrelevantes para o projeto real. Os exemplos ricos e detalhados do dominio ficam nos `EXAMPLES.md` dos agentes especificos (domain-expert, architect, modeler, reviewer).

---

#### Agente: domain-expert (especifico do projeto)

**Papel:** Extrair conceitos de dominio a partir de uma feature ou especificacao, aplicando-os ao contexto do repair shop.

**Responsabilidades:**
- Identificar quais termos da linguagem ubiqua se aplicam
- Mapear comandos, eventos e atores envolvidos
- Identificar politicas e eventos pivotais relevantes
- Garantir que a linguagem usada na spec corresponda ao glossario do dominio
- Sinalizar termos ambiguos e em qual contexto estao sendo usados

**Referencia DDD:** `agents/ddd/capabilities/extract-domain.md`, `agents/ddd/capabilities/map-event-storming.md`

**Consome do dominio:** `context/ubiquitous-language.md`, `context/event-storming.md`

**AGENT.md** deve definir o papel e referenciar os arquivos acima.

**CONTEXT.md** deve listar explicitamente quais arquivos de `context/` e `agents/ddd/` esse agente consome.

**OUTPUT.md** — Define o formato de saida em Markdown estruturado. Deve conter secoes para: Bounded Context, Actors, Commands, Events (marcando pivotais), Policies, Ubiquitous Language Terms Used, Ambiguities Detected.

**EXAMPLES.md** — Exemplos concretos do dominio repair shop. Por exemplo, para o bounded context "Ordem de Servico", o domain-expert identificaria: Atores (Cliente, Atendente, Mecanico), Comandos (Cria a OS, Realiza diagnostico, Avalia OS), Eventos (OS criada, Diagnostico realizado, OS aprovada, OS nao aprovada), Politicas (Quando diagnostico realizado → enviar orcamento ao cliente).

---

#### Agente: architect (especifico do projeto)

**Papel:** Validar que a modelagem respeita boundaries de bounded contexts e separacao de responsabilidades.

**Responsabilidades:**
- Confirmar que entidades nao vazam entre bounded contexts
- Validar que comunicacao entre contextos usa referencias por ID (nao objetos compartilhados)
- Identificar acoplamentos indevidos
- Sugerir pontos de integracao entre contextos

**Referencia DDD:** `agents/ddd/principles/boundaries.md`, `agents/ddd/principles/anti-patterns.md`

**Consome do dominio:** `context/er-diagram.md`

**AGENT.md** deve definir o papel e referenciar os arquivos acima.

**CONSTRAINTS.md** — Restricoes arquiteturais especificas do projeto:
- Os 5 bounded contexts do projeto e quais tabelas pertencem a cada um
- Monolito em camadas (nao microservicos)
- Comunicacao interna entre contextos via referencias por ID

**OUTPUT.md** — Define o formato de saida em Markdown estruturado. Deve conter secoes para: Bounded Context (entidades pertencentes, dependencias externas), Cross-Context References, Violations, Integration Points.

**EXAMPLES.md** — Exemplos concretos de validacao arquitetural do repair shop.

---

#### Agente: modeler (especifico do projeto)

**Papel:** Definir agregados, entidades, value objects e invariantes para uma feature ou bounded context.

**Responsabilidades:**
- Definir quais entidades sao Aggregate Roots
- Identificar Value Objects (ex: Document para CPF/CNPJ)
- Listar invariantes que o agregado deve proteger
- Traduzir regras de negocio em estrutura de dominio

**Referencia DDD:** `agents/ddd/capabilities/define-aggregate.md`, `agents/ddd/capabilities/enforce-invariants.md`, `agents/ddd/principles/aggregates.md`

**Consome do dominio:** `context/er-diagram.md`, `context/business-rules.md`

**AGENT.md** deve definir o papel e referenciar os arquivos acima.

**RULES.md** — Regras de modelagem especificas do projeto (ex: todas as entidades usam UUID como PK, timestamps sao `created`/`updated`).

**OUTPUT.md** — Define o formato de saida em Markdown estruturado. Deve conter secoes para: Aggregate (Root, Entities, Value Objects), Invariants (com codigos INV-XXX), Behavior (metodos e quais invariantes protegem).

**EXAMPLES.md** — Exemplos concretos de modelagem do repair shop.

---

#### Agente: reviewer (especifico do projeto)

**Papel:** Validar todo o modelo contra principios DDD e retornar um relatorio com score, problemas e sugestoes.

**Responsabilidades:**
- Verificar se todos os agregados tem Aggregate Root definido
- Verificar se invariantes estao dentro dos boundaries corretos
- Verificar se a linguagem ubiqua esta sendo respeitada
- Verificar se nao ha anti-patterns (anemic model, god aggregate, etc.)
- Atribuir score de 0.0 a 1.0

**Referencia DDD:** `agents/ddd/capabilities/validate-model.md`, `agents/ddd/principles/anti-patterns.md`

**Consome do dominio:** todos os arquivos de `context/`

**AGENT.md** deve definir o papel e referenciar os arquivos acima.

**CHECKS.md** — Checklist de validacao especifico do projeto:
- Todos os agregados tem Aggregate Root
- Invariantes dentro dos boundaries
- Value Objects extraidos onde aplicavel
- Linguagem ubiqua respeitada
- Sem anti-patterns detectados
- Convencoes de naming respeitadas (tb_, id_tb_, etc.)
- Status machines validas (status_os, status_service)

**OUTPUT.md** — Define o formato de saida em Markdown estruturado. Deve conter secoes para: Score (0.0 a 1.0), Issues (com severidade HIGH/MEDIUM/LOW), Suggestions, Checklist (com checkboxes).

**EXAMPLES.md** — Exemplos concretos de review do repair shop.

---

### 3. adapters/

Camada de integracao com o Spec Kit. Define como o conhecimento DDD e injetado no fluxo do spec-kit.

```
adapters/
  spec-kit-constitution.md
  spec-kit-templates.md
```

#### adapters/spec-kit-constitution.md

Constitution DDD-aware para ser usada pelo spec-kit. Deve:
- Injetar principios de DDD no fluxo de especificacao
- Referenciar os agentes como etapas sequenciais do raciocinio
- Referenciar os arquivos de context/ como base semantica
- Referenciar agents/ddd/principles/ como guarda-corpos
- Definir que toda spec deve conter: atores, comandos, eventos, invariantes, bounded context

Estrutura sugerida:

```markdown
# Constitution: Repair Shop

## Pre-loading Instructions
Before starting ANY specification workflow, read ALL files listed below
into your context. Do not proceed until all files have been loaded.
This ensures the full DDD foundation and domain knowledge is available
throughout the entire workflow without needing to re-read files mid-process.

### DDD Foundation (read first — generic, project-agnostic)
- agents/ddd/AGENT.md
- agents/ddd/principles/ddd.md
- agents/ddd/principles/aggregates.md
- agents/ddd/principles/boundaries.md
- agents/ddd/principles/anti-patterns.md
- agents/ddd/principles/naming.md
- agents/ddd/capabilities/extract-domain.md
- agents/ddd/capabilities/map-event-storming.md
- agents/ddd/capabilities/define-aggregate.md
- agents/ddd/capabilities/enforce-invariants.md
- agents/ddd/capabilities/validate-model.md

### Spring Boot + Kotlin Foundation (read for implementation phase — generic, project-agnostic)
- agents/spring-kotlin/AGENT.md
- agents/spring-kotlin/principles/kotlin-idioms.md
- agents/spring-kotlin/principles/spring-boot.md
- agents/spring-kotlin/principles/spring-data-jpa.md
- agents/spring-kotlin/principles/spring-security.md
- agents/spring-kotlin/principles/testing.md
- agents/spring-kotlin/principles/openapi.md
- agents/spring-kotlin/principles/error-handling.md
- agents/spring-kotlin/principles/validation.md
- agents/spring-kotlin/capabilities/create-entity.md
- agents/spring-kotlin/capabilities/create-repository.md
- agents/spring-kotlin/capabilities/create-service.md
- agents/spring-kotlin/capabilities/create-controller.md
- agents/spring-kotlin/capabilities/create-test.md
- agents/spring-kotlin/capabilities/create-dto.md
- agents/spring-kotlin/capabilities/create-migration.md
- agents/spring-kotlin/capabilities/create-enum.md
- agents/spring-kotlin/capabilities/create-config.md

### Domain Context (read second — project-specific)
- context/ubiquitous-language.md
- context/event-storming.md
- context/er-diagram.md
- context/business-rules.md

### Agent Definitions (read third — roles and rules)
- agents/domain-expert/AGENT.md
- agents/domain-expert/CONTEXT.md
- agents/architect/AGENT.md
- agents/architect/CONSTRAINTS.md
- agents/modeler/AGENT.md
- agents/modeler/RULES.md
- agents/reviewer/AGENT.md
- agents/reviewer/CHECKS.md

## Specification Workflow (specify/plan phase)
After all files are loaded, apply the following roles sequentially:
1. Apply domain-expert role (agents/domain-expert/AGENT.md)
   - Uses capabilities: agents/ddd/capabilities/extract-domain.md, map-event-storming.md
   - Output format: agents/domain-expert/OUTPUT.md
2. Apply architect role (agents/architect/AGENT.md)
   - Uses principles: agents/ddd/principles/boundaries.md
   - Output format: agents/architect/OUTPUT.md
3. Apply modeler role (agents/modeler/AGENT.md)
   - Uses capabilities: agents/ddd/capabilities/define-aggregate.md, enforce-invariants.md
   - Output format: agents/modeler/OUTPUT.md

## Quality Gates
Before finalizing any spec, apply reviewer role (agents/reviewer/AGENT.md).
- Uses capability: agents/ddd/capabilities/validate-model.md
- Output format: agents/reviewer/OUTPUT.md
- Score must be >= 0.7 to proceed.

## Implementation Workflow (implement phase)
When generating code from an approved spec:
1. Apply spring-kotlin agent (agents/spring-kotlin/AGENT.md)
   - Uses principles for idiomatic patterns
   - Uses capabilities for each artifact type (entity, repository, service, controller, etc.)
   - Output format: agents/spring-kotlin/OUTPUT.md
2. Follow DDD model from the spec (aggregates, boundaries, invariants)
3. Follow project conventions from CLAUDE.md (naming, packages)

## Feedback Loop
If reviewer score < 0.7:
1. List all HIGH and MEDIUM issues from the review
2. Re-apply modeler role to address each issue specifically
3. Re-run reviewer to validate corrections
4. If score still < 0.7 after 2 iterations, stop and present
   the remaining issues to the user for decision
5. Register the user's decision in memory/decisions.md
```

#### adapters/spec-kit-templates.md

Extensoes aos templates padrao do spec-kit. Toda spec gerada deve incluir, alem dos campos padrao do spec-kit:
- **Actors:** quem interage com a feature
- **Commands:** acoes que a feature expoe
- **Events:** eventos de dominio que a feature gera
- **Invariants:** regras que devem ser protegidas
- **Bounded Context:** em qual contexto a feature se insere
- **Edge Cases:** cenarios de borda baseados nas regras de negocio

---

### 4. memory/

Registro de decisoes e padroes identificados ao longo do desenvolvimento. Esses arquivos sao atualizados incrementalmente conforme o projeto evolui.

```
memory/
  decisions.md
  patterns.md
```

#### memory/decisions.md

Log de decisoes arquiteturais tomadas pelo time. Dividido em duas secoes:

**Active Decisions (always read)** — Decisoes que impactam specs e modelagem atuais. O LLM deve ler esta secao sempre antes de gerar qualquer spec. Cada entrada deve ter:
- Data
- Decisao
- Motivo
- Alternativas consideradas

**Historical Decisions (read only if relevant)** — Decisoes que ja foram aplicadas e nao impactam specs novas. O LLM so le esta secao se precisar de contexto historico. Quando uma decisao ativa perde relevancia para specs futuras, mover para esta secao.

Iniciar com as decisoes ja tomadas:

Active:
- PostgreSQL como banco (impacta toda spec que envolve persistencia)
- Arquitetura monolitica em camadas (impacta toda decisao arquitetural)
- Prefixo tb_ para tabelas (impacta toda modelagem de entidades)
- UUID para primary keys (impacta toda modelagem de entidades)

Historical:
- Migrou de Gradle para Maven (ja aplicado, nao impacta specs)

#### memory/patterns.md

Padroes identificados e reutilizaveis no projeto. Iniciar vazio — sera preenchido conforme o time identifica padroes durante a implementacao.

---

### 5. skills/

Comandos invocaveis pelo usuario via `/nome` no Claude Code. Skills sao o unico mecanismo nativo do Claude Code para slash commands — arquivos SKILL.md dentro de `.claude/skills/<nome>/`.

```
skills/
  commit/SKILL.md              → (ja existe, nao modificar)
  domain-reviewer/SKILL.md     → validacao DDD sob demanda
```

#### skills/domain-reviewer/SKILL.md

Skill invocavel com `/domain-reviewer`. Permite ao usuario validar o modelo de dominio a qualquer momento, fora do fluxo do spec-kit.

**Frontmatter:**
```yaml
---
name: domain-reviewer
description: Validate the current domain model against DDD principles and project conventions
disable-model-invocation: true
---
```

**Comportamento:**
1. Ler todos os arquivos de `context/` para entender o dominio atual
2. Ler `agents/ddd/principles/` para criterios genericos de DDD
3. Ler `agents/ddd/capabilities/validate-model.md` para o metodo de validacao
4. Ler `agents/reviewer/CHECKS.md` para o checklist especifico do projeto
5. Aplicar o papel do reviewer conforme `agents/reviewer/AGENT.md`
6. Retornar o relatorio no formato definido em `agents/reviewer/OUTPUT.md`

---

## Resolucao de conflitos

Quando regras de dominio (context/) parecerem conflitar com principios DDD (agents/ddd/):
1. Tratar como um sinal de modelagem, nao como um conflito — reavaliar se os boundaries dos agregados estao corretamente definidos
2. Se a remodelagem resolver a tensao, atualizar o modelo de acordo
3. Se a tensao for genuina (trade-off pragmatico), as regras de dominio prevalecem — o negocio define O QUE deve acontecer, o DDD orienta COMO
4. Documentar a decisao em memory/decisions.md com:
   - O conflito identificado
   - Por que a remodelagem nao foi viavel (se aplicavel)
   - O trade-off aceito
   - Impacto no modelo

---

## Filosofia de design

- Trate LLMs como assistentes, nao como autoridade
- Mantenha logica de dominio no codigo (Kotlin/Spring Boot)
- Use este sistema apenas para: raciocinio, modelagem e validacao
- Saidas sempre em Markdown estruturado, nunca JSON
- O spec-kit e o orquestrador — o sistema cognitivo e a inteligencia que ele consome
- Agentes sao documentos de referencia, nao processos separados
- Todo o contexto e mantido na mesma sessao do LLM (sem fragmentacao)
- Conhecimento generico (agents/spec-writer/, agents/ddd/ e agents/spring-kotlin/) e separado do conhecimento do dominio (context/ + agentes especificos)

---

## Entregaveis

Gerar a estrutura completa dentro de `.claude/` com todos os arquivos listados acima:

1. **context/** — 4 arquivos com grounding do dominio (baseados em `docs/`, nao copias)
2. **agents/spec-writer/** — **JA EXISTE (17 arquivos) — NAO gerar novamente**
3. **agents/ddd/** — 1 AGENT.md + 5 principles + 5 capabilities + OUTPUT.md + EXAMPLES.md (12 arquivos, todos agnosticos)
4. **agents/spring-kotlin/** — **JA EXISTE (20 arquivos) — NAO gerar novamente**
5. **agents/domain-expert/** — AGENT.md, CONTEXT.md, OUTPUT.md, EXAMPLES.md (4 arquivos, especificos do repair shop)
6. **agents/architect/** — AGENT.md, CONSTRAINTS.md, OUTPUT.md, EXAMPLES.md (4 arquivos, especificos do repair shop)
7. **agents/modeler/** — AGENT.md, RULES.md, OUTPUT.md, EXAMPLES.md (4 arquivos, especificos do repair shop)
8. **agents/reviewer/** — AGENT.md, CHECKS.md, OUTPUT.md, EXAMPLES.md (4 arquivos, especificos do repair shop)
9. **adapters/** — 2 arquivos de integracao com spec-kit
10. **memory/** — 2 arquivos de registro (decisions pre-preenchido, patterns vazio)
11. **skills/domain-reviewer/** — 1 skill invocavel

**Total a gerar:** 37 arquivos.
**Ja existentes (NAO gerar):** `agents/spec-writer/` (17 arquivos) + `agents/spring-kotlin/` (20 arquivos) + `skills/commit/SKILL.md` (1 arquivo).

---

## Ordem de implementacao

Implementar na seguinte ordem, pois cada camada depende da anterior:

1. **context/** (primeiro — e a base que tudo referencia)
2. **agents/spec-writer/** — **PULAR: ja existe em `.claude/agents/spec-writer/`**
3. **agents/ddd/** (segundo — fundamentos DDD que os agentes especificos referenciam)
4. **agents/spring-kotlin/** — **PULAR: ja existe em `.claude/agents/spring-kotlin/`**
5. **agents/domain-expert/**, **agents/architect/**, **agents/modeler/**, **agents/reviewer/** (terceiro — consomem context/, agents/spec-writer/, agents/ddd/ e agents/spring-kotlin/)
6. **adapters/** (quarto — conecta agentes ao spec-kit, referencia todos os agentes genericos)
7. **memory/** (quinto — registro inicial de decisoes)
8. **skills/domain-reviewer/** (sexto — skill que usa agents/reviewer/ e agents/ddd/)

---

Agora gere o sistema completo.
