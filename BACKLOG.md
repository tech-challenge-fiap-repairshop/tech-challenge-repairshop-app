# BACKLOG.md — Repair Shop

Backlog de melhorias identificadas via analise completa do codigo-fonte.
Organizado por severidade e categoria.
Itens resolvidos marcados com ~~tachado~~ e data de resolucao.

---

## ALTA — Bugs de Runtime

### ~~BKL-001: Mismatch Execution.service_order_id vs schema service_order~~ (RESOLVIDO 2026-04-27)

- **Arquivo:** `Execution.kt:24`
- **Schema:** `V1__create_schema.sql:97`
- **Problema:** A entity mapeava `@JoinColumn(name = "service_order_id")`, mas a coluna no banco e `service_order` (sem `_id`).
- **Resolucao:** JoinColumn corrigido para `name = "service_order"` na Fase 1B da refatoracao DDD.

### ~~BKL-002: Mismatch ServiceOrder.createdAt/updatedAt vs schema created/updated~~ (RESOLVIDO 2026-04-27)

- **Arquivo:** `ServiceOrder.kt:71-76`
- **Schema:** `V1__create_schema.sql:82-83`
- **Problema:** A entity usava `createdAt`/`updatedAt`, mas as colunas na tabela sao `created`/`updated`.
- **Resolucao:** Campos renomeados para `created`/`updated` com `@Column(name = "...")` explicito na Fase 1A.

### ~~BKL-003: Logica invertida em CustomerService.create~~ (RESOLVIDO 2026-04-27)

- **Arquivo:** `CustomerService.kt:41`
- **Problema:** `verifyAndTakeByEmail(email)` lancava `EntityNotFoundException` se o customer NAO existir, impedindo criacao de customer novo.
- **Resolucao:** Substituido por verificacao de duplicata com `DuplicateEntityException` na Fase 5A.

---

## MEDIA — Seguranca

### ~~BKL-004: JWT secret hardcoded no application.properties~~ (RESOLVIDO 2026-04-28)

- **Arquivo:** `application.properties:25`
- **Problema:** `jwt.secret=super-secret-key-that-should-be-changed-in-production-at-least-256-bits-long` esta commitado no repositorio.
- **Correcao:** Usar variavel de ambiente: `jwt.secret=${JWT_SECRET}`.
- **Origem:** Analise de codigo + SonarQube (secrets:S6703)

### ~~BKL-005: Credenciais de banco hardcoded~~ (RESOLVIDO 2026-04-28)

- **Arquivo:** `application.properties:5-6`, `docker-compose.yml:6-8`
- **Problema:** Username e password do PostgreSQL estao fixos no codigo commitado.
- **Correcao:** Usar variaveis de ambiente para producao. Aceitavel para desenvolvimento local se documentado.
- **Origem:** Analise de codigo + SonarQube (java:S6437)

### ~~BKL-006: Registro aberto permite criar ATTENDANT sem restricao~~ (EXCLUÍDO/TRANSFORMADO EM EPIC 2026-04-28)

- **Arquivo:** `SecurityConfig.kt:31`
- **Problema:** `POST /auth/register` e `permitAll`. Qualquer pessoa pode registrar um user com role `ATTENDANT`, que tem acesso completo ao sistema.
- **Correcao:** Restringir criacao de ATTENDANT a users ja autenticados com role ATTENDANT, ou criar endpoint administrativo separado.

### ~~BKL-007: JwtService.validateToken engole todas as excecoes~~ (RESOLVIDO 2026-04-28)

- **Arquivo:** `JwtService.kt:29-38`
- **Problema:** `catch (e: Exception)` captura qualquer excecao (inclusive `OutOfMemoryError` via `Exception`). Retorna `null` silenciosamente.
- **Correcao:** Capturar apenas `JwtException` (e subclasses) ao inves de `Exception`.

### ~~BKL-008: Endpoints /services, /insumes e /invoices sem autorizacao por role~~ (RESOLVIDO 2026-04-28)

- **Arquivo:** `SecurityConfig.kt:29-48`
- **Problema:** Nao ha regra explicita para `/services/**`, `/insumes/**` e `/invoices/**`. Caem no `anyRequest, authenticated`, permitindo que CUSTOMER crie/delete executions, insumes e invoices.
- **Correcao:** Adicionar regras de autorizacao por role para esses endpoints.

---

## MEDIA — Logica de Negocio

### ~~BKL-009: UserService.verifyRegisteredCustomer nao verifica role CUSTOMER~~ (RESOLVIDO 2026-04-27)

- **Arquivo:** `UserService.kt:56-58`
- **Problema:** Apenas verificava se existia um user com o email, sem verificar role `CUSTOMER`.
- **Resolucao:** Adicionada verificacao `if (user.function != UserRole.CUSTOMER) throw ComplianceException(...)` na Fase 5B.

### ~~BKL-010: ServiceOrderMetricsService.getMetrics carrega todos os registros em memoria~~ (RESOLVIDO 2026-04-28)

- **Arquivo:** `ServiceOrderMetricsService.kt` (movido de ServiceOrderService na Fase 4B)
- **Problema:** `serviceOrderRepository.findAll()` sem paginacao. Com volume alto de OSs, causa `OutOfMemoryError`.
- **Correcao:** Usar query nativa com agregacao no banco, ou stream com `@QueryHints`.

### ~~BKL-011: Document init usa require com throw interno (redundante)~~ (RESOLVIDO 2026-04-27)

- **Arquivo:** `Document.kt:12-14`
- **Problema:** `require(...) { throw InvalidDocumentException(...) }` — funciona por acidente mas e confuso.
- **Resolucao:** Substituido por `if (...) throw InvalidDocumentException(...)` na Fase 3A.

### ~~BKL-012: InvoiceService.create usa require ao inves de exception de dominio~~ (RESOLVIDO 2026-04-27)

- **Arquivo:** `InvoiceService.kt:30-32`
- **Problema:** Lancava `IllegalArgumentException` (400) ao inves de exception de dominio (422).
- **Resolucao:** Substituido por `InvalidStateTransitionException` na Fase 5C.

---

