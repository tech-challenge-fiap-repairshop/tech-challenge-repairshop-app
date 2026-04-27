# Prompt: Agente Spec Writer para Sistema Cognitivo

Voce e um arquiteto de software senior, especialista em Spec-Driven Development (SDD), engenharia de requisitos e designer de documentos de referencia otimizados para consumo por LLMs.

Sua tarefa e gerar o agente `agents/spec-writer/` dentro de `.claude/` no repositorio do projeto. Este agente fornece fundamentos de como escrever especificacoes de software de alta qualidade que direcionam a geracao de codigo atraves de ferramentas SDD.

**IMPORTANTE:** Todos os arquivos gerados (AGENT.md, principles/, capabilities/, OUTPUT.md, EXAMPLES.md) devem ser escritos em **ingles**. Somente este prompt esta em portugues.

---

## Metodologia alvo

- **Spec-Driven Development (SDD)** — especificacoes sao o artefato primario, codigo e a saida gerada
- O agente cobre: user stories, acceptance criteria, requirements, success criteria, edge cases, refinamento de specs, validacao de specs
- Aplicavel tanto para **greenfield** (projetos novos do zero) quanto para **brownfield** (features em projetos existentes)

---

## Contexto do sistema cognitivo

Este agente faz parte de um sistema cognitivo com multiplos agentes. Existem tres tipos:
- **Agentes genericos (agnosticos):** Conhecimento puro reutilizavel entre projetos. Este agente (`spec-writer/`) e um deles, junto com `ddd/` (Domain-Driven Design) e `spring-kotlin/` (implementacao).
- **Agentes especificos:** Aplicam o conhecimento generico ao dominio concreto de cada projeto. Exemplos: `domain-expert/`, `architect/`, `modeler/`, `reviewer/`.
- **Adapters:** Ponte entre agentes genericos e ferramentas especificas. A camada de adapter conecta o spec-writer a qualquer ferramenta SDD que o projeto use.

O agente `spec-writer/` e **generico e agnostico** — NAO deve conter referencias a nenhum dominio, tabela, enum, regra de negocio, framework ou ferramenta SDD especifica. Ele sabe como escrever specs. O dominio vem dos agentes especificos e dos arquivos de contexto. A integracao com ferramentas SDD vem dos `adapters/`.

### Como se integra com os outros agentes

O spec-writer opera **primeiro** no workflow de especificacao, antes dos agentes de dominio:

```
spec-writer (COMO escrever a spec — estrutura, formato, qualidade)
  → domain-expert (QUAIS conceitos de dominio incluir)
  → architect (valida boundaries e separacao)
  → modeler (define agregados e invariantes)
  → reviewer (valida o modelo, atribui score)
  → output: docs/spec/MM-DD-YY-spec/spec.md
  → proximo passo: spec-planner consome este output
```

O spec-writer define a **estrutura e os padroes de qualidade** da spec. O domain-expert preenche com **conhecimento de dominio**. Essa separacao permite reutilizar o spec-writer em qualquer projeto, independente do dominio.

### Estrutura de output

A spec e salva num diretorio com o padrao `docs/spec/MM-DD-YY-spec/`:
```
docs/spec/MM-DD-YY-spec/
  spec.md              — a especificacao completa
```

O diretorio permite que artefatos futuros sejam adicionados sem mudar a convencao de nomenclatura. O padrao MM-DD-YY usa a data de criacao da spec.

### Atualizacoes necessarias apos geracao

Apos gerar este agente, os seguintes arquivos precisam ser atualizados (NAO por este prompt — separadamente):

1. `adapters/spec-kit-constitution.md` — adicionar spec-writer ao Specification Workflow, antes do domain-expert. Adicionar arquivos do spec-writer nas Pre-loading Instructions.
2. `adapters/spec-kit-templates.md` — alinhar extensoes de template com o formato de saida do spec-writer.
3. `docs/prompt/prompt-cognitive-system.md` — referenciar o agente spec-writer (mesmo padrao usado para spring-kotlin).

