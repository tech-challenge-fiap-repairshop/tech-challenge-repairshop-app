# MIGRATION-DDD+CLEAN.md — Plano de Refatoracao DDD-First com Clean Architecture

## Context

O projeto Repair Shop e um MVP de oficina mecanica (Kotlin + Spring Boot 4.x) que sera avaliado pela aderencia ao DDD. A analise linha a linha revelou que, embora a estrutura de modulos reflita bounded contexts corretos e os enums de estado machine estejam bem modelados, o codigo sofre de **modelo de dominio anemico**, **violacoes de fronteira entre contextos**, e **logica de negocio concentrada em services ao inves de aggregates**. Este plano corrige esses problemas com DDD como prioridade #1.

### Especialistas consultados

| Especialista | Foco | Agentes cognitivos de suporte |
|---|---|---|
| DDD Estrategico | Bounded Contexts, Context Mapping, Linguagem Ubiqua | `domain-expert`, `architect` |
| DDD Tatico | Aggregates, Entities, Value Objects, Invariantes | `modeler`, `reviewer` |
| Clean Architecture | Dependency Rule, Ports & Adapters | Nenhum (novo vies) |
| Clean Code / SOLID | SRP, nomes, funcoes, tratamento de erros | `spring-kotlin` (parcial) |
| Spring/Kotlin Idiomatico | Kotlin idioms, Spring Boot 4.x patterns | `spring-kotlin` |

### Grafo de dependencias atual (problema)

```
register.CustomerService --> serviceorder.ServiceOrderService  (DIRECAO ERRADA)
register.VehicleService  --> serviceorder.ServiceOrderService  (DIRECAO ERRADA)
serviceorder.ServiceOrderService --> register.CustomerService, register.VehicleService, inventory.InsumeService
serviceorder.ServiceOrderService cria execution.Execution entities  (CRUZA BC)
execution.ExecutionService --> inventory.InsumeRepository (BYPASSA SERVICE, CRUZA BC)
execution.ExecutionService --> serviceorder.ServiceOrderRepository (BYPASSA SERVICE, CRUZA BC)
payment.InvoiceService --> serviceorder.ServiceOrderService, register.CustomerService
```

### Grafo de dependencias alvo (apos refatoracao)

```
serviceorder --> register (via interfaces definidas em serviceorder)
serviceorder --> inventory (via interfaces definidas em serviceorder)
execution --> serviceorder (via interface definida em execution)
execution --> inventory (via interface definida em execution)
payment --> serviceorder (customer acessivel via serviceOrder.customer)
register tem ZERO dependencias externas
inventory tem ZERO dependencias externas
```

---

## Fase 1: Enriquecer Modelo de Dominio

**Objetivo:** Eliminar o anti-pattern Anemic Domain Model. Aggregates devem proteger suas proprias invariantes.

---

### 1A. Enriquecer ServiceOrder (Aggregate Root)

**Arquivo:** `serviceorder/entity/ServiceOrder.kt`

#### O que foi analisado

O `ServiceOrder` e o Aggregate Root do bounded context "Ordem de Servico". Atualmente a entidade possui apenas dois metodos de dominio: `advanceStatus(newStatus)` que valida transicoes de estado, e `recalculateTotalPrice(servicePrices)` que recalcula o preco total. Todo o resto da logica de negocio — criacao de historico, verificacao de pre-condicoes para finalizacao, gerenciamento de executions, workflow de aprovacao — vive em `ServiceOrderService` (188 linhas).

**Codigo atual analisado:**

1. **Historico duplicado em ServiceOrderService** (linhas 106-123 e 144-155): o calculo de intervalo (`ChronoUnit.SECONDS.between`) e a criacao de `ServiceOrderHistory` aparecem identicos em `advanceStatus()` e `approve()`. Essa logica pertence ao aggregate, pois o historico e uma invariante da OS — toda transicao de estado DEVE gerar um registro de historico.

2. **Checagem de finalizacao em ServiceOrderService** (linhas 101-103): `if (newStatus == FINALIZED) { val allFinalized = order.executions.all { it.status == ExecutionStatus.FINALIZED } }`. Essa e uma invariante do aggregate — a OS so pode ser finalizada se todos os servicos estao finalizados. Nao faz sentido que o service valide uma regra que depende exclusivamente do estado interno do aggregate.

3. **endTime setado no service** (linhas 109-110): `if (newStatus == FINALIZED) order.endTime = LocalDateTime.now()`. O aggregate deveria controlar seus proprios timestamps de ciclo de vida.

4. **Aprovacao/Recusa em ServiceOrderService** (linhas 129-158): o metodo `approve()` mistura logica de dominio (transicao de estado + historico) com logica de aplicacao (deducao de estoque). O aggregate deveria encapsular a transicao; o service deveria orquestrar a interacao com o contexto de inventario.

5. **Criacao manual de Execution em ServiceOrderService** (linhas 52-75): o service constroi `Execution`, `ExecutionInsume` e `ExecutionInsumeId` manualmente. O aggregate root deveria controlar como child entities sao adicionados (Factory Method pattern do DDD).

6. **recalculateTotalPrice recebe parametro externo** (linha 77): `order.recalculateTotalPrice(order.executions.map { it.price })`. O aggregate ja possui a colecao `executions` internamente — passar os precos como parametro e redundante e viola encapsulamento.

7. **Campos createdAt/updatedAt** (linhas 71-76): usam `@CreationTimestamp`/`@UpdateTimestamp` com nomes `createdAt`/`updatedAt`, mas a coluna no banco e `created`/`updated` (V1__create_schema.sql linhas 82-83). O JPA mapeia camelCase para snake_case automaticamente, gerando `created_at`/`updated_at` — colunas que NAO existem. **Bug de runtime** (BKL-002).

8. **Import wildcard com ponto e virgula** (linha 12): `import jakarta.persistence.*;` — sintaxe Java, nao Kotlin (BKL-020).

#### Por que precisa ser modificado

**Anti-pattern: Anemic Domain Model.** Segundo Eric Evans (Domain-Driven Design, Cap. 5), o Aggregate Root e responsavel por proteger todas as invariantes do aggregate. Quando a logica de negocio vive no service, o aggregate vira um "data bag" — qualquer service pode manipular seu estado de formas inconsistentes. Isso viola o principio fundamental do DDD de que o dominio e a fonte de verdade.