## MEDIA — Arquitetura / DDD

### ~~BKL-024: Bounded context register dependia de serviceorder (direcao errada)~~ (RESOLVIDO 2026-04-27)

- **Arquivos:** `CustomerService.kt`, `VehicleService.kt`
- **Problema:** Modulo register importava `ServiceOrderService` para checar existencia de OSs no delete. Direcao errada de dependencia.
- **Resolucao:** Criada interface `ServiceOrderExistenceChecker` no modulo register, implementada por adapter no modulo serviceorder (Fase 2A).

### ~~BKL-025: ExecutionService acessava repositories de outros bounded contexts~~ (RESOLVIDO 2026-04-27)

- **Arquivo:** `ExecutionService.kt`
- **Problema:** Importava `InsumeRepository` e `ServiceOrderRepository` diretamente, bypassando a camada de servico dos outros contextos.
- **Resolucao:** Criadas interfaces `InsumeLookup` e `ServiceOrderAccessor` com adapters (Fase 2B+2C).

### ~~BKL-026: ServiceOrderService construia entidades de outro bounded context~~ (RESOLVIDO 2026-04-27)

- **Arquivo:** `ServiceOrderService.kt:52-75`
- **Problema:** Construia `Execution`, `ExecutionInsume` e `ExecutionInsumeId` manualmente. Violacao de encapsulamento entre contextos.
- **Resolucao:** Delegate para `ServiceOrder.addExecution()` (Fase 2D).

### ~~BKL-027: Modelo de dominio anemico~~ (RESOLVIDO 2026-04-27)

- **Arquivos:** `ServiceOrder.kt`, `Execution.kt`, `Customer.kt`, `Vehicle.kt`
- **Problema:** Entidades eram data holders sem comportamento. Toda logica no service.
- **Resolucao:** Adicionados metodos de dominio: `advanceStatus()`, `recordHistory()`, `approve()`, `refuse()`, `addExecution()`, `collectInsumeRequirements()`, `recalculateTotalPrice()`, `addInsume()`, `updateDetails()` (Fases 1A-1D).

### ~~BKL-028: Value Objects acoplados ao JPA~~ (RESOLVIDO 2026-04-27)

- **Arquivos:** `Document.kt`, `Email.kt`, `Plate.kt`
- **Problema:** Usavam `@Embeddable` (annotation de infraestrutura em objetos de dominio).
- **Resolucao:** Removido `@Embeddable`, criados `AttributeConverter` na camada de persistencia (Fase 3).

### BKL-029: BasicExecution deveria ser tabela ao inves de enum

- **Arquivo:** `execution/domain/BasicExecution.kt`
- **Problema:** `BasicExecution` e um enum Kotlin com valores fixos (OIL_CHANGE, BRAKE_INSPECTION, etc.). Para adicionar um novo tipo de servico, e necessario alterar o codigo e redeployar. Tipos de servico sao dados cadastrais, nao constantes.
- **Correcao:** Criar tabela `tb_basic_execution` (id, name, description), entity `BasicExecutionEntity`, e substituir o enum por FK na `tb_execution`. Requer nova migration.
- **Origem:** Anotacao de revisao do grupo

### ~~BKL-030: ExecutionService ainda dispara save no ServiceOrder (filho atualiza pai)~~ (RESOLVIDO 2026-04-28)

- **Arquivo:** `ExecutionService.kt` — metodo `updateServiceOrderPrice()`
- **Problema:** Ao criar, deletar ou atualizar uma Execution, o `ExecutionService` chama `serviceOrderAccessor.save(serviceOrder)` para recalcular o preco. O filho (Execution) esta disparando persistencia no pai (ServiceOrder), o que viola a direcao de responsabilidade do aggregate.
- **Correcao:** Mover a logica de recalculo de preco para o `ServiceOrderService`, expondo um metodo `recalculatePrice(serviceOrderId)` que o controller ou um evento chama apos operacoes em executions.
- **Origem:** Anotacao de revisao do grupo ("Filho nao atualiza o pai - o pai atualiza o filho")

---

## BAIXA — Inconsistencias

### ~~BKL-013: Convencao de timestamps misturada entre entidades~~ (PARCIALMENTE RESOLVIDO 2026-04-27)

- **Problema:** `ServiceOrder` e `Execution` usavam `@CreationTimestamp`/`@UpdateTimestamp`. Outras entidades usavam `LocalDateTime.now()` manual.
- **Resolucao parcial:** ServiceOrder corrigido para usar `LocalDateTime.now()` (Fase 1A). Execution ainda usa `LocalDateTime.now()` manual no `advanceStatus` mas nao tem mais `@CreationTimestamp`/`@UpdateTimestamp`.

### ~~BKL-014: ExecutionHistory usa UUID direto vs ServiceOrderHistory usa @ManyToOne~~ (RESOLVIDO 2026-04-28)

- **Arquivo:** `ExecutionHistory.kt:22-23` vs `ServiceOrderHistory.kt:17-18`
- **Problema:** `ExecutionHistory` referencia execution por `executionId: UUID` (coluna simples). `ServiceOrderHistory` referencia por `@ManyToOne var serviceOrder: ServiceOrder`. Inconsistencia no padrao de relacionamento.
- **Correcao:** Padronizar para um dos dois estilos.

### ~~BKL-015: SafeString no campo password rejeita caracteres validos~~ (RESOLVIDO 2026-04-28)

- **Arquivo:** `UserDto.kt:26`
- **Problema:** `@SafeString` no password rejeita `<`, `>` e outros caracteres. Senhas sao hasheadas antes de persistir, entao essa validacao restringe senhas desnecessariamente.
- **Correcao:** Remover `@SafeString` do campo password.

### ~~BKL-016: Falta @SafeString no invoiceNumber~~ (RESOLVIDO 2026-04-28)

- **Arquivo:** `InvoiceDto.kt:17`
- **Problema:** Todos os outros campos de texto user-facing usam `@SafeString`, mas `invoiceNumber` nao.
- **Correcao:** Adicionar `@field:SafeString` ao campo.

### ~~BKL-017: InsumeService.getEntityById sem @Transactional~~ (RESOLVIDO 2026-04-27)