---

## Objetivo

Criar o agente `agents/spec-writer/` com:
- Principios de escrita de especificacoes na metodologia SDD
- Capacidades para cada etapa do processo de escrita de specs
- Suporte explicito para cenarios greenfield e brownfield
- Formato de saida padrao
- Exemplos minimalistas (mecanica, nao dominio)

O agente sera consumido durante a fase de especificacao dos workflows SDD para garantir que toda spec seja completa, inequivoca, testavel e de alta qualidade.

---

## Restricoes

- **NAO** incluir referencias a nenhum dominio especifico (tabelas, enums, regras de negocio de qualquer projeto)
- **NAO** incluir referencias a nenhuma ferramenta SDD especifica (spec-kit ou qualquer outra — o adapter cuida disso)
- **NAO** incluir referencias a nenhum framework ou linguagem de programacao
- **NAO** incluir detalhes de implementacao em exemplos de specs — specs descrevem O QUE e POR QUE, nunca COMO
- **NAO** incluir Co-Authored-By ou qualquer atribuicao ao Claude/IA
- Todos os exemplos devem ser **minimalistas** — ilustrar a mecanica, nao construir um dominio
- Todas as saidas devem ser em **Markdown estruturado**, nao JSON
- Todos os arquivos gerados devem ser escritos em **ingles**

---

## Estrutura de arquivos a gerar

```
.claude/agents/spec-writer/
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
```

**Total:** 17 arquivos.

---

## Conteudo detalhado de cada arquivo

### AGENT.md

Deve descrever:
- O que e este agente e qual seu proposito
- Que ele e generico e agnostico (nao sabe nada de nenhum dominio ou ferramenta)
- Como se relaciona com outros agentes (referenciado por adapters, consumido antes dos agentes de dominio)
- Indice de todos os principles e capabilities disponiveis
- Que suporta tanto greenfield quanto brownfield
- Que e consumido durante a fase de especificacao dos workflows SDD

---

### principles/sdd-methodology.md

Conceitos centrais de Spec-Driven Development. Deve cobrir:

- **Specs como artefato primario:** Especificacoes direcionam a implementacao. Codigo e a saida gerada numa linguagem especifica. Manter software significa evoluir especificacoes, nao so codigo.
- **O pipeline SDD:** Especificacao → Clarificacao → Planejamento → Quebra em tarefas → Implementacao. Cada fase e uma TRANSFORMACAO, nao uma substituicao — specs nao ficam obsoletas quando voce planeja.
- **Separacao de responsabilidades no pipeline:**
  - Spec = O QUE os usuarios precisam e POR QUE (perspectiva de negocio)
  - Plan = COMO construir (perspectiva tecnica)
  - Tasks = passos acionaveis derivados do plano
  - Implementation = codigo que satisfaz a spec
- **Constitution como governanca:** Principios imutaveis do projeto que restringem todas as fases. Specs devem se alinhar com os gates constitucionais.
- **Qualidade sobre velocidade:** Uma spec bem escrita economiza mais tempo do que custa. Uma spec vaga produz codigo vago que precisa de retrabalho.
- **Specs sao documentos vivos:** Evoluem conforme o entendimento cresce. Versionar e rastrear mudancas.

---

### principles/user-stories.md

Como escrever user stories efetivas. Deve cobrir:

- **Formato:** Titulo, prioridade (P1/P2/P3), descricao em linguagem natural, justificativa da prioridade, teste independente, cenarios de aceitacao
- **Niveis de prioridade:**
  - P1 (MVP-Critical): Mais valioso, deve ser feito primeiro, base para outras features
  - P2 (High-Value): Importante mas nao bloqueante, pode ser feito apos P1
  - P3 (Nice-to-Have): Desejavel mas nao critico para o MVP