**Principio violado: Tell, Don't Ask.** O service "pergunta" ao aggregate sobre seu estado (`order.executions.all { ... }`) e toma decisoes com base nisso, ao inves de "dizer" ao aggregate o que fazer e deixa-lo validar internamente.

**Duplicacao de logica:** O calculo de historico aparece em 2 lugares no service. Se a regra de calculo de intervalo mudar, e necessario lembrar de atualizar ambos.

#### Decisao tecnica

Adicionar os seguintes metodos ao aggregate:

| Metodo | Responsabilidade | Justificativa DDD |
|---|---|---|
| `recordHistory(newStatus)` | Cria ServiceOrderHistory com calculo de intervalo | Invariante: toda transicao gera historico. Elimina duplicacao no service. |
| `advanceStatus(newStatus)` enriquecido | Inclui checagem de finalizacao, setar endTime, e chamar recordHistory | Aggregate protege suas proprias pre-condicoes e efeitos colaterais internos. |
| `approve()` | Transicao especifica para APPROVED | Expressividade: linguagem ubiqua ("aprovar OS") traduzida em metodo de dominio. |
| `refuse()` | Transicao especifica para REFUSED | Idem. |
| `addExecution(...)` | Factory method para child entity Execution | Aggregate Root controla adicao de child entities (DDD Factory Method). |
| `collectInsumeRequirements()` | Retorna List<Pair<UUID, Int>> | Permite que o service orquestre deducao de estoque SEM o aggregate conhecer o contexto de inventario. |
| `recalculateTotalPrice()` sem parametro | Deriva de `executions.map { it.price }` internamente | Encapsulamento: aggregate usa seus proprios dados. |

Corrigir `createdAt`/`updatedAt` para `created`/`updated` com `@Column(name = "created")` explicito. Substituir import wildcard.

---

### 1B. Enriquecer Execution (Entity dentro do aggregate ServiceOrder)

**Arquivo:** `execution/entity/Execution.kt`

#### O que foi analisado

A entidade `Execution` ja possui um metodo `advanceStatus(newStatus)` que valida transicoes, mas todo o gerenciamento de historico e vinculacao de insumos vive em `ExecutionService`:

1. **Historico de execution em ExecutionService** (linhas 127-143): o service busca o ultimo historico via `executionHistoryRepository.findByExecutionIdOrderByRegisterTimeAsc()`, calcula o intervalo, cria `ExecutionHistory` e salva via repository. Essa logica e identica a do ServiceOrder — o historico e uma invariante da execution, nao uma preocupacao do service.

2. **Vinculacao de insumos em ExecutionService** (linhas 159-173 `attachInsumesToExecution`): o service constroi `ExecutionInsumeId` e `ExecutionInsume` manualmente e adiciona a colecao. A entity deveria encapsular essa operacao.

3. **Mismatch de JoinColumn** (linha 24): `@JoinColumn(name = "service_order_id")`, mas a coluna no banco e `service_order` (V1__create_schema.sql linha 97). **Bug de runtime** (BKL-001).

#### Por que precisa ser modificado

**Mesmo anti-pattern da 1A:** logica que pertence a entity esta no service. O `ExecutionService` "sabe" como construir o historico de uma execution — se uma nova forma de criar executions surgir, o historico pode ser esquecido.

**Encapsulamento quebrado:** qualquer chamador pode manipular `execution.insumes` diretamente sem passar pelo metodo de dominio, potencialmente criando ExecutionInsume com IDs inconsistentes.

**Bug critico:** o mismatch de JoinColumn impede persistencia/carregamento de Execution.

#### Decisao tecnica

| Metodo | Responsabilidade | Justificativa DDD |
|---|---|---|
| `recordHistory(newStatus)` | Cria ExecutionHistory com calculo de intervalo | Invariante: toda transicao gera historico. |
| `advanceStatus(newStatus)` enriquecido | Chamar recordHistory apos transicao | Entity protege seu ciclo de vida completo. |
| `addInsume(insume, quantity)` | Encapsula criacao de ExecutionInsume/ExecutionInsumeId | Encapsulamento: entity controla sua colecao interna. |

Corrigir JoinColumn para `name = "service_order"`.

---

### 1C. Enriquecer Customer

**Arquivo:** `register/entity/Customer.kt`

#### O que foi analisado

A entidade `Customer` e um puro data holder — nenhum metodo de dominio. A atualizacao de campos acontece em `CustomerService.update()` (linhas 82-87):

```kotlin
customer.name = request.name
customer.email = email
customer.phone = request.phone
customer.birthDate = request.birthDate
customer.updated = LocalDateTime.now()
```

O service acessa setters individualmente e e responsavel por lembrar de atualizar o timestamp `updated`.

#### Por que precisa ser modificado

**Anti-pattern: Anemic Domain Model.** Customer nao expressa nenhuma operacao de dominio. Na linguagem ubiqua, "atualizar dados do cliente" e uma acao de dominio — deveria existir como metodo na entity.

**Risco de inconsistencia:** se outro service ou metodo atualizar campos do Customer sem chamar `customer.updated = LocalDateTime.now()`, o timestamp fica desatualizado. Encapsular garante que a invariante (atualizacao de timestamp) e sempre respeitada.

#### Decisao tecnica

Adicionar `updateDetails(name, email, phone, birthDate)` que encapsula mutacao de campos e atualiza `updated`. O service passa a chamar um unico metodo ao inves de manipular 5 campos individualmente.

---

### 1D. Enriquecer Vehicle

**Arquivo:** `register/entity/Vehicle.kt`

#### O que foi analisado

Mesma situacao do Customer — puro data holder. A atualizacao de campos esta em `VehicleService.update()` (linhas 72-78), com 6 setters individuais e atualizacao manual de `updated`.

Alem disso, `Vehicle.kt` importa `Document` e `Email` (linhas 3-5) que nao sao utilizados em nenhum lugar da classe.

#### Por que precisa ser modificado

Mesma justificativa do 1C: anti-pattern anemico, risco de inconsistencia de timestamp. Os imports nao utilizados sao code smell que indica copia de codigo sem limpeza.

#### Decisao tecnica