- **Arquivo:** `InsumeService.kt:42-45`
- **Resolucao:** Adicionado `@Transactional(readOnly = true)` na Fase 5D.

### ~~BKL-018: CustomerService.verifyAndTakeByEmail — nome confuso~~ (RESOLVIDO 2026-04-27)

- **Arquivo:** `CustomerService.kt:53`
- **Resolucao:** Renomeado para `findByEmailOrThrow` na Fase 5E.

### ~~BKL-019: Enums PostgreSQL criados no schema mas nao utilizados~~ (RESOLVIDO 2026-04-27)

- **Arquivo:** `V1__create_schema.sql:6-16`
- **Resolucao:** Criada migration `V2__drop_unused_enums.sql` na Fase 5F.

### ~~BKL-020: Wildcard import com ponto e virgula estilo Java~~ (RESOLVIDO 2026-04-27)

- **Arquivo:** `ServiceOrder.kt:12`
- **Resolucao:** Imports reescritos como imports explicitos Kotlin na Fase 1A.

### ~~BKL-021: Imports nao utilizados em varias classes~~ (RESOLVIDO 2026-04-27)

- **Arquivos:** `Insume.kt`, `Vehicle.kt`, `VehicleRepository.kt`
- **Resolucao:** Imports removidos nas Fases 1D e 5G.

### ~~BKL-022: ComplianceException usada para regras de negocio simples~~ (RESOLVIDO 2026-04-28)

- **Arquivo:** `CustomerService.kt`, `VehicleService.kt`
- **Problema:** `ComplianceException` (nome sugere conformidade regulatoria) e usada para impedir delecao com OSs associadas.
- **Correcao:** Considerar `IllegalStateException` ou exception mais descritiva.

### ~~BKL-023: ExecutionService usa alias de import para @Service~~ (RESOLVIDO 2026-04-28)

- **Arquivo:** `ExecutionService.kt:28`
- **Problema:** `import org.springframework.stereotype.Service as SpringService` para evitar conflito de nome. Funcional mas atipico.
- **Correcao:** Aceitavel. Alternativa seria usar FQN na annotation.

### ~~BKL-031: SonarQube — collection deveria ser imutavel no MetricsService~~ (RESOLVIDO 2026-04-28)

- **Arquivo:** `ServiceOrderMetricsService.kt`
- **Problema:** `serviceOrderRepository.findAll()` retorna `MutableList`, mas o resultado so e lido (nunca mutado). SonarQube rule kotlin:S6524.
- **Correcao:** Adicionar `.toList()` apos `findAll()` para tornar a referencia imutavel.
- **Origem:** SonarQube report (abr. 24)

---

## MEDIA — Validacao e Regras de Negocio

### ~~BKL-032: OS pode ser criada sem nenhum servico (lista vazia)~~ (EXCLUÍDO/REGRA DE NEGÓCIO 2026-04-28)

- **Arquivo:** `ServiceOrderService.kt` — `createServiceOrder()`
- **Problema:** O campo `request.services` pode ser uma lista vazia. O sistema cria uma OS sem nenhuma execution vinculada, com `totalPrice = 0`. Na pratica, uma OS sem servicos nao faz sentido no dominio.
- **Correcao:** Adicionar validacao: se `request.services` estiver vazia, lancar exception. Alternativa: adicionar `@field:NotEmpty` no DTO `CreateServiceOrderRequest.services`.
- **Origem:** Anotacao "Cadastro user / UserRole / ServiceOrder" — fluxo de criacao de OS

### ~~BKL-033: Sem validacao de que diagnostico produziu executions antes de avancar para WAITING_APPROVAL~~ (RESOLVIDO 2026-04-28)

- **Arquivo:** `ServiceOrder.kt` — `advanceStatus()`
- **Problema:** A transicao `IN_DIAGNOSIS -> WAITING_APPROVAL` nao verifica se pelo menos uma execution foi adicionada durante o diagnostico. Um mecanico pode "concluir" o diagnostico sem registrar nenhum servico, e o orcamento sera enviado ao cliente com valor zero.
- **Correcao:** Adicionar validacao no `advanceStatus()`: se `newStatus == WAITING_APPROVAL && executions.isEmpty()`, lancar exception.
- **Origem:** Anotacao "Nao cria a OS direto, so depois do diagnostico"

### ~~BKL-034: Insume.deductStock aceita quantidades negativas e zero~~ (RESOLVIDO 2026-04-28)

- **Arquivo:** `Insume.kt:59-62` — `deductStock(amount)`
- **Problema:** O metodo verifica `if (quantity < amount)` mas nao verifica se `amount <= 0`. Passar `amount = -5` faz `5 < -5 = false`, entao subtrai -5, resultando em `quantity += 5`. Isso permite adicionar estoque via deducao. Passar `amount = 0` e um no-op silencioso.
- **Correcao:** Adicionar `if (amount <= 0) throw IllegalArgumentException("Deduction amount must be positive")` no inicio do metodo.
- **Origem:** Anotacao "Regra de insumos - subtrair os insumos da OS da gestao/estoque"

### ~~BKL-035: InsumeItemRequest.quantity sem validacao de minimo~~ (RESOLVIDO 2026-04-28)

- **Arquivo:** `ServiceOrderDto.kt:18` — `InsumeItemRequest`
- **Problema:** `val quantity: Int = 1` aceita qualquer valor inteiro, inclusive zero e negativos. Nao tem `@Min(1)`. Um request com `quantity: 0` ou `quantity: -3` e aceito silenciosamente.
- **Correcao:** Adicionar `@field:Min(1, message = "Quantity must be at least 1")` ao campo.
- **Origem:** Anotacao "Testar cadastro da OS e dos insumos"

### ~~BKL-036: Mesmo insume pode ser adicionado duas vezes na mesma execution~~ (RESOLVIDO 2026-04-28)