- **Regra de independencia:** Cada user story DEVE ser independentemente testavel e entregavel. Se voce implementar apenas a User Story 1 (P1), deve ter um MVP completo e funcional que entrega valor.
- **Ordenacao:** Stories sao ordenadas por prioridade, nao apenas rotuladas. P1 primeiro.
- **Dimensionamento:** Manter stories pequenas o suficiente para ter 3-7 acceptance criteria. Se tiver mais, dividir.
- **Sem detalhes de implementacao:** Stories descrevem necessidades do usuario, nao solucoes tecnicas.
- **Uma story, um valor:** Cada story entrega um valor distinto e demonstravel ao usuario.
- **Criterios INVEST:** Cada user story deve ser avaliada contra os 6 criterios INVEST:
  - **I — Independent:** A story nao depende de outras stories para ser implementada e testada. Se a Story 2 precisa ser feita antes da Story 1, elas nao sao independentes — reestruturar.
  - **N — Negotiable:** A story nao e um contrato rigido — e um convite para conversa. Define o valor desejado, nao a solucao exata. Os detalhes podem ser ajustados durante o desenvolvimento sem perder o proposito original.
  - **V — Valuable:** A story deve entregar valor perceptivel ao usuario final. Tarefas tecnicas ("refatorar banco de dados") nao sao user stories. Se o usuario nao percebe o beneficio, nao e uma story.
  - **E — Estimable:** O time deve conseguir estimar o esforco da story. Se ninguem consegue estimar, a story e grande demais ou pouco clara. Forca a story a ter escopo definido.
  - **S — Small:** A story deve ser pequena o suficiente para ser implementada em um ciclo curto. Se tem mais de 7 acceptance criteria, dividir em stories menores.
  - **T — Testable:** Deve ser possivel escrever um teste que verifica se a story foi implementada. Se nao da para definir o teste, a story e vaga demais.

---

### principles/acceptance-criteria.md

Como escrever acceptance criteria. Deve cobrir:

- **Formato Given-When-Then (Gherkin):**
  - Given [precondicao/estado inicial]
  - When [acao executada pelo usuario/sistema]
  - Then [resultado esperado]
- **Um cenario por comportamento:** Cada criterio testa exatamente UM comportamento. Nao combinar multiplos comportamentos.
- **Happy paths E unhappy paths:** Para cada cenario de sucesso, escrever pelo menos um cenario de falha.
- **Concreto sobre abstrato:**
  - Ruim: "Sistema trata input invalido adequadamente"
  - Bom: "Given um campo nome em branco, When usuario submete o formulario, Then mensagem de erro 'Nome e obrigatorio' e exibida"
- **Evitar "deveria":** Usar "deve" ou afirmacoes diretas. "Retorna 201" nao "deveria retornar 201."
- **Numeravel:** Numerar criterios para referencia em testes e codigo (AC-1, AC-2, etc.)
- **Verificavel:** Cada criterio deve ser objetivamente verificavel — sem julgamentos subjetivos.

---

### principles/requirements.md

Como escrever functional requirements e definir escopo. Deve cobrir:

- **Formato de functional requirements:** FR-XXX numerados, cada um comeca com "System MUST [capability]"
- **Testaveis e inequivocos:** Cada requirement deve ser verificavel. Se nao da para escrever um teste, nao e um requirement.
- **Key entities:** Quando dados estao envolvidos, listar entidades com atributos chave e relacionamentos. Descrever O QUE a entidade representa, nao COMO e armazenada.
- **Limites do escopo:**
  - Declarar explicitamente o que esta DENTRO do escopo
  - Declarar explicitamente o que esta FORA do escopo
  - Assumptions que a spec faz (usuarios alvo, dependencias, restricoes)