Adicionar `updateDetails(plate, brand, model, color, manufacturingDate)`. Remover imports nao utilizados.

---

## Fase 2: Corrigir Violacoes de Bounded Context

**Objetivo:** Cada contexto so acessa outros contextos via interfaces (Dependency Inversion a servico do DDD).

---

### 2A. Inverter dependencia register --> serviceorder

#### O que foi analisado

**CustomerService.kt** (linhas 23-27 — construtor):
```kotlin
class CustomerService(
    private val customerRepository: CustomerRepository,
    private val serviceOrderService: ServiceOrderService,  // <-- PROBLEMA
    private val userService: UserService
)
```

O unico uso de `serviceOrderService` em CustomerService e na linha 96:
```kotlin
if (serviceOrderService.existsByCustomer(customer))
    throw ComplianceException(ErrorMessages.Customer.HAS_SERVICE_ORDERS)
```

**VehicleService.kt** (linhas 22-26 — construtor): mesma situacao, usa `serviceOrderService.existsByVehicleId(vehicle)` apenas no delete (linha 92).

O modulo `register` (Cadastro) depende do modulo `serviceorder` (Ordem de Servico) para verificar se pode deletar um customer/vehicle.

#### Por que precisa ser modificado

**Violacao de Bounded Context — direcao errada de dependencia.** No DDD, o contexto de Cadastro e upstream (fornece dados) e o contexto de Ordem de Servico e downstream (consome dados). A dependencia deveria fluir de serviceorder -> register, nao o contrario.

Na documentacao de event-storming (`context/event-storming.md`), o fluxo e: Cliente e cadastrado -> OS e criada referenciando o cliente. O cadastro nao sabe e nao deveria saber sobre OSs.

**Referencia DDD:** Eric Evans, Cap. 14 (Context Mapping) — quando dois bounded contexts precisam se comunicar, a dependencia deve fluir na direcao correta. O pattern aplicavel e **Customer-Supplier**: serviceorder (Supplier) fornece a informacao de existencia para register (Customer).

**Risco pratico:** Se `ServiceOrderService` for refatorado ou sua assinatura mudar, `CustomerService` quebra — um modulo de cadastro nao deveria ser afetado por mudancas no modulo de OSs.

#### Decisao tecnica

Aplicar **Dependency Inversion** via interface:

```
ANTES:  register.CustomerService --> serviceorder.ServiceOrderService
DEPOIS: register.CustomerService --> register.domain.ServiceOrderExistenceChecker (interface)
                                     ^
                                     | implementa
        serviceorder.service.ServiceOrderExistenceAdapter
```

**Novos arquivos:**
- `register/domain/ServiceOrderExistenceChecker.kt` — interface com `existsByCustomerId(UUID): Boolean` e `existsByVehicleId(UUID): Boolean`
- `serviceorder/service/ServiceOrderExistenceAdapter.kt` — implementa a interface, delega para `ServiceOrderRepository`

**Arquivos modificados:**
- `CustomerService.kt` — substituir `ServiceOrderService` por `ServiceOrderExistenceChecker`
- `VehicleService.kt` — idem

A interface vive no modulo `register` (quem precisa), o adapter vive em `serviceorder` (quem fornece). O modulo register passa a ter ZERO imports de serviceorder.

---

### 2B. Eliminar acesso direto de ExecutionService a InsumeRepository

#### O que foi analisado

**ExecutionService.kt** (linhas 17-18 e 34 — imports e construtor):
```kotlin
import com.cao.repairshop.inventory.repository.InsumeRepository
...
private val insumeRepository: InsumeRepository,
```

Uso direto na linha 161:
```kotlin
val insume = insumeRepository.findById(itemRequest.insumeId)
    .orElseThrow { EntityNotFoundException(ErrorMessages.Insume.notFoundById(itemRequest.insumeId)) }
```

O modulo `execution` (Servico) acessa diretamente o repository do modulo `inventory` (Estoque), bypassando completamente o `InsumeService`.

#### Por que precisa ser modificado

**Violacao de Bounded Context — bypass de camada de servico.** O repository e um detalhe de implementacao do bounded context de Estoque. Quando o contexto de Servico acessa `InsumeRepository` diretamente, ele:

1. **Quebra o encapsulamento do contexto de Estoque:** qualquer mudanca na estrutura de persistencia do inventario (ex: mudar de JPA para cache) afeta o modulo de execution
2. **Ignora regras de negocio do inventario:** se `InsumeService` adicionar validacoes, logging, ou metricas no futuro, o `ExecutionService` nao sera afetado — inconsistencia silenciosa
3. **Cria acoplamento estrutural:** o modulo de execution "sabe" que insumes sao persistidos via JPA repository

**Referencia DDD:** `principles/boundaries.md` — contextos devem se comunicar via interfaces explicitas, nunca via acesso direto a detalhes internos.

**Nota:** O proprio `InsumeService` ja possui o metodo `getEntityById(id)` (linha 42-45) que faz exatamente o que o ExecutionService precisa. O bypass e desnecessario.

#### Decisao tecnica

Criar interface no modulo execution, implementada pelo modulo inventory:

**Novos arquivos:**
- `execution/domain/InsumeLookup.kt` — interface com `findById(UUID): Insume`
- `inventory/service/InsumeLookupAdapter.kt` — implementa, delega para `InsumeService.getEntityById()`

**Arquivo modificado:** `ExecutionService.kt` — substituir `insumeRepository` por `insumeLookup`

---

### 2C. Eliminar acesso direto de ExecutionService a ServiceOrderRepository

#### O que foi analisado

**ExecutionService.kt** (linhas 20 e 35):
```kotlin
import com.cao.repairshop.serviceorder.repository.ServiceOrderRepository
...
private val serviceOrderRepository: ServiceOrderRepository
```

Usos:
- Linha 150: `serviceOrderRepository.findById(id)` para carregar ServiceOrder
- Linha 187: `serviceOrderRepository.save(serviceOrder)` para salvar apos recalcular preco

O modulo `execution` acessa diretamente o repository do modulo `serviceorder`.

#### Por que precisa ser modificado