- **Arquivo:** `Execution.kt` — `addInsume()`
- **Problema:** A colecao `insumes` e um `MutableSet<ExecutionInsume>` com `@EmbeddedId` composto por `(executionId, insumeId)`. Se o mesmo insumeId for passado duas vezes na request, o Set deduplica pela PK composta — a segunda chamada sobrescreve a primeira silenciosamente, perdendo a quantidade da primeira.
- **Correcao:** Validar no `addInsume()`: se ja existe um `ExecutionInsume` com o mesmo `insume.id`, lancar exception ou somar quantidades.
- **Origem:** Anotacao "Testar cadastro da OS e dos insumos"

---

## MEDIA — Anti-patterns e Clean Code

### ~~BKL-037: ApprovalDomainService instanciado manualmente ao inves de injetado~~ (RESOLVIDO 2026-04-28)

- **Arquivo:** `ServiceOrderService.kt` — `private val approvalDomainService = ApprovalDomainService()`
- **Problema:** Domain Service criado com `= ApprovalDomainService()` direto no campo, ao inves de ser injetado via construtor. Isso impede mockar o domain service em testes e viola o padrao de injecao de dependencia.
- **Correcao:** Anotar `ApprovalDomainService` com `@Component` (ou registrar como `@Bean`) e injetar no construtor de `ServiceOrderService`.
- **Origem:** Anotacao "Validar anti-pattern -> executar clean code"

### ~~BKL-038: ExecutionService manipula colecao interna do ServiceOrder diretamente~~ (RESOLVIDO 2026-04-28)

- **Arquivo:** `ExecutionService.kt` — `serviceOrder.executions.add(savedExecution)` nos metodos `create()` e `createBatch()`
- **Problema:** O service acessa `serviceOrder.executions` diretamente e adiciona execution, ao inves de usar o metodo de dominio `serviceOrder.addExecution()`. Isso bypassa a logica de encapsulamento do aggregate (recalculo de preco, etc). E Feature Envy — o service "inveja" a responsabilidade do aggregate.
- **Correcao:** Alinhar com BKL-030 — se Execution e child do aggregate, toda manipulacao deve passar pelo ServiceOrderService.
- **Origem:** Anotacao "Validar anti-pattern -> executar clean code"

---

## MEDIA — Observabilidade

### BKL-039: Zero logging em toda a aplicacao

- **Problema:** Nenhum service, controller, filter ou exception handler tem logging. Operacoes criticas (aprovacao de OS, deducao de estoque, autenticacao) executam sem rastro.
- **Correcao:** Adicionar `kotlin-logging` ao pom.xml. Implementar logging em: ServiceOrderService (create, approve), InsumeService (deductStock, restoreStock), UserService (authenticate, createUser), JwtAuthenticationFilter, GlobalExceptionHandler.
- **Origem:** Analise do especialista de Observabilidade

### ~~BKL-040: Sem Spring Boot Actuator (health checks, info)~~ (RESOLVIDO 2026-04-28)

- **Problema:** `spring-boot-starter-actuator` nao esta no pom.xml. Docker nao tem como detectar se a app travou ou ficou em estado inconsistente. Nao existe `/actuator/health`.
- **Correcao:** Adicionar dependencia, configurar endpoints `health` e `info`, criar health check customizado para DB, atualizar docker-compose com healthcheck na app.
- **Origem:** Analise do especialista de Observabilidade

### BKL-041: Sem correlation ID para rastreamento de requests

- **Problema:** Nao existe `X-Correlation-ID` header nem MDC logging. Impossivel correlacionar logs de uma mesma request entre diferentes services/metodos.
- **Correcao:** Criar `CorrelationIdFilter`, configurar logback-spring.xml com pattern incluindo `%X{correlationId}`.
- **Origem:** Analise do especialista de Observabilidade

---

## MEDIA — Cobertura de Testes

### BKL-042: Metodos de dominio novos sem testes diretos (~15 testes faltantes)

- **Arquivos:** `ServiceOrder.kt`, `Execution.kt`, `Customer.kt`
- **Problema:** Os metodos `approve()`, `refuse()`, `recordHistory()`, `addExecution()`, `collectInsumeRequirements()`, `addInsume()`, `updateDetails()` adicionados na refatoracao DDD nao tem testes unitarios diretos. Sao testados apenas indiretamente via services com mocks.
- **Correcao:** Criar `ServiceOrderDomainTest`, `ExecutionDomainTest`, `CustomerDomainTest`.
- **Origem:** Analise do especialista de Testes + anotacao "Testar cadastro da OS e dos insumos"

### ~~BKL-043: ApprovalDomainService sem testes~~ (RESOLVIDO 2026-04-29)

- **Arquivo:** `ApprovalDomainService.kt`
- **Problema:** Domain service novo, zero cobertura. Metodos `approve()` e `refuse()` nao testados.
- **Correcao:** Criar `ApprovalDomainServiceTest` com cenarios: approve retorna stock requirements, refuse transiciona status, approve com OS sem insumes retorna lista vazia.
- **Origem:** Analise do especialista de Testes

### BKL-044: AttributeConverters sem testes (DocumentConverter, EmailConverter, PlateConverter)

- **Arquivos:** `register/repository/converter/*.kt`
- **Problema:** 3 converters novos, zero cobertura. `convertToDatabaseColumn` e `convertToEntityAttribute` nao testados, inclusive com nulls.
- **Correcao:** Criar `DocumentConverterTest`, `EmailConverterTest`, `PlateConverterTest`.
- **Origem:** Analise do especialista de Testes

### BKL-045: Email value object sem testes de validacao

- **Arquivo:** `Email.kt`
- **Problema:** Document e Plate tem testes (`DocumentTest`, `PlateTest`), mas Email nao. A validacao no construtor (regex) nao e testada.
- **Correcao:** Criar `EmailTest` com cenarios: email valido, sem @, sem dominio, vazio, com espacos.
- **Origem:** Analise do especialista de Testes

### BKL-046: Zero testes de integracao com banco real

- **Problema:** Todos os 121 testes sao unitarios com mocks. Nenhum testa: converters com JPA, cascade de entidades, transacoes, lazy loading. `RepairshopApplicationTests` esta `@Disabled` por falta de banco.
- **Correcao:** Adicionar Testcontainers (PostgreSQL) ao pom.xml. Criar `ServiceOrderIntegrationTest` com fluxo completo: criar OS -> aprovar -> verificar estoque. Habilitar `RepairshopApplicationTests`.
- **Origem:** Analise do especialista de Testes