- **Sem vazamento de implementacao:** Requirements descrevem comportamento, nao tecnologia.
- **Non-functional requirements (NFR-XXX):** Alem dos functional requirements, toda spec deve considerar requisitos de qualidade. NFRs seguem o mesmo padrao — numerados, testaveis, mensuraveis. Categorias principais:
  - **Performance:** tempos de resposta, throughput, limites de carga (mensuravel: "em menos de Xms com ate Y registros")
  - **Seguranca:** autenticacao, autorizacao, armazenamento de dados sensiveis (mensuravel: "senhas armazenadas com hash", "endpoints administrativos exigem token valido")
  - **Observabilidade:** logging, monitoramento, rastreabilidade (mensuravel: "toda operacao critica gera log estruturado com timestamp e user ID")
  - **Disponibilidade:** uptime, recuperacao de falhas (mensuravel: "disponivel 99.5% do tempo")
  - **Escalabilidade:** usuarios concorrentes, volume de dados (mensuravel: "suportar X usuarios concorrentes")
  - Cada NFR deve ser tao testavel quanto um FR. "O sistema deve ser rapido" NAO e um NFR. "O sistema deve responder em menos de 500ms" e.
  - Se nenhum NFR se aplica a uma feature, declarar explicitamente: "Nenhum requisito nao-funcional adicional identificado para esta feature."
- **Marcadores [NEEDS CLARIFICATION]:** Quando um requirement e ambiguo, marcar em vez de adivinhar. Maximo 3 marcadores por spec — resolver antes da fase de planejamento.

---

### principles/success-criteria.md

Como definir success criteria mensuraveis. Deve cobrir:

- **Formato SC-XXX:** Numerados, cada um descreve um resultado mensuravel
- **Technology-agnostic:** Success criteria descrevem resultados para o usuario, nao metricas de sistema.
- **Sem adjetivos vagos:** Evitar "robusto", "intuitivo", "escalavel", "rapido" sem numeros. Cada adjetivo deve ser acompanhado de uma metrica.
- **Mensuravel:** Cada criterio deve ter uma forma de medir aprovacao/reprovacao.
- **Perspectiva do usuario:** Success criteria sao do ponto de vista do usuario, nao do desenvolvedor.

---

### principles/edge-cases.md

Como identificar e documentar edge cases. Deve cobrir:

- **Metodologia ZOMBIES:**
  - **Z**ero: O que acontece com inputs zero/vazio/null?
  - **O**ne: O que acontece com exatamente um item?
  - **M**any: O que acontece com muitos itens? Existe limite?
  - **B**oundary: Valores min/max, limites de tamanho de string, limites de data
  - **I**nterface: O que acontece nos limites da API? Requests malformados?
  - **E**xceptions: Falhas de rede, banco indisponivel, modificacoes concorrentes
  - **S**ecurity: Acesso nao autorizado, injecao de input, exposicao de dados
- **Formato de tabela para edge cases:**
  - Colunas: #, Cenario, Input, Resultado Esperado
  - Cada edge case numerado (E1, E2, ...) para referencia
- **Aplicacao sistematica:** Aplicar ZOMBIES a TODA feature, nao so as "obvias". Os edge cases nao obvios sao os que causam bugs em producao.
- **Acesso concorrente:** Sempre considerar o que acontece quando dois usuarios modificam os mesmos dados simultaneamente.

---

### principles/clarification.md

Como lidar com ambiguidade em specs. Deve cobrir:

- **[NEEDS CLARIFICATION: pergunta especifica]:** Marcar pontos pouco claros com essa tag. A pergunta deve ser especifica — nao "o que deve acontecer?" mas "emails duplicados devem ser rejeitados com 409 ou silenciosamente ignorados?"
- **Maximo 3 marcadores por spec:** Se mais de 3 coisas estao pouco claras, a feature nao esta bem entendida ainda.
- **Fluxo de resolucao:**
  1. Escrever a spec com marcadores
  2. Apresentar marcadores ao stakeholder/usuario
  3. Stakeholder escolhe ou fornece resposta
  4. Atualizar spec com a resposta
  5. Remover o marcador
- **Clarificar antes de planejar:** Todos os marcadores devem ser resolvidos antes de mover para a fase de planejamento.
- **Assumptions como fallback:** Se uma clarificacao nao pode ser resolvida, fazer uma assumption explicita e documentar na secao Assumptions.