**Mesma violacao da 2B** — acesso direto ao detalhe de persistencia de outro bounded context. Agravante: o `ExecutionService` nao apenas le, mas tambem **salva** ServiceOrders via repository estrangeiro. Isso significa que o modulo de execution pode modificar o estado do aggregate ServiceOrder sem passar pelo ServiceOrderService, potencialmente violando invariantes do contexto de OS.

#### Decisao tecnica

**Novos arquivos:**
- `execution/domain/ServiceOrderAccessor.kt` — interface com `findById(UUID): ServiceOrder` e `save(ServiceOrder): ServiceOrder`
- `serviceorder/service/ServiceOrderAccessorAdapter.kt` — implementa, delega para `ServiceOrderRepository`

**Arquivo modificado:** `ExecutionService.kt` — substituir `serviceOrderRepository` por `serviceOrderAccessor`

---

### 2D. ServiceOrder assume controle da criacao de Executions

#### O que foi analisado

**ServiceOrderService.createServiceOrder()** (linhas 52-75):

```kotlin
request.services.forEach { sd ->
    val execution = Execution(
        serviceOrder = order,
        basicDescription = sd.basicDescription,
        fullDescription = sd.fullDescription,
        price = sd.price ?: BigDecimal.ZERO,
        estimatedTime = sd.estimatedTime
    )
    sd.insumes.forEach { itemRequest ->
        val insume = insumeService.getEntityById(itemRequest.insumeId)
        execution.insumes.add(
            ExecutionInsume(
                id = ExecutionInsumeId(execution.id, insume.id),
                execution = execution,
                insume = insume,
                quantity = itemRequest.quantity
            )
        )
    }
    order.executions.add(execution)
}
```

O ServiceOrderService (contexto de OS) constroi entidades `Execution`, `ExecutionInsume` e `ExecutionInsumeId` manualmente. Ele conhece a estrutura interna do contexto de Servico.

#### Por que precisa ser modificado

**Violacao de encapsulamento entre contextos.** O ServiceOrderService "sabe" como construir uma Execution com seus insumos — se a estrutura de Execution mudar (novo campo obrigatorio, nova invariante), o ServiceOrderService quebra.

**Referencia DDD:** O Aggregate Root (ServiceOrder) deve controlar como child entities sao adicionados ao aggregate. Esse e o pattern **Factory Method** do DDD (Evans, Cap. 6) — o aggregate fornece um metodo que garante que child entities sao criados de forma consistente.

**Nota:** A relacao ServiceOrder -> Execution e `CascadeType.ALL`, o que confirma que Execution e uma child entity dentro do aggregate ServiceOrder. O aggregate root TEM que controlar a criacao.

#### Decisao tecnica

Usar o metodo `addExecution()` criado na Fase 1A. O service passa a resolver insumes (preocupacao cross-context) e delegar a construcao ao aggregate:

```kotlin
request.services.forEach { sd ->
    val resolvedInsumes = sd.insumes.map { insumeService.getEntityById(it.insumeId) to it.quantity }
    order.addExecution(sd.basicDescription, sd.fullDescription, sd.price ?: BigDecimal.ZERO, sd.estimatedTime, resolvedInsumes)
}
```

O service mantem a responsabilidade de resolver referencias cross-context (insumes), mas delega a construcao ao aggregate.

---

### 2E. Simplificar ServiceOrderService

#### O que foi analisado

Apos as Fases 1A e 2D, `ServiceOrderService` (188 linhas) contem logica que agora pertence ao aggregate. Os metodos atuais sao longos e misturam orquestracao com logica de dominio:

- `createServiceOrder()`: ~45 linhas — resolve customer/vehicle, constroi executions manualmente, calcula preco, cria historico, salva
- `advanceStatus()`: ~30 linhas — carrega order, valida finalizacao, avanca estado, seta endTime, cria historico, salva
- `approve()`: ~30 linhas — carrega order, deduz estoque, avanca estado, cria historico, salva
- `existsByCustomer()` e `existsByVehicleId()`: metodos que so existem para servir o modulo register (dependencia invertida na Fase 2A)

#### Por que precisa ser modificado

**Anti-pattern: God Service / Fat Service.** O ServiceOrderService acumula responsabilidades de orquestracao, logica de dominio, e queries de existencia. Apos mover a logica de dominio para o aggregate (Fase 1A), o service deve se tornar um **Application Service** fino: carregar entidades, chamar metodos de dominio, orquestrar interacoes cross-context, e salvar.

**Principio SOLID violado: SRP (Single Responsibility Principle).** Um service nao deveria ser responsavel por regras de estado E orquestracao E queries de existencia.

#### Decisao tecnica

Apos as fases anteriores, cada metodo se reduz a:

| Metodo | Antes | Depois |
|---|---|---|
| `createServiceOrder()` | 45 linhas | Resolver customer/vehicle, `order.addExecution()` por servico, `order.recordHistory(RECEIVED)`, save |
| `advanceStatus()` | 30 linhas | Carregar order, `order.advanceStatus(newStatus)`, save |
| `approve()` | 30 linhas | Carregar order, `order.collectInsumeRequirements()` + `insumeService.deductStock()`, `order.approve()`, save |

Remover `existsByCustomer()` e `existsByVehicleId()` (movidos para adapter 2A).

---

### 2F. Simplificar ExecutionService

#### O que foi analisado

`ExecutionService` (189 linhas) tem os mesmos problemas: metodos privados `registerInitialHistory()` e `attachInsumesToExecution()` encapsulam logica que pertence a entity, e o `advanceStatus()` gerencia historico via `executionHistoryRepository` ao inves de delegar para a entity.

Alem disso, a dependencia de `executionHistoryRepository` (linhas 15-16 e 127-142) se torna desnecessaria quando a entity gerencia seu proprio historico — o `CascadeType.ALL` em `Execution.histories` garante que novas entradas de historico adicionadas a colecao serao persistidas automaticamente quando a Execution for salva.

#### Por que precisa ser modificado

**Mesma justificativa da 2E:** logica de dominio no service, service gordo. Apos enriquecer a entity (Fase 1B), os metodos privados se tornam redundantes.

**Sobre o executionHistoryRepository:** a entity `Execution` tem `@OneToMany(cascade = [CascadeType.ALL])` sobre `histories`. O `ExecutionHistory` tem `executionId: UUID` definido no construtor. Quando `execution.histories.add(newHistory)` e chamado e o `execution` e salvo via `executionRepository.save()`, o cascade persiste o historico automaticamente. O repository separado e desnecessario.