### BKL-047: Auditoria da rota de métricas (getMetrics)

- **Arquivo:** `ServiceOrderMetricsService.kt`
- **Problema:** Validar se a lógica agregada via Query Nativa está retornando os dados corretos após a introdução dos novos status (`IN_EXECUTION`, `PAID`).
- **Correcao:** Criar testes de integração ou auditoria manual de banco para validar os agrupamentos de métricas.
- **Origem:** Analise de consistência financeira.

### BKL-048: Proteção contra retrocesso de status no Update

- **Arquivo:** `ServiceOrderService.kt` / `ServiceOrder.kt`
- **Problema:** O método de atualização da Ordem de Serviço deve garantir que um status avançado (ex: `PAID`) não possa ser alterado manualmente para um status anterior (ex: `IN_DIAGNOSIS`), quebrando a integridade da máquina de estados.
- **Correcao:** Aplicar as mesmas regras de `allowedTransitions()` no momento de salvar atualizações parciais da OS.
- **Origem:** Especialista em Arquitetura.

### BKL-049: Imutabilidade de Entidades Finalizadas/Pagas

- **Arquivos:** `ServiceOrder.kt`, `Execution.kt`, `ServiceOrderService.kt`
- **Problema:** Atualmente, o sistema permite atualizar atributos de execuções ou adicionar novos itens mesmo após a OS estar `FINALIZED` ou `PAID`. Isso compromete a integridade histórica e financeira.
- **Correcao:** Implementar travas no domínio e no service. Se a entidade estiver em um estado terminal (`FINALIZED`, `PAID`, `CANCELED`), qualquer tentativa de alteração de preço, descrição, insumos ou remoção de itens deve ser bloqueada.
- **Origem:** Especialistas (Arquitetura e Produto).

---

## ALTA — Fase 2: Requisitos do Tech Challenge

> Itens identificados na análise do PDF `14SOAT - Fase 2 - Tech challenge.pdf` utilizando os especialistas de `docs/sdd/`.
> Estes itens são **requisitos obrigatórios** para entrega da Fase 2.

### ~~BKL-050: Listagem de OS com ordenação por prioridade de status~~ (RESOLVIDO 2026-06-29)

- **Arquivo:** `ServiceOrderRepository.kt`, `ServiceOrderGateway.kt`, `FindAllServiceOrdersImpl.kt`
- **Problema:** `findAll(pageable)` retorna as OS sem ordenação por prioridade de status. O PDF exige ordenação: Em Execução > Aguardando Aprovação > Diagnóstico > Recebida.
- **Correção:** Criar query customizada com `ORDER BY CASE WHEN status = 'IN_EXECUTION' THEN 1 WHEN status = 'WAITING_APPROVAL' THEN 2 WHEN status = 'IN_DIAGNOSIS' THEN 3 WHEN status = 'RECEIVED' THEN 4 ELSE 5 END`. Adicionar novo método no Gateway ou tornar o padrão.
- **Origem:** PDF Fase 2, slide sobre Listagem de OS

### ~~BKL-051: Listagem de OS com ordenação temporal (mais antigas primeiro)~~ (RESOLVIDO 2026-06-29)

- **Arquivo:** `FindAllServiceOrdersImpl.kt`, `ServiceOrderGatewayImplJPA.kt`
- **Problema:** Sem ordenação padrão por `enterTime ASC`. O Pageable do Spring aceita `?sort=enterTime,asc` via query param, mas não há default.
- **Correção:** Definir `Sort.by(Sort.Direction.ASC, "enterTime")` como default no use case ou adicionar como secondary sort à query de prioridade de status.
- **Origem:** PDF Fase 2, slide sobre Listagem de OS

### ~~BKL-052: Listagem de OS com filtro de exclusão de finalizadas/entregues~~ (RESOLVIDO 2026-06-29)

- **Arquivo:** `ServiceOrderRepository.kt`, `ServiceOrderGateway.kt`, `FindAllServiceOrdersImpl.kt`
- **Problema:** `findAll(pageable)` retorna TODAS as OS, incluindo FINALIZED, PAID e CANCELED. O PDF exige exclusão lógica (não física) das OS finalizadas e entregues.
- **Correção:** Criar `findAllActive(pageable)` com `@Query("SELECT so FROM ServiceOrderEntity so WHERE so.status NOT IN ('FINALIZED', 'PAID', 'CANCELED')")` ou usar Spring Data derived query `findByStatusNotIn(...)`.
- **Origem:** PDF Fase 2, slide sobre Listagem de OS

### ~~BKL-053: Criar manifestos Kubernetes (Deployments, Services, ConfigMaps, Secrets, HPA)~~ (RESOLVIDO 2026-06-29)

- **Diretório:** `/k8s/` (a ser criado)
- **Problema:** Não existem manifestos Kubernetes no projeto. O PDF exige orquestração com K8s.
- **Correção:** Criar pasta `/k8s/` com:
  - `deployment.yaml` — Deployment da aplicação com liveness/readiness probes via Actuator
  - `service.yaml` — ClusterIP ou LoadBalancer para expor a app
  - `configmap.yaml` — Variáveis de configuração (URLs, profiles)
  - `secret.yaml` — Credenciais de banco, JWT secret
  - `hpa.yaml` — HPA baseado em CPU/memória (80% threshold)
  - `postgres-deployment.yaml` — Deployment do PostgreSQL (ou referência a managed DB)
- **Origem:** PDF Fase 2, seção Orquestração com K8s

### ~~BKL-054: Criar scripts Terraform para infraestrutura~~ (RESOLVIDO 2026-06-29)

- **Diretório:** `/infra/` (a ser criado)
- **Problema:** Não existem scripts de IaC no projeto. O PDF exige provisionamento com Terraform.
- **Correção:** Criar pasta `/infra/` com:
  - `main.tf` — Provider e recursos principais
  - `variables.tf` — Variáveis de entrada
  - `outputs.tf` — Saídas do provisionamento
  - `kubernetes.tf` — Cluster K8s (EKS/GKE/AKS ou Kind/Minikube para local)
  - `database.tf` — PostgreSQL (RDS/Cloud SQL ou container)
  - `README.md` — Documentação de como aplicar os scripts