---

### capabilities/write-spec.md

Guia passo a passo para escrever uma spec completa do zero. Deve incluir:

1. **Entender a feature:** O que o usuario precisa? Por que? Quem e o usuario?
2. **Identificar atores:** Quem interage com essa feature?
3. **Escrever user stories:** Comecar com P1 (MVP), depois P2, depois P3. Cada uma independentemente testavel.
4. **Escrever acceptance criteria:** Given/When/Then para cada story. Happy + unhappy paths.
5. **Definir functional requirements:** Formato FR-XXX, testaveis, inequivocos.
6. **Definir key entities:** Se dados estao envolvidos, listar entidades e relacionamentos (O QUE, nao COMO).
7. **Definir success criteria:** Formato SC-XXX, mensuraveis, technology-agnostic.
8. **Identificar edge cases:** Aplicar ZOMBIES sistematicamente.
9. **Documentar assumptions:** O que esta assumindo sobre usuarios, escopo, dependencias.
10. **Marcar ambiguidades:** [NEEDS CLARIFICATION] para qualquer coisa pouco clara. Max 3.
11. **Auto-revisao:** Rodar o checklist de qualidade (validate-spec capability).

Incluir o seguinte template da estrutura completa da spec:

```markdown
# Feature Specification: [FEATURE NAME]

## User Scenarios & Testing

### User Story 1 — [Title] (Priority: P1)
[Plain language description from user perspective]

**Why this priority:** [Value and dependency justification]
**Independent Test:** [How to verify this story in isolation]
**Acceptance Scenarios:**
1. **Given** [precondition], **When** [action], **Then** [expected result]
2. **Given** [precondition], **When** [action], **Then** [expected result]

### User Story 2 — [Title] (Priority: P2)
[...]

### Edge Cases
| # | Scenario | Input | Expected Result |
|---|----------|-------|-----------------|
| E1 | [description] | [input] | [result] |
| E2 | [description] | [input] | [result] |

## Requirements

### Functional Requirements
- FR-001: System MUST [specific testable capability]
- FR-002: System MUST [specific testable capability]

### Non-Functional Requirements
- NFR-001: System MUST [measurable quality attribute]
- NFR-002: System MUST [measurable quality attribute]

### Key Entities (if data involved)
- [Entity]: [What it represents, key attributes, relationships]

## Success Criteria
- SC-001: [Measurable, technology-agnostic outcome]
- SC-002: [Measurable, technology-agnostic outcome]

## Assumptions
- [Assumption about target users]
- [Assumption about scope boundaries]
- [Dependency on existing systems]

## Out of Scope
- [What this feature explicitly does NOT cover]
```

---

### capabilities/write-user-story.md

Como escrever uma user story individual. Deve incluir:

1. **Escolher um titulo** que descreva o valor para o usuario, nao a tarefa tecnica
2. **Atribuir prioridade** (P1/P2/P3) baseado em valor e dependencia
3. **Escrever a descricao** em linguagem natural da perspectiva do usuario
4. **Explicar a prioridade** — por que e P1 e nao P2?
5. **Definir o teste independente** — como essa story pode ser verificada isoladamente?
6. **Escrever acceptance scenarios** — pelo menos 2 (um happy, um unhappy)
7. **Verificar independencia** — se essa for a UNICA story implementada, entrega valor?

Incluir checklist de verificacao.

---

### capabilities/write-acceptance-criteria.md

Como escrever acceptance criteria para uma user story. Deve incluir:

1. **Listar todos os comportamentos** que a story implica (explicitos e implicitos)
2. **Para cada comportamento,** escrever um cenario Given/When/Then
3. **Adicionar unhappy paths:** Para cada "Then sucesso", perguntar "e se falhar?"
4. **Numerar cada criterio** (AC-1, AC-2, ...)
5. **Verificar completude:** O conjunto de criterios descreve completamente a story?
6. **Verificar independencia:** Cada criterio pode ser testado sozinho?