#### Decisao tecnica

| Mudanca | Detalhe |
|---|---|
| `advanceStatus()` | Chamar `execution.advanceStatus(newStatus)`, save. Remover `executionHistoryRepository` |
| `create()` | Usar `execution.addInsume()` e `execution.recordHistory()` |
| Remover | `registerInitialHistory()` e `attachInsumesToExecution()` — logica agora na entity |
| Remover dependencia | `executionHistoryRepository` do construtor |

---

## Fase 3: Desacoplar Value Objects do JPA

**Objetivo:** Domain objects nao devem carregar annotations de infraestrutura.

---

### 3A. Remover @Embeddable dos Value Objects

**Arquivos:** `register/domain/Document.kt`, `register/domain/Email.kt`, `register/domain/Plate.kt`

#### O que foi analisado

Os tres Value Objects do modulo register carregam `@Embeddable` do JPA:

**Document.kt** (linhas 7-8):
```kotlin
@Embeddable
data class Document(val value: String) {
```

**Email.kt** (linhas 5-6):
```kotlin
@Embeddable
data class Email(val value: String) {
```

**Plate.kt** (linhas 7-8):
```kotlin
@Embeddable
data class Plate(val value: String) {
```

Esses sao Value Objects de dominio — representam conceitos do negocio (documento CPF/CNPJ, email, placa) com validacao rica nos construtores. Porem, carregam uma annotation de infraestrutura (`jakarta.persistence.Embeddable`).

**Alem disso, Document.kt** (linhas 12-14) tem um anti-pattern Kotlin:
```kotlin
require(digits.length == 11 || digits.length == 14) {
    throw InvalidDocumentException(ErrorMessages.Document.INVALID_FORMAT)
}
```
O `require` do Kotlin espera um lambda que retorna uma String (a mensagem de erro). Ao usar `throw` dentro do lambda, a excecao e lancada ANTES do `require` processar — funciona por acidente mas e confuso e semanticamente errado (BKL-011).

#### Por que precisa ser modificado

**Violacao da Dependency Rule (Clean Architecture a servico do DDD).** Value Objects pertencem a camada de dominio — a camada mais interna. A camada de dominio NAO deve depender de frameworks de infraestrutura. Quando `Document` importa `jakarta.persistence.Embeddable`, o dominio depende do JPA. Se o projeto migrar para outro ORM ou mecanismo de persistencia, os Value Objects — que sao logica pura de negocio — precisariam ser alterados.

**Referencia DDD:** Evans (Cap. 5, Value Objects) — Value Objects devem ser imutaveis e conter apenas logica de dominio. Annotations de persistencia nao sao logica de dominio.

**Testabilidade:** com `@Embeddable`, testes unitarios dos Value Objects precisam de classpath com JPA. Sem a annotation, sao POJOs puros — testaveis sem framework.

**Nota pragmatica:** os Value Objects ja estao bem modelados (validacao no construtor, `normalized` property, `type` enum em Document). O problema e APENAS a annotation — a solucao e mover a preocupacao de persistencia para converters.

#### Decisao tecnica

- Remover `@Embeddable` e import `jakarta.persistence.Embeddable` dos tres arquivos
- Corrigir `Document.kt`: substituir `require(...) { throw ... }` por `if (...) throw ...`

---

### 3B. Criar AttributeConverters (camada de persistencia)

#### O que foi analisado

Atualmente, as entidades `Customer` e `Vehicle` usam `@Embedded` + `@AttributeOverride` para mapear os Value Objects:

**Customer.kt** (linhas 33-34):
```kotlin
@Embedded
@AttributeOverride(name = "value", column = Column(name = "document", nullable = false, unique = true, length = 14))
var document: Document,
```

Esse approach requer que o Value Object seja `@Embeddable`. Ao remover essa annotation (Fase 3A), precisamos de outro mecanismo de persistencia.

#### Por que precisa ser modificado

O mecanismo `@Embedded`/`@Embeddable` cria acoplamento bidirecional: a entidade sabe que o VO e embeddable, e o VO sabe que e embeddable. `AttributeConverter` e unidirecional: apenas o converter (camada de persistencia) conhece ambos os lados.

#### Decisao tecnica

**Novos arquivos no pacote `register/repository/converter/`:**

| Arquivo | Converte | Para DB | Para Entity |
|---|---|---|---|
| `DocumentConverter.kt` | `Document <-> String` | `attribute.normalized` | `Document(dbData)` |
| `EmailConverter.kt` | `Email <-> String` | `attribute.value` | `Email(dbData)` |
| `PlateConverter.kt` | `Plate <-> String` | `attribute.normalized` | `Plate(dbData)` |

Todos usam `@Converter(autoApply = true)`. Os converters vivem no pacote `repository/converter/` — camada de persistencia, nao de dominio.

---

### 3C. Atualizar entidades Customer e Vehicle

#### O que foi analisado

Apos remover `@Embeddable` e criar converters, as entidades precisam trocar de mecanismo de mapeamento.

**Customer.kt** atual:
```kotlin
@Embedded
@AttributeOverride(name = "value", column = Column(name = "document", nullable = false, unique = true, length = 14))
var document: Document,
```

**Vehicle.kt** atual:
```kotlin
@Embedded
@AttributeOverride(name = "value", column = Column(name = "plate", nullable = false, unique = true, length = 7))
var plate: Plate,
```

#### Por que precisa ser modificado

Consequencia direta da Fase 3A — sem `@Embeddable`, o `@Embedded` falha. O converter substitui com SQL identico.

#### Decisao tecnica

Substituir `@Embedded @AttributeOverride(...)` por `@Convert(converter = XxxConverter::class) @Column(...)`. O schema SQL permanece identico — nenhuma migration necessaria. O banco continua vendo colunas `document VARCHAR(14)`, `email VARCHAR(255)`, `plate VARCHAR(7)`.

---

## Fase 4: Domain Services para logica multi-entidade

---

### 4A. ApprovalDomainService

**Novo:** `serviceorder/domain/ApprovalDomainService.kt`

#### O que foi analisado

O workflow de aprovacao em `ServiceOrderService.approve()` (linhas 129-158) envolve duas preocupacoes distintas:

1. **Transicao de estado do ServiceOrder:** `order.advanceStatus(APPROVED)` ou `order.advanceStatus(REFUSED)`
2. **Deducao de estoque de insumos:** iteracao sobre `order.executions.forEach { service -> service.insumes.forEach { item -> insumeService.deductStock(item.insume.id, item.quantity) } }`

Nenhuma entidade unica "possui" esse workflow completo:
- O ServiceOrder nao deveria conhecer o contexto de inventario (deducao de estoque)
- O Insume nao deveria conhecer o contexto de OS (aprovacao)

#### Por que precisa ser modificado

**Referencia DDD:** Evans (Cap. 5, Domain Services) — quando uma operacao de dominio nao pertence naturalmente a nenhuma entidade, ela deve ser modelada como Domain Service. O Domain Service e diferente do Application Service: ele encapsula **logica de dominio** que cruza entidades, enquanto o Application Service orquestra **infraestrutura** (carregar, salvar, chamar servicos externos).

Atualmente, `ServiceOrderService.approve()` mistura ambos: logica de dominio (decidir o que significa aprovar) + infraestrutura (carregar do banco, chamar InsumeService, salvar).

#### Decisao tecnica

```kotlin
class ApprovalDomainService {
    data class StockRequirement(val insumeId: UUID, val quantity: Int)

    fun approve(order: ServiceOrder): List<StockRequirement> {
        order.approve()  // transicao de estado (dominio)
        return order.collectInsumeRequirements()  // coleta requisitos (dominio)
            .map { (id, qty) -> StockRequirement(id, qty) }
    }

    fun refuse(order: ServiceOrder) {
        order.refuse()  // transicao de estado (dominio)
    }
}
```

O Application Service (`ServiceOrderService.approve()`) orquestra: carrega order -> chama domain service -> chama `insumeService.deductStock()` por item (infraestrutura) -> salva.

O `StockRequirement` e um DTO do domain service que permite ao Application Service fazer a deducao sem que o domain service conhea o `InsumeService`.

---

### 4B. Extrair ServiceOrderMetricsService

**Novo:** `serviceorder/service/ServiceOrderMetricsService.kt`

#### O que foi analisado

**ServiceOrderService.getMetrics()** (linhas 164-177):

```kotlin
fun getMetrics(): ServiceOrderMetricsResponse {
    val allOrders = serviceOrderRepository.findAll()  // CARREGA TUDO EM MEMORIA
    val executionSeconds = allOrders.mapNotNull { order ->
        val inExecution = order.histories.find { it.status == ServiceOrderStatus.IN_EXECUTION }
        val finalized = order.histories.find { it.status == ServiceOrderStatus.FINALIZED }
        if (inExecution != null && finalized != null) {
            ChronoUnit.SECONDS.between(inExecution.registerTime, finalized.registerTime)
        } else null
    }
    val avgMinutes = if (executionSeconds.isNotEmpty()) executionSeconds.average() / 60.0 else null
    return ServiceOrderMetricsResponse(averageExecutionTimeMinutes = avgMinutes)
}
```

Problemas identificados:
1. `findAll()` sem paginacao carrega TODOS os registros em memoria (BKL-010)
2. E um metodo de **leitura/reporting** no meio de um service de **escrita/comando**
3. Nao faz transicoes de estado nem modifica dados — e uma preocupacao completamente diferente

#### Por que precisa ser modificado

**CQRS-lite (Command Query Responsibility Segregation).** Metricas sao uma preocupacao de leitura (Query). O `ServiceOrderService` e responsavel por comandos (criar OS, avançar status, aprovar). Misturar queries complexas com comandos viola SRP e torna o service mais dificil de testar e manter.

**Performance:** `findAll()` em producao com milhares de OSs causa `OutOfMemoryError`. Em um service dedicado, pode ser otimizado com query nativa (`@Query` com agregacao SQL) sem poluir o service principal.

#### Decisao tecnica

Extrair para `ServiceOrderMetricsService` dedicado. Modificar `ServiceOrderController.kt` para injetar o novo service no endpoint `/metrics`. Futuramente, esse service pode usar query nativa para agregar no banco.

---

## Fase 5: Higiene DDD e Bugs do BACKLOG

---

### 5A. Corrigir CustomerService.create (BKL-003)

**Arquivo:** `register/service/CustomerService.kt`

#### O que foi analisado

**CustomerService.create()** (linhas 30-49):
```kotlin
fun create(request: CreateCustomerRequest): Customer {
    val document = Document(request.document)
    val email = Email(request.email)
    userService.verifyRegisteredCustomer(request.email)
    customerRepository.findByDocument(document)?.let {
        throw DuplicateEntityException(ErrorMessages.Customer.DUPLICATE_DOCUMENT)
    }
    verifyAndTakeByEmail(email)  // <-- PROBLEMA: lanca EntityNotFoundException se NAO existir
    ...
}
```

O metodo `verifyAndTakeByEmail` (linhas 53-55):
```kotlin
fun verifyAndTakeByEmail(email: Email): Customer =
    customerRepository.findByEmail(email)
        ?: throw EntityNotFoundException(ErrorMessages.Customer.NOT_FOUND)
```

Na criacao de um novo customer, o email NAO deveria existir. Mas `verifyAndTakeByEmail` lanca excecao se o email NAO existir — logica invertida. Resultado: so e possivel criar customer se ja existir outro com o mesmo email.

#### Por que precisa ser modificado

**Bug de logica que impede o fluxo principal da aplicacao.** A criacao de customer e o primeiro passo do fluxo de cadastro. Se esse metodo falha para emails novos, o sistema nao funciona.

#### Decisao tecnica

Substituir `verifyAndTakeByEmail(email)` por verificacao de duplicata:
```kotlin
customerRepository.findByEmail(email)?.let {
    throw DuplicateEntityException(ErrorMessages.Customer.DUPLICATE_EMAIL)
}
```

---

### 5B. Verificar role CUSTOMER em UserService (BKL-009)

**Arquivo:** `user/service/UserService.kt`

#### O que foi analisado

**UserService.verifyRegisteredCustomer()** (linhas 56-58):
```kotlin
fun verifyRegisteredCustomer(email: String): User =
    userRepository.findByEmail(email)
        ?: throw EntityNotFoundException(ErrorMessages.Customer.USER_NOT_FOUND_FOR_EMAIL)
```