- **Origem:** PDF Fase 2, seção Infraestrutura como Código

### ~~BKL-055: Expandir pipeline CI/CD com Docker build e K8s deploy~~ (RESOLVIDO 2026-06-29)

- **Arquivo:** `.github/workflows/ci.yml`
- **Problema:** Pipeline atual só faz Build, Test e Quality Gate. Falta: build da imagem Docker, push para registry, deploy no K8s, deploy do banco, aplicação de manifestos YAML.
- **Correção:** Adicionar stages ao pipeline:
  - Stage 4: `docker build` + `docker push` para Docker Hub ou GHCR
  - Stage 5: `kubectl apply` dos manifestos K8s ou Terraform apply
  - Stage 6: Verificação de deploy (healthcheck)
- **Origem:** PDF Fase 2, seção CI/CD

### BKL-056: Atualizar README.md para Fase 2

- **Arquivo:** `README.md`
- **Problema:** README referencia "Fase 1" (`POSTECH 15SOAT — Tech Challenge Fase 1`). Faltam seções sobre K8s, Terraform e link do vídeo.
- **Correção:**
  - Atualizar referência para "Fase 2"
  - Adicionar seção "Deploy no Kubernetes" com instruções
  - Adicionar seção "Provisionamento com Terraform" com instruções
  - Adicionar link do vídeo demonstrativo
- **Origem:** PDF Fase 2, seção Entregáveis

### BKL-057: Pipeline de CI para feature branches (build + test sem SonarCloud)

- **Arquivo:** `.github/workflows/` (novo arquivo `ci-branch.yml` ou condição no `ci.yml`)
- **Problema:** O pipeline atual só roda em `main` e `develop`. Branches de feature não têm validação automática de build e testes antes do PR, aumentando o risco de regressões serem descobertas tarde. O SonarCloud não deve ser executado em branches de feature para evitar consumo desnecessário de análises.
- **Correção:** Criar workflow dedicado `.github/workflows/ci-branch.yml` acionado em `push` para qualquer branch que **não** seja `main` ou `develop`, com:
  - Stage 1 — **Build**: `mvn clean compile -B -ntp -DskipTests`
  - Stage 2 — **Test**: `mvn clean verify -B -ntp` com Testcontainers + upload de relatórios JaCoCo/Surefire
  - **Sem** Quality Gate (SonarCloud) — economia de análises e tokens
  - PR comment com status de testes (via `actions/upload-artifact` + `github-script`)
- **Benefício (DevSecOps):** Feedback rápido ao desenvolvedor no próprio push da branch; erros capturados antes do PR para `develop`.
- **Origem:** Melhoria de processo — Shift-Left CI

### ~~BKL-058: Smoke Tests após build e após deploy~~ (RESOLVIDO 2026-06-29)

- **Arquivo:** `.github/workflows/ci.yml`, nova pasta `src/test/kotlin/.../smoke/` ou script shell
- **Problema:** O pipeline não possui nenhuma validação de sanidade funcional. Após o build, não há confirmação de que o JAR inicializa corretamente. Após o deploy em K8s, não há verificação de que a aplicação está respondendo e os endpoints críticos estão acessíveis.
- **Correção (duas etapas):**
  - **Smoke após Build** — Stage intermediário entre `test` e `quality`: subir o JAR via `java -jar` (com PostgreSQL via Testcontainers ou H2) e verificar:
    - `/actuator/health` retorna `{"status": "UP"}`
    - `/actuator/info` retorna HTTP 200
    - Timeout de 30s para startup; falha o pipeline se não subir
  - **Smoke após Deploy** — Stage final do pipeline (após `kubectl apply`): usando `curl` ou script de health-check contra a URL do serviço K8s:
    - `GET /actuator/health` → `200 UP`
    - `GET /actuator/health/readiness` → `200 ACCEPTING_TRAFFIC`
    - `POST /auth/login` com credencial de smoke → `200` (valida stack JWT + banco)
    - Retry com backoff exponencial (3 tentativas, 10s de intervalo)
- **Benefício (QA + DevSecOps):** Detecta falhas de startup, configuração de banco, variáveis de ambiente e roteamento K8s antes de promover o artefato.
- **Origem:** Melhoria de qualidade — Shift-Left Testing + pós-deploy validation

### BKL-059: Atualização de status da OS via e-mail

- **Diretório:** `/src/.../notifications/` (a ser implementado)
- **Problema:** O PDF exige o envio de e-mail ao cliente na atualização de status da OS. Atualmente não há notificação assíncrona funcional para troca de status.
- **Correção:** Implementar o envio de e-mail (ex: Spring Boot Starter Mail) ao alterar o status, consumindo as configurações do Mailpit.
- **Origem:** PDF Fase 2 (Evolução da aplicação) + Especialistas

### ~~BKL-060: Revisão de Refatoração Clean Code e Arquitetura Hexagonal~~ (RESOLVIDO 2026-06-29)

- **Problema:** A Fase 2 requer refatoração do código existente para garantir aderência total ao Clean Code e Arquitetura Hexagonal.
- **Correção:** O Arquiteto e o Tech Lead devem auditar os Bounded Contexts, garantir que as dependências apontam para o domínio e aplicar boas práticas do Kotlin.
- **Origem:** PDF Fase 2 (Evolução da aplicação) + Arquiteto

### ~~BKL-061: Revisão do Dockerfile e docker-compose (DevSecOps)~~ (RESOLVIDO 2026-06-29)

- **Problema:** O PDF requer Dockerfile e docker-compose revisados.
- **Correção:** O Eng. DevSecOps deve auditar o Dockerfile para otimizações (multi-stage) e least privilege (non-root user), e garantir segurança nas imagens locais.
- **Origem:** PDF Fase 2 (Infraestrutura) + DevSecOps