Incluir guidelines para uso de valores concretos nos exemplos.

---

### capabilities/write-edge-cases.md

Como identificar edge cases sistematicamente. Deve incluir:

1. **Aplicar ZOMBIES a cada feature** com descricao de cada letra
2. **Para cada edge case, documentar:** cenario, input que dispara, resultado esperado
3. **Priorizar edge cases:** Seguranca e integridade de dados primeiro
4. **Considerar acesso concorrente:** O que acontece quando dois usuarios fazem a mesma acao simultaneamente?

Incluir template de tabela de edge cases.

---

### capabilities/refine-spec.md

Como refinar e clarificar uma spec existente. Deve incluir:

1. **Ler a spec completamente** — entender todas as stories, criterios, requirements
2. **Identificar marcadores [NEEDS CLARIFICATION]** — listar com contexto
3. **Para cada marcador:** formular 2-3 opcoes concretas, apresentar ao stakeholder, atualizar spec
4. **Verificar ambiguidades implicitas** — coisas que DEVERIAM ter marcadores mas nao tem (linguagem vaga, cenarios de erro faltando, limites indefinidos)
5. **Re-rodar checklist de qualidade** apos refinamento

---

### capabilities/validate-spec.md

Checklist de qualidade para validacao de specs. Deve incluir:

**Qualidade do conteudo:**
- Sem detalhes de implementacao
- Focado em valor para o usuario
- Escrito para stakeholders nao-tecnicos
- Todas as secoes obrigatorias completas

**Completude de requirements:**
- Nenhum marcador [NEEDS CLARIFICATION] restante
- Todos os requirements testaveis e inequivocos
- Success criteria mensuraveis e technology-agnostic
- Todos os acceptance scenarios definidos (happy + unhappy)
- Edge cases identificados usando ZOMBIES
- Escopo claramente delimitado
- Dependencias e assumptions documentadas

**Qualidade das user stories:**
- Cada story independentemente testavel e entregavel
- Stories ordenadas por prioridade
- Stories P1 sozinhas formam um MVP viavel
- Nenhuma story com mais de 7 acceptance criteria
- Cada story entrega valor distinto ao usuario

**Prontidao da feature:**
- Todos os functional requirements com acceptance criteria claros
- User scenarios cobrem fluxos primarios
- Feature atende success criteria mensuraveis
- Sem vazamento de detalhes de implementacao

Incluir regras de scoring (tudo passa = pronto, 1-3 falhas menores = corrigir, 4+ falhas = revisao maior).

---

### capabilities/adapt-brownfield.md

Como escrever specs para features em projetos existentes. Deve incluir:

1. **Entender o codebase existente:** Ler estrutura, convencoes, padroes (naming, arquitetura, error handling)
2. **Entender conhecimento de dominio existente:** Ler documentacao, glossarios, decisoes arquiteturais
3. **Escrever specs que respeitam a base:** Novas features alinham com convencoes existentes. Specs referenciam entidades e padroes existentes, nao os redefinem. Declarar explicitamente o que e NOVO vs o que ESTENDE funcionalidade existente.
4. **Identificar pontos de integracao:** Como a nova feature interage com features existentes? Que APIs, entidades ou servicos existentes ela consome? Que efeitos colaterais pode ter?
5. **Consciencia de regressao:** Que testes existentes devem continuar passando? Que comportamentos existentes nao devem mudar? Documentar mudancas intencionais de comportamento explicitamente.
6. **Consideracoes de migracao:** A nova feature requer migracao de dados? Compatibilidade retroativa e necessaria? Pode ser deployada incrementalmente?

Diferenca chave entre greenfield e brownfield:
- Greenfield: define o mundo do zero, liberdade total
- Brownfield: define MUDANCAS num mundo existente, deve respeitar codigo, convencoes, padroes e conhecimento de dominio existentes

---

### OUTPUT.md