O metodo verifica se existe um user com o email, mas NAO verifica se o user tem role `CUSTOMER`. Um user com role `ATTENDANT` seria aceito como customer. A mensagem `ErrorMessages.Customer.USER_NOT_CUSTOMER_ROLE` (definida em ErrorMessages.kt linha 13) nunca e usada.

#### Por que precisa ser modificado

**Regra de negocio nao implementada.** O sistema define dois roles (CUSTOMER, ATTENDANT) e o cadastro de customer deveria ser restrito a users com role CUSTOMER. Sem essa verificacao, um atendente pode ser cadastrado como cliente, violando a separacao de papeis definida no dominio.

#### Decisao tecnica

Adicionar verificacao apos encontrar o user:
```kotlin
val user = userRepository.findByEmail(email)
    ?: throw EntityNotFoundException(ErrorMessages.Customer.USER_NOT_FOUND_FOR_EMAIL)
if (user.function != UserRole.CUSTOMER)
    throw ComplianceException(ErrorMessages.Customer.USER_NOT_CUSTOMER_ROLE)
return user
```

---

### 5C. Corrigir InvoiceService — usar exception de dominio (BKL-012)

**Arquivo:** `payment/service/InvoiceService.kt`

#### O que foi analisado

**InvoiceService.create()** (linhas 30-32):
```kotlin
require(serviceOrder.status == ServiceOrderStatus.FINALIZED) {
    "Cannot create invoice: service order status must be FINALIZED, but was ${serviceOrder.status}."
}
```

`require` lanca `IllegalArgumentException` (HTTP 400 Bad Request). Porem, o Swagger da `InvoiceApi` (linha 26) documenta `422 Unprocessable Entity` para esse caso. Alem disso, todo o resto do projeto usa `InvalidStateTransitionException` para regras de transicao de estado.

#### Por que precisa ser modificado

**Inconsistencia no tratamento de erros.** O `GlobalExceptionHandler` tem handler para `InvalidStateTransitionException` que retorna 422 (UNPROCESSABLE_ENTITY). Usar `require` bypassa esse handler e retorna 400 (BAD_REQUEST), inconsistente com a documentacao e com o padrao do projeto.

#### Decisao tecnica

Substituir por:
```kotlin
if (serviceOrder.status != ServiceOrderStatus.FINALIZED)
    throw InvalidStateTransitionException("Cannot create invoice: service order status must be FINALIZED, but was ${serviceOrder.status}.")
```

---

### 5D. Adicionar @Transactional em InsumeService.getEntityById (BKL-017)

**Arquivo:** `inventory/service/InsumeService.kt`

#### O que foi analisado

**InsumeService.getEntityById()** (linhas 42-45):
```kotlin
fun getEntityById(id: UUID): Insume {
    return insumeRepository.findById(id)
        .orElseThrow { EntityNotFoundException(ErrorMessages.Insume.notFoundById(id)) }
}
```

Todos os outros metodos de leitura no projeto usam `@Transactional(readOnly = true)`. Este nao tem annotation nenhuma.

#### Por que precisa ser modificado

**Inconsistencia e risco de performance.** `readOnly = true` permite ao Hibernate otimizar o flush mode e ao pool de conexoes usar replicas de leitura (se configurado). Sem a annotation, o metodo pode abrir uma transacao de escrita desnecessariamente, dependendo da propagacao do chamador.

#### Decisao tecnica

Adicionar `@Transactional(readOnly = true)`.

---

### 5E. Renomear verifyAndTakeByEmail para findByEmailOrThrow (BKL-018)

**Arquivo:** `register/service/CustomerService.kt`

#### O que foi analisado

O metodo `verifyAndTakeByEmail` (linha 53) tem nome que sugere side-effect ("take") mas e read-only. "Verify" sugere boolean, "Take" sugere mutacao. O metodo faz nenhum dos dois — apenas busca e lanca excecao se nao encontrar.

#### Por que precisa ser modificado

**Clean Code — nomes devem revelar intencao.** O nome atual confunde o leitor sobre o que o metodo faz. `findByEmailOrThrow` segue o padrao ja usado no projeto (`findById` + `orElseThrow`).

#### Decisao tecnica

Renomear para `findByEmailOrThrow`. Atualizar todos os chamadores (`VehicleService.kt` linha 37).

---

### 5F. Remover enums SQL nao utilizados (BKL-019)

**Arquivo:** Nova migration `V2__drop_unused_enums.sql`

#### O que foi analisado

**V1__create_schema.sql** (linhas 6-16):
```sql
CREATE TYPE status_os AS ENUM ('RECEIVED', 'IN_DIAGNOSIS', ...);
CREATE TYPE status_service AS ENUM ('INITIATED', 'PENDING', 'FINALIZED');
```

Esses tipos PostgreSQL sao criados mas NUNCA usados — todas as colunas de status usam `VARCHAR(30)` e `VARCHAR(20)`, nao o tipo enum.

#### Por que precisa ser modificado

**Codigo morto no schema.** Os tipos ocupam espaco no catalogo do banco e confundem quem lê o migration — parece que deveriam ser usados mas nao sao.

#### Decisao tecnica

Nova migration `V2__drop_unused_enums.sql`:
```sql
DROP TYPE IF EXISTS status_os;
DROP TYPE IF EXISTS status_service;
```

---

### 5G. Limpar imports nao utilizados (BKL-021)

#### O que foi analisado

- **Insume.kt** (linhas 4-5): importa `Execution` e `ServiceOrder` — nenhum dos dois e referenciado na classe
- **Vehicle.kt** (linhas 3-5): importa `Document` e `Email` — nao usados
- **VehicleRepository.kt** (linhas 5-7): importa `Document` e `Email` — nao usados

#### Por que precisa ser modificado

**Code smell.** Imports nao utilizados indicam codigo copiado sem limpeza, aumentam ruido visual, e podem causar conflitos de nome desnecessarios.

#### Decisao tecnica

Remover os imports nao utilizados.

---

## Fase 6: Atualizar e Criar Testes

---

### Testes existentes a atualizar

#### O que foi analisado