### BKL-062: Ampliação da Cobertura de Testes Automatizados (Integração e E2E)

- **Problema:** Necessário cobrir os fluxos críticos da API (Abertura, Consulta, Aprovação e Listagem de OS) com testes de integração e E2E.
- **Correção:** O Eng. QA deve definir e implementar testes nos novos fluxos da Fase 2, garantindo prevenção de regressões.
- **Origem:** PDF Fase 2 (Evolução da aplicação) + QA Engineer

---

## Resumo de status

| Status | Qtd |
|---|---|
| Resolvido | 38 |
| **Resolvido (Fase 2)** | **9** |
| Excluído / Epic | 2 |
| Pendente (Backlog original) | 10 |
| **Pendente (Fase 2)** | **4** |
| **Total** | **63** |

### Tabela de Tarefas Pendentes — Fase 2 (Requisitos do PDF & Especialistas)

| Tarefa | Prioridade | Categoria | Descrição Curta |
|---|:---:|---|---|
| BKL-056 | 🟡 Média | Docs | Atualização do README para Fase 2 |
| BKL-057 | 🟡 Média | CI/CD | Pipeline leve para feature branches (build + test, sem SonarCloud) |
| BKL-059 | 🔴 Alta | API | Atualização de status da OS via e-mail |
| BKL-062 | 🟡 Média | Testes | Ampliação da Cobertura de Testes Automatizados |

### Tabela de Tarefas Prontas — Fase 2

| Tarefa | Motivo da Resolução | Data |
|---|---|---|
| ~~BKL-050~~ | Ordenação por prioridade implementada no ServiceOrderRepository | 2026-06-29 |
| ~~BKL-051~~ | Ordenação temporal (enterTime ASC) implementada no ServiceOrderRepository | 2026-06-29 |
| ~~BKL-052~~ | Exclusão lógica (NOT IN) implementada no ServiceOrderRepository | 2026-06-29 |
| ~~BKL-053~~ | Manifestos YAML criados na pasta `/k8s` | 2026-06-29 |
| ~~BKL-054~~ | Scripts Terraform criados na pasta `/infra/terraform/cloud` | 2026-06-29 |
| ~~BKL-055~~ | Pipeline CI/CD com docker build, push, terraform e deploy configurado em `ci.yml` | 2026-06-29 |
| ~~BKL-058~~ | Smoke Tests após build e start via compose configurados no pipeline | 2026-06-29 |
| ~~BKL-060~~ | Refatoração de Clean Code / Clean Architecture / Hexagonal confirmada pelo time | 2026-06-29 |
| ~~BKL-061~~ | Boas práticas de DevSecOps aplicadas corretamente nos arquivos | 2026-06-29 |

### Tabela de Tarefas Pendentes (Análise de Sentido — Backlog Original)