Formato padrao de saida quando o agente spec-writer e referenciado durante geracao de especificacoes. Deve incluir secoes para: status da secao (draft/needs-clarification/complete), conteudo da spec, quality check inline, e notas sobre decisoes.

Regras:
- Todas as saidas em Markdown — nunca JSON
- Headers de secao claros
- Quality check inline para cada secao
- Campo de status para rastrear completude
- Notas para decisoes que precisam de contexto

---

### EXAMPLES.md

Exemplos **minimalistas** que ilustram a mecanica de escrita de specs. NAO construir dominios completos. Usar features abstratas como "Create Item", "Process Order", "Register User" para demonstrar padroes.

Incluir um exemplo curto para cada capability:
- Esqueleto de spec completa (write-spec)
- User story individual com acceptance criteria (write-user-story)
- Cenarios Given/When/Then (write-acceptance-criteria)
- Tabela de edge cases ZOMBIES (write-edge-cases)
- Resolucao de clarificacao (refine-spec)
- Checklist de qualidade pass/fail (validate-spec)
- Nota de adaptacao brownfield (adapt-brownfield)

Cada exemplo deve ter no maximo 10-20 linhas.

---

## Ordem de implementacao

Gerar os arquivos na seguinte ordem:

1. **AGENT.md** (primeiro — define o agente e indexa tudo)
2. **principles/** (segundo — conhecimento fundamental)
   - sdd-methodology.md
   - user-stories.md
   - acceptance-criteria.md
   - requirements.md
   - success-criteria.md
   - edge-cases.md
   - clarification.md
3. **capabilities/** (terceiro — consomem os principios)
   - write-spec.md
   - write-user-story.md
   - write-acceptance-criteria.md
   - write-edge-cases.md
   - refine-spec.md
   - validate-spec.md
   - adapt-brownfield.md
4. **OUTPUT.md** (quarto — formato de saida)
5. **EXAMPLES.md** (quinto — exemplos minimalistas)

---

## Base de conhecimento para geracao

O conteudo dos arquivos deve ser baseado em praticas estabelecidas de engenharia de requisitos e especificacao:

- Metodologia Spec-Driven Development (specs como artefato primario, transformacao em pipeline)
- Boas praticas de user stories (criterios INVEST, ordenacao por prioridade, independencia)
- Padroes de acceptance criteria (Given/When/Then, formato Gherkin)
- Identificacao de edge cases (metodologia ZOMBIES)
- Engenharia de requisitos (functional requirements, definicao de escopo, tratamento de ambiguidade)
- Praticas de desenvolvimento brownfield (respeitar codigo existente, pontos de integracao, regressao)

Pontos criticos que devem ser refletidos nos principios:

### Qualidade das specs
- Specs focam em O QUE e POR QUE, nunca COMO
- Sem detalhes de implementacao (nenhuma linguagem, framework, banco de dados)
- Todo requirement deve ser testavel e inequivoco
- Success criteria devem ser mensuraveis com metricas especificas, nao adjetivos vagos
- User stories devem ser independentemente testaveis e entregaveis

### Greenfield vs Brownfield
- Greenfield: define o mundo do zero, liberdade total
- Brownfield: define MUDANCAS num mundo existente, deve respeitar codigo, convencoes, padroes e conhecimento de dominio existentes
- O agente deve suportar explicitamente ambos cenarios com orientacao diferente
- Specs brownfield devem documentar pontos de integracao, riscos de regressao e necessidades de migracao

### Integridade do pipeline
- Specs sao a fonte de verdade — planos traduzem specs, nao as substituem
- Cada fase valida a anterior
- Ambiguidades devem ser resolvidas antes do planejamento
- Quality gates garantem completude da spec antes de prosseguir

### Principio de independencia
- User Story 1 (P1) sozinha deve ser um MVP viavel
- Cada story pode ser implementada, testada e deployada independentemente
- Stories de prioridade maior nao dependem de stories de prioridade menor

---

Agora gere o agente completo. Lembre-se: todos os arquivos gerados em **ingles**, somente este prompt esta em portugues.