Os testes existentes usam MockK para mockar dependencias dos services. Apos as fases anteriores, as dependencias mudaram:

- `CustomerServiceTest` e `VehicleServiceTest` mockam `ServiceOrderService` — apos Fase 2A, passam a mockar `ServiceOrderExistenceChecker`
- `ExecutionServiceTest` mocka `InsumeRepository` e `ServiceOrderRepository` — apos Fases 2B e 2C, passa a mockar `InsumeLookup` e `ServiceOrderAccessor`
- `ExecutionServiceTest` mocka `ExecutionHistoryRepository` — apos Fase 2F, essa dependencia nao existe mais

#### Por que precisa ser modificado

**Testes devem refletir o contrato real do service.** Se o service nao depende mais de `ServiceOrderService`, o teste nao deve mocka-lo. Testes com mocks errados dao falsa confianca.

#### Decisao tecnica

| Teste | Mudanca |
|---|---|
| `CustomerServiceTest` | Substituir mock de `ServiceOrderService` por `ServiceOrderExistenceChecker` |
| `VehicleServiceTest` | Idem |
| `ExecutionServiceTest` | Substituir mocks de `InsumeRepository`/`ServiceOrderRepository` por `InsumeLookup`/`ServiceOrderAccessor`. Remover mock de `ExecutionHistoryRepository` |
| `ServiceOrderServiceTest` | Verificar metodos de dominio no aggregate ao inves de logica no service |

---

### Novos testes de dominio

#### O que foi analisado

Atualmente existem testes para `ServiceOrderStatus` (transicoes) e `ExecutionStatus` (transicoes), mas nao existem testes para os metodos de dominio das entidades — porque eles nao existiam. Apos as Fases 1A-1D, novos metodos de dominio precisam de cobertura.

#### Por que precisa ser modificado

**Testes de dominio sao os mais valiosos em DDD.** Eles validam as invariantes de negocio sem dependencia de framework. Sao rapidos, determinísticos e documentam o comportamento esperado do dominio.

#### Decisao tecnica

| Teste | Cenarios |
|---|---|
| `ServiceOrderDomainTest` | `advanceStatus()` com finalization check (todas executions FINALIZED vs nao), `approve()` em estado WAITING_APPROVAL, `refuse()` em estado WAITING_APPROVAL, `addExecution()` recalcula preco, `collectInsumeRequirements()` agrega corretamente, `recordHistory()` calcula intervalo, transicoes invalidas lancam exception |
| `ExecutionDomainTest` | `addInsume()` adiciona a colecao corretamente, `advanceStatus()` gera historico, transicoes invalidas lancam exception |
| `CustomerDomainTest` | `updateDetails()` atualiza todos os campos e timestamp |

---

## Ordem de Execucao

| Ordem | Fase | Dependencia | Arquivos criados | Arquivos modificados |
|-------|------|-------------|-----------------|---------------------|
| 1 | 1A+1B | nenhuma | — | ServiceOrder.kt, Execution.kt |
| 2 | 1C+1D | nenhuma | — | Customer.kt, Vehicle.kt |
| 3 | 2A | nenhuma | ServiceOrderExistenceChecker.kt, ServiceOrderExistenceAdapter.kt | CustomerService.kt, VehicleService.kt |
| 4 | 2B+2C | nenhuma | InsumeLookup.kt, InsumeLookupAdapter.kt, ServiceOrderAccessor.kt, ServiceOrderAccessorAdapter.kt | ExecutionService.kt |
| 5 | 2D+2E | 1A | — | ServiceOrderService.kt |
| 6 | 2F | 1B | — | ExecutionService.kt |
| 7 | 3A+3B+3C | nenhuma | DocumentConverter.kt, EmailConverter.kt, PlateConverter.kt | Document.kt, Email.kt, Plate.kt, Customer.kt, Vehicle.kt |
| 8 | 4A+4B | 1A | ApprovalDomainService.kt, ServiceOrderMetricsService.kt | ServiceOrderService.kt, ServiceOrderController.kt |
| 9 | 5 (bugs) | nenhuma | — | CustomerService.kt, UserService.kt, InvoiceService.kt, InsumeService.kt |
| 10 | 6 (testes) | todas | ServiceOrderDomainTest.kt, ExecutionDomainTest.kt, CustomerDomainTest.kt | todos os *Test.kt existentes |

---

## Verificacao

1. `./mvnw clean compile` — deve compilar sem erros apos cada fase
2. `./mvnw test` — todos os testes devem passar apos fase 6
3. `docker-compose up` + testar endpoints via Swagger — comportamento identico ao atual
4. Validar que nenhum service importa repository de outro modulo (`grep -r "import.*repository" --include="*Service.kt"`)
5. Validar que nenhum value object importa `jakarta.persistence` (`grep -r "jakarta.persistence" register/domain/`)

---

## O que este plano NAO faz (e por que)

| Decisao | Justificativa |
|---|---|
| Nao cria pacotes `application/`, `infrastructure/`, `ports/` | A estrutura atual e suficiente. Interfaces vivem em `domain/`, adapters em `service/`. Criar pacotes extras aumenta complexidade sem ganho de DDD — o importante e a direcao das dependencias, nao a nomenclatura dos pacotes. |
| Nao implementa domain events | O projeto e um monolito sincrono com transacoes ACID. Domain events seriam over-engineering nesta etapa — o ganho de desacoplamento nao justifica a complexidade de event bus/handlers para um MVP. Pode ser adicionado quando houver necessidade real de eventual consistency ou microservices. |
| Nao altera o schema do banco | Todas as mudancas sao code-level. AttributeConverters produzem SQL identico ao @Embedded. Entity method additions nao impactam persistencia. Isso garante zero risco de regressao no banco. |
| Nao renomeia para portugues | A linguagem ubiqua (context/ubiquitous-language.md) usa PT-BR mas o codigo usa EN. Renomear seria breaking change massivo (endpoints, DTOs, testes, Swagger) sem ganho real de DDD — a ESTRUTURA alinhada aos bounded contexts e o que importa, nao a lingua dos identificadores. |
| Nao adiciona Phone/InvoiceNumber Value Objects | Seria desejavel (elimina Primitive Obsession) mas nao e critico para compliance DDD nesta etapa. Pode ser feito depois como melhoria incremental. |