| Tarefa | Faz sentido? | Motivo | Link |
|---|:---:|---|---|
| BKL-029 | ✅ Sim | Transformar `BasicExecution` em tabela permite cadastrar serviços sem necessidade de deploy no código. | [BKL-029](#bkl-029-basicexecution-deveria-ser-tabela-ao-inves-de-enum) |
| BKL-039 | ✅ Sim | Zero logging na aplicação dificulta imensamente debugging e rastreabilidade em produção. | [BKL-039](#bkl-039-zero-logging-em-toda-a-aplicacao) |
| BKL-041 | ⚠️ Analisar | Correlation ID é útil, mas menos crítico em monolito que em microsserviços. Depende do padrão desejado. | [BKL-041](#bkl-041-sem-correlation-id-para-rastreamento-de-requests) |
| BKL-042 | ✅ Sim | Ausência de testes em Domain Services recém criados (`ServiceOrder`, `Execution`, etc). | [BKL-042](#bkl-042-metodos-de-dominio-novos-sem-testes-diretos) |
| BKL-044 | ✅ Sim | AttributeConverters sem testes de fluxo com banco de dados. | [BKL-044](#bkl-044-attributeconverters-sem-testes-documentconverter-emailconverter-plateconverter) |
| BKL-045 | ✅ Sim | `Email` Value Object sem cobertura para validar regras de regex. | [BKL-045](#bkl-045-email-value-object-sem-testes-de-validacao) |
| BKL-046 | ✅ Sim | Nenhum teste de integração que valide o fluxo completo no PostgreSQL via Testcontainers. | [BKL-046](#bkl-046-zero-testes-de-integracao-com-banco-real) |
| BKL-047 | ✅ Sim | Garantir que o Dashboard financeiro reflete a realidade dos novos status automáticos. | [BKL-047](#bkl-047-auditoria-da-rota-de-métricas-getmetrics) |
| BKL-048 | ✅ Sim | Evitar manipulação inconsistente de estados após o faturamento/aprovação. | [BKL-048](#bkl-048-proteção-contra-retrocesso-de-status-no-update) |
| BKL-049 | ✅ Sim | Invariante de negócio crítica para garantir a imutabilidade de dados históricos e financeiros. | [BKL-049](#bkl-049-imutabilidade-de-entidades-finalizadaspagas) |

### Tabela de Tarefas Resolvidas

| Tarefa | Motivo da Resolução | Data |
|---|---|---|
| [BKL-001](#bkl-001-mismatch-execution-service_order_id-vs-schema-service_order) | JoinColumn corrigido para `name = "service_order"` | 2026-04-27 |
| [BKL-002](#bkl-002-mismatch-serviceorder-createdatupdatedat-vs-schema-createdupdated) | Campos renomeados para `created`/`updated` | 2026-04-27 |
| [BKL-003](#bkl-003-logica-invertida-em-customerservice-create) | Substituído por verificação de duplicata | 2026-04-27 |
| [BKL-004](#bkl-004-jwt-secret-hardcoded-no-applicationproperties) | Variáveis de ambiente configuradas no properties | 2026-04-28 |
| [BKL-005](#bkl-005-credenciais-de-banco-hardcoded) | Variáveis de ambiente configuradas no properties | 2026-04-28 |
| [BKL-007](#bkl-007-jwtservicevalidatetoken-engole-todas-as-excecoes) | Captura de `JwtException` específica | 2026-04-28 |
| [BKL-008](#bkl-008-endpoints-services-insumes-e-invoices-sem-autorizacao-por-role) | Adicionadas regras de autorização no SecurityConfig | 2026-04-28 |
| [BKL-009](#bkl-009-userservice-verifyregisteredcustomer-nao-verifica-role-customer) | Adicionada verificação de `UserRole.CUSTOMER` | 2026-04-27 |
| [BKL-010](#bkl-010-serviceordermetricsservicegetmetrics-carrega-todos-os-registros-em-memoria) | Uso de query nativa com agregação no banco | 2026-04-28 |
| [BKL-011](#bkl-011-document-init-usa-require-com-throw-interno-redundante) | Substituído por `if (...) throw` explícito | 2026-04-27 |
| [BKL-012](#bkl-012-invoiceservicecreate-usa-require-ao-inves-de-exception-de-dominio) | Substituído por `InvalidStateTransitionException` | 2026-04-27 |
| [BKL-013](#bkl-013-convencao-de-timestamps-misturada-entre-entidades) | Padronizado com @CreationTimestamp e @UpdateTimestamp | 2026-04-28 |
| [BKL-014](#bkl-014-executionhistory-usa-uuid-direto-vs-serviceorderhistory-usa-manytoone) | Padronizado para @ManyToOne em ambas as entidades | 2026-04-28 |
| [BKL-015](#bkl-015-safestring-no-campo-password-rejeita-caracteres-validos) | `@SafeString` removido do campo password | 2026-04-28 |
| [BKL-016](#bkl-016-falta-safestring-no-invoicenumber) | `@SafeString` adicionado em `invoiceNumber` | 2026-04-28 |
| [BKL-017](#bkl-017-insumeservicegetentitybyid-sem-transactional) | Adicionado `@Transactional(readOnly = true)` | 2026-04-27 |
| [BKL-018](#bkl-018-customerserviceverifyandtakebyemail--nome-confuso) | Renomeado para `findByEmailOrThrow` | 2026-04-27 |
| [BKL-019](#bkl-019-enums-postgresql-criados-no-schema-mas-nao-utilizados) | Migration V2 de Drop de enums não utilizados | 2026-04-27 |
| [BKL-020](#bkl-020-wildcard-import-com-ponto-e-virgula-estilo-java) | Remoção de wildcard imports e estilo Java | 2026-04-27 |
| [BKL-021](#bkl-021-imports-nao-utilizados-em-varias-classes) | Limpeza de imports via análise estática | 2026-04-27 |
| [BKL-022](#bkl-022-complianceexception-usada-para-regras-de-negocio-simples) | Substituído por exceções de domínio mais semânticas | 2026-04-28 |
| [BKL-023](#bkl-023-executionservice-usa-alias-de-import-para-service) | Removido alias atípico em favor de FQN ou padrão | 2026-04-28 |
| [BKL-024](#bkl-024-bounded-context-register-dependia-de-serviceorder-direcao-errada) | Inversão de dependência via interfaces e adapters | 2026-04-27 |
| [BKL-025](#bkl-025-executionservice-acessava-repositories-de-outros-bounded-contexts) | Acesso via serviços de domínio e interfaces seguras | 2026-04-27 |
| [BKL-026](#bkl-026-serviceorderservice-construia-entidades-de-outro-bounded-context) | Lógica movida para Aggregates (Domain-Driven) | 2026-04-27 |
| [BKL-027](#bkl-027-modelo-de-dominio-anemico) | Comportamento movido para Entidades de Domínio | 2026-04-27 |
| [BKL-028](#bkl-028-value-objects-acoplados-ao-jpa) | Uso de AttributeConverters para desacoplar JPA | 2026-04-27 |
| [BKL-030](#bkl-030-executionservice-ainda-dispara-save-no-serviceorder-filho-atualiza-pai) | Responsabilidade de persistência movida para o Pai | 2026-04-28 |
| [BKL-031](#bkl-031-sonarqube--collection-deveria-ser-imutavel-no-metricsservice) | Uso de `.toList()` para garantir imutabilidade | 2026-04-28 |
| [BKL-033](#bkl-033-sem-validacao-de-que-diagnostico-produziu-executions-antes-de-avancar-para-waiting_approval) | Validação `executions.isEmpty()` adicionada no domínio | 2026-04-28 |
| [BKL-034](#bkl-034-insume-deductstock-aceita-quantidades-negativas-e-zero) | Proteção `amount <= 0` adicionada no Insume | 2026-04-28 |
| [BKL-035](#bkl-035-insumeitemrequest-quantity-sem-validacao-de-minimo) | `@field:Min(1)` adicionado ao DTO de Insumo | 2026-04-28 |
| [BKL-036](#bkl-036-mesmo-insume-pode-ser-adicionado-duas-vezes-na-mesma-execution) | Validação de duplicidade em `addInsume()` | 2026-04-28 |
| [BKL-037](#bkl-037-approvaldomainservice-instanciado-manualmente-ao-inves-de-injetado) | Registro como `@Component` e injeção de dependência | 2026-04-28 |
| [BKL-038](#bkl-038-executionservice-manipula-colecao-interna-do-serviceorder-diretamente) | Encapsulamento via Aggregate Root | 2026-04-28 |
| [BKL-040](#bkl-040-sem-spring-boot-actuator-health-checks-info) | Actuator configurado para observabilidade básica | 2026-04-28 |
| [BKL-043](#bkl-043-approvaldomainservice-sem-testes) | Criada suíte `ApprovalDomainServiceTest.kt` | 2026-04-29 |

### Tabela de Tarefas Excluídas / Descontinuadas

| Tarefa | Motivo da Exclusão | Data |
|---|---|---|
| [BKL-006](#bkl-006-registro-aberto-permite-criar-attendant-sem-restricao) | Transformado em Épico de Autenticação Externa (Keycloak) | 2026-04-28 |
| [BKL-032](#bkl-032-os-pode-ser-criada-sem-nenhum-servico-lista-vazia) | Regra de Negócio: OS pode nascer vazia para diagnóstico posterior | 2026-04-28 |
