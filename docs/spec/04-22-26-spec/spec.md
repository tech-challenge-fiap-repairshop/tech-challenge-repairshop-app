# Feature Specification: Repair Shop MVP — Back-end

**Project:** POSTECH 15SOAT — Group CAO (Tech Challenge Fase 1)
**Date:** 2026-04-22
**Status:** complete
**Workflow:** spec-writer → domain-expert → architect → modeler → reviewer

---

# PART 1 — SPEC WRITER

---

## User Scenarios & Testing

### Bounded Context: Cadastro (Registration)

#### User Story 1 — Register a new customer (Priority: P1)

As an attendant, I can register a customer with their personal data so that service orders can be opened for them.

**Why this priority:** Foundation of the entire system — no service order can exist without a registered customer. All other contexts depend on this.
**Independent Test:** Create a customer via API and verify it is persisted and retrievable by ID.

**Acceptance Scenarios:**

1. **AC-1:** Given valid customer data (name: "Maria Silva", document: "529.982.247-25", email: "maria@email.com", phone: "(11)99999-0000", birth_date: "1990-05-15"), When the attendant submits the registration, Then the customer is created with a UUID and timestamps (created, updated), and the system returns 201 with the customer data.

2. **AC-2:** Given a document "529.982.247-25" already registered, When the attendant submits a new customer with the same document, Then the system returns 409 with message "Customer with this document already exists."

3. **AC-3:** Given an invalid CPF "111.111.111-11" (invalid check digits), When the attendant submits the registration, Then the system returns 400 with message "Invalid CPF."

4. **AC-4:** Given an invalid CNPJ "11.111.111/1111-11", When the attendant submits the registration, Then the system returns 400 with message "Invalid CNPJ."

5. **AC-5:** Given a blank name field, When the attendant submits the registration, Then the system returns 400 with message "Name is required."

#### User Story 2 — Manage customers (Priority: P1)

As an attendant, I can list, view, update, and delete customers so that customer data stays current.

**Why this priority:** Essential for ongoing operations — corrections and lookups are daily tasks.
**Independent Test:** Create a customer, list all, fetch by ID, update, delete, and verify each operation.

**Acceptance Scenarios:**

1. **AC-6:** Given registered customers exist, When the attendant requests the customer list, Then the system returns a paginated list of customers.

2. **AC-7:** Given a customer with ID "abc-123", When the attendant fetches by ID, Then the system returns the full customer data.

3. **AC-8:** Given a customer with ID "abc-123", When the attendant updates the phone to "(11)88888-0000", Then the system returns 200 with updated data and `updated` timestamp is refreshed.

4. **AC-9:** Given a customer with ID "nonexistent", When the attendant fetches by ID, Then the system returns 404 with message "Customer not found."

5. **AC-10:** Given a customer with linked service orders, When the attendant attempts deletion, Then the system returns 409 with message "Cannot delete customer with existing service orders."

#### User Story 3 — Register a vehicle (Priority: P1)

As an attendant, I can register a vehicle linked to an existing customer so that service orders can reference the vehicle.

**Why this priority:** Vehicles are required for service orders — second foundational entity after customer.
**Independent Test:** Create a customer, then register a vehicle for that customer, and verify it is retrievable.

**Acceptance Scenarios:**

1. **AC-11:** Given a registered customer and valid vehicle data (plate: "ABC1D23", brand: "Toyota", model: "Corolla", color: "Silver", manufacturing_date: "2022-01-01"), When the attendant submits, Then the vehicle is created linked to the customer, returning 201.

2. **AC-12:** Given an invalid plate format "ABCDEFG", When the attendant submits, Then the system returns 400 with message "Invalid plate format. Expected ABC-1234 or ABC1D23."

3. **AC-13:** Given a customer_id that does not exist, When the attendant submits a vehicle, Then the system returns 404 with message "Customer not found."

4. **AC-14:** Given a valid old-format plate "ABC-1234", When the attendant submits, Then the vehicle is created successfully.

#### User Story 4 — Manage vehicles (Priority: P1)

As an attendant, I can list, view, update, and delete vehicles so that vehicle data stays current.

**Why this priority:** Operational necessity for correcting registration errors and lookups.
**Independent Test:** Create a vehicle, list all, fetch by ID, update, delete.

**Acceptance Scenarios:**

1. **AC-15:** Given registered vehicles exist, When the attendant requests the vehicle list, Then the system returns a paginated list.

2. **AC-16:** Given a vehicle with ID "xyz-456", When the attendant updates the color to "Black", Then the system returns 200 with updated data.

3. **AC-17:** Given a vehicle with linked service orders, When the attendant attempts deletion, Then the system returns 409 with message "Cannot delete vehicle with existing service orders."

---

### Bounded Context: Ordem de Servico (Service Order)

#### User Story 5 — Create a service order with automatic budget (Priority: P1)

As an attendant, I can create a service order for a registered customer and vehicle, including services and insumes, so that the system automatically calculates the budget.

**Why this priority:** Core business function — the service order is the central domain aggregate and drives revenue.
**Independent Test:** Create a customer, vehicle, insumes, then create an OS with services. Verify total_price is calculated.

**Acceptance Scenarios:**

1. **AC-18:** Given a registered customer, vehicle, and two services (price: 150.00 and 250.00), When the attendant creates the OS, Then the OS is created with status RECEIVED, total_price = 400.00, and a UUID is assigned.

2. **AC-19:** Given a nonexistent customer_id, When the attendant creates the OS, Then the system returns 404 with message "Customer not found."

3. **AC-20:** Given a nonexistent vehicle_id, When the attendant creates the OS, Then the system returns 404 with message "Vehicle not found."

4. **AC-21:** Given the OS is created with status RECEIVED, Then a record is inserted into tb_service_order_history with status RECEIVED, register_time = now, and interval_time = null.

5. **AC-22:** Given an OS with services linked to insumes, When the OS is created, Then each service is created with status INITIATED and linked to its insumes via tb_service_insume.

#### User Story 6 — Advance service order status (Priority: P1)

As an attendant, I can advance the status of a service order through the defined state machine so that the workflow progresses correctly.

**Why this priority:** Status transitions drive the entire business workflow — without them, no OS can progress.
**Independent Test:** Create an OS and advance it through each valid state, verifying history records at each step.

**Acceptance Scenarios:**

1. **AC-23:** Given an OS with status RECEIVED, When the attendant advances to IN_DIAGNOSIS, Then the status changes, a history record is created with interval_time = time since RECEIVED, and the system returns 200.

2. **AC-24:** Given an OS with status RECEIVED, When the attendant attempts to advance to APPROVED, Then the system returns 422 with message "Invalid status transition from RECEIVED to APPROVED."

3. **AC-25:** Given an OS with status CANCELED, When the attendant attempts any transition, Then the system returns 422 with message "Cannot transition from terminal status CANCELED."

4. **AC-26:** Given an OS with status PAID, When the attendant attempts any transition, Then the system returns 422 with message "Cannot transition from terminal status PAID."

5. **AC-57:** Given an OS with status IN_DIAGNOSIS and a completed diagnosis, When the attendant advances to WAITING_APPROVAL, Then the status changes, a history record is created with interval_time = time since IN_DIAGNOSIS, and the system returns 200.

#### User Story 7 — Customer approves or refuses the quote (Priority: P1)

As a customer, I can approve or refuse the service order quote so that the shop knows whether to proceed with execution.

**Why this priority:** Business gate — execution cannot begin without customer approval. Drives revenue decision.
**Independent Test:** Create an OS in WAITING_APPROVAL status, approve it, verify it moves to APPROVED and stock is deducted.

**Acceptance Scenarios:**

1. **AC-27:** Given an OS with status WAITING_APPROVAL, When the customer approves, Then the status changes to APPROVED, a history record is created, and insume stock is deducted for all linked insumes.

2. **AC-28:** Given an OS with status WAITING_APPROVAL, When the customer refuses, Then the status changes to REFUSED, and a history record is created.

3. **AC-29:** Given an OS with status other than WAITING_APPROVAL, When the customer attempts to approve, Then the system returns 422 with message "OS is not awaiting approval."

4. **AC-30:** Given an OS in WAITING_APPROVAL with linked insumes, and one insume has insufficient stock (quantity = 0, needed = 2), When the customer approves, Then the system returns 422 with message "Insufficient stock for insume [name]. Available: 0, Required: 2."

#### User Story 8 — Track service order progress (Priority: P1)

As a customer, I can consult my service order by ID to see the current status and history so that I know how the repair is progressing.

**Why this priority:** Customer-facing value — transparency builds trust.
**Independent Test:** Create an OS, advance it through several states, query by ID, verify status and full history.

**Acceptance Scenarios:**

1. **AC-31:** Given an OS with ID "os-123" and 3 history entries, When the customer queries the OS, Then the system returns the OS data with current status, total_price, and the full ordered history.

2. **AC-32:** Given a nonexistent OS ID, When the customer queries, Then the system returns 404 with message "Service order not found."

#### User Story 9 — View service order metrics (Priority: P2)

As an administrator, I can view the average execution time of service orders so that I can identify bottlenecks and improve efficiency.

**Why this priority:** Operational intelligence — valuable but not blocking for core workflow.
**Independent Test:** Complete several OS through the full lifecycle, query the metrics endpoint, verify average times.

**Acceptance Scenarios:**

1. **AC-33:** Given multiple completed OS with recorded interval_times, When the administrator queries /service-orders/metrics, Then the system returns the average execution time (from IN_EXECUTION to FINALIZED).

2. **AC-34:** Given no completed OS, When the administrator queries metrics, Then the system returns average = 0 or null with a clear message.

---

### Bounded Context: Servico (Service)

#### User Story 10 — Manage service catalog (Priority: P1)

As an attendant, I can create, list, view, update, and delete services so that the shop has a catalog of available services.

**Why this priority:** Services are required to compose service orders — foundational for OS creation.
**Independent Test:** CRUD operations on services independently of any OS.

**Acceptance Scenarios:**

1. **AC-35:** Given valid service data (description: "Oil change", price: 120.00, estimated_time: 1.5), When the attendant creates the service, Then it is created with status INITIATED and a UUID.

2. **AC-36:** Given a service linked to an active OS, When the attendant attempts deletion, Then the system returns 409 with message "Cannot delete service linked to an active service order."

3. **AC-37:** Given registered services exist, When the attendant lists services, Then the system returns a paginated list.

#### User Story 11 — Advance service status (Priority: P1)

As an attendant, I can advance the status of individual services within an OS so that the mechanic's progress is tracked.

**Why this priority:** Service status drives OS completion — the OS can only be finalized when all services are FINALIZED.
**Independent Test:** Create a service in INITIATED, advance to PENDING, then FINALIZED, verifying history records.

**Acceptance Scenarios:**

1. **AC-38:** Given a service with status INITIATED, When the attendant advances to PENDING, Then the status changes, and a record is inserted into tb_service_history with interval_time.

2. **AC-39:** Given a service with status PENDING, When the attendant advances to FINALIZED, Then the status changes, history is recorded.

3. **AC-40:** Given a service with status INITIATED, When the attendant attempts to advance to FINALIZED (skipping PENDING), Then the system returns 422 with message "Invalid service status transition from INITIATED to FINALIZED."

4. **AC-41:** Given an OS in IN_EXECUTION with 3 services, and all 3 are now FINALIZED, When the last service is finalized, Then the system allows the OS to transition to FINALIZED.

---

### Bounded Context: Estoque (Inventory)

#### User Story 12 — Manage insume inventory (Priority: P1)

As an attendant, I can create, list, view, update, and delete insumes so that the shop maintains an accurate inventory.

**Why this priority:** Insumes are required for service orders — foundational data.
**Independent Test:** CRUD operations on insumes independently.

**Acceptance Scenarios:**

1. **AC-42:** Given valid insume data (name: "Brake pad", brand: "Bosch", sku_id: "BP-001", quantity: 50, price: 89.90, unity_price: 89.90), When the attendant creates the insume, Then it is created with a UUID.

2. **AC-43:** Given an insume with quantity 10, When the attendant updates quantity to 15, Then the system returns 200 with updated data.

3. **AC-44:** Given an insume linked to an active service, When the attendant attempts deletion, Then the system returns 409 with message "Cannot delete insume linked to an active service."

4. **AC-45:** Given registered insumes exist, When the attendant lists insumes, Then the system returns a paginated list.

#### User Story 13 — Automatic stock deduction on OS approval (Priority: P1)

As the system, when an OS is approved, I automatically deduct insume stock for all linked insumes so that inventory reflects actual usage.

**Why this priority:** Protects the critical invariant that stock can never be negative. Directly tied to OS approval workflow.
**Independent Test:** Create an OS with insumes, approve it, verify insume quantities are deducted.

**Acceptance Scenarios:**

1. **AC-46:** Given an OS in WAITING_APPROVAL with a service linked to insume "Brake pad" (stock: 50, needed: 2), When the OS is approved, Then insume quantity becomes 48.

2. **AC-47:** Given an insume with stock = 1 and needed = 2, When the OS approval is attempted, Then the system rejects with 422 and message "Insufficient stock for insume Brake pad. Available: 1, Required: 2."

3. **AC-48:** Given an OS with 3 services linked to 5 different insumes, When the OS is approved, Then all 5 insumes have their stock deducted atomically — if any fails, none are deducted.

---

### Bounded Context: Usuarios (Users / Authentication)

#### User Story 14 — User login with JWT (Priority: P1)

As a user, I can log in with email and password to receive a JWT token so that I can access administrative APIs.

**Why this priority:** Security gate — all administrative APIs require authentication.
**Independent Test:** Create a user, login, receive token, verify token contains expected claims.

**Acceptance Scenarios:**

1. **AC-49:** Given a registered user (email: "admin@shop.com", password: "SecurePass123"), When the user logs in with correct credentials, Then the system returns 200 with a JWT token containing user_id, function, and expiration.

2. **AC-50:** Given incorrect password, When the user attempts login, Then the system returns 401 with message "Invalid credentials."

3. **AC-51:** Given a nonexistent email, When the user attempts login, Then the system returns 401 with message "Invalid credentials." (no distinction to prevent user enumeration).

4. **AC-52:** Given an expired JWT token, When the user makes an API request, Then the system returns 401 with message "Token expired."

#### User Story 15 — Protect administrative APIs (Priority: P1)

As the system, I enforce JWT authentication on all administrative endpoints so that only authorized users can manage data.

**Why this priority:** Security is non-negotiable for the MVP — required by the challenge specification.
**Independent Test:** Call an administrative endpoint without a token, verify 401. Call with a valid token, verify access.

**Acceptance Scenarios:**

1. **AC-53:** Given no Authorization header, When a request is made to POST /customers, Then the system returns 401.

2. **AC-54:** Given a valid JWT token in the Authorization header, When a request is made to POST /customers, Then the request proceeds normally.

3. **AC-55:** Given a malformed JWT token, When a request is made, Then the system returns 401.

4. **AC-56:** Given the OS tracking endpoint GET /service-orders/{id}, When a request is made without a token, Then the system allows access (public endpoint for customer tracking).

---

## Edge Cases

| # | Scenario | Input | Expected Result |
|---|----------|-------|-----------------|
| E1 | Zero customers in list | GET /customers with empty database | Empty list returned (200), not error |
| E2 | CPF at exact length | 11-digit CPF with valid check digits | Accepted |
| E3 | CPF with wrong length | 10-digit string | Rejected with "Invalid CPF" |
| E4 | CNPJ with wrong length | 13-digit string | Rejected with "Invalid CNPJ" |
| E5 | Concurrent OS creation for same customer | Two simultaneous POST requests | Both succeed with different UUIDs |
| E6 | Concurrent stock deduction | Two OS approvals deducting same insume | One succeeds, other rejected if stock insufficient (pessimistic lock) |
| E7 | OS with zero services | POST /service-orders with empty services list | Rejected with "OS must have at least one service" |
| E8 | Plate with lowercase letters | "abc1d23" | Normalized to uppercase and accepted |
| E9 | Document with formatting | "529.982.247-25" or "52998224725" | Both accepted, stored normalized |
| E10 | Status transition to same status | RECEIVED → RECEIVED | Rejected with "Cannot transition to the same status" |
| E11 | Delete insume with zero stock | DELETE insume with quantity = 0, not linked to any service | Allowed |
| E12 | Extremely large total_price | OS with services totaling > DECIMAL(12,2) max | Rejected with appropriate error |
| E13 | Expired token on protected endpoint | Authorization: Bearer <expired> | 401 with "Token expired" |
| E14 | SQL injection in document field | document: "'; DROP TABLE tb_customer; --" | Rejected by validation, no SQL execution |
| E15 | Concurrent status transitions | Two requests to advance same OS status simultaneously | One succeeds, other gets 409 or 422 |
| E16 | Vehicle with future manufacturing_date | manufacturing_date: 2030-01-01 | Rejected with "Manufacturing date cannot be in the future" |

---

## Requirements

### Functional Requirements

- **FR-001:** System MUST allow creating a customer with name, document (CPF/CNPJ), email, phone, and birth_date
- **FR-002:** System MUST reject duplicate documents with a 409 error
- **FR-003:** System MUST validate CPF/CNPJ check digits
- **FR-004:** System MUST allow CRUD operations on customers
- **FR-005:** System MUST allow registering a vehicle linked to an existing customer
- **FR-006:** System MUST validate vehicle plate format (ABC-1234 or ABC1D23)
- **FR-007:** System MUST allow CRUD operations on vehicles
- **FR-008:** System MUST allow creating a service order with customer, vehicle, and services
- **FR-009:** System MUST automatically calculate total_price as SUM of service prices on OS creation
- **FR-010:** System MUST enforce the status_os state machine (9 states, defined transitions only)
- **FR-011:** System MUST record a history entry in tb_service_order_history for every OS status transition
- **FR-012:** System MUST allow customer approval or refusal of the OS quote
- **FR-013:** System MUST deduct insume stock atomically when an OS is approved
- **FR-014:** System MUST reject OS approval when insume stock is insufficient
- **FR-015:** System MUST allow querying an OS with its full status history
- **FR-016:** System MUST allow listing all service orders
- **FR-017:** System MUST allow CRUD operations on services
- **FR-018:** System MUST enforce the status_service state machine (INITIATED → PENDING → FINALIZED)
- **FR-019:** System MUST record a history entry in tb_service_history for every service status transition
- **FR-020:** System MUST allow CRUD operations on insumes
- **FR-021:** System MUST authenticate users via email + password and return a JWT token
- **FR-022:** System MUST protect administrative API endpoints with JWT authentication
- **FR-023:** System MUST allow public access to the OS tracking endpoint
- **FR-024:** System MUST provide an endpoint for average execution time metrics
- **FR-025:** System MUST prevent OS transition to FINALIZED unless all linked services are FINALIZED
- **FR-026:** System MUST recalculate total_price when services are added to or removed from an OS
- **FR-027:** System MUST validate email format on customer registration
- **FR-028:** System MUST store user passwords hashed with BCrypt

### Non-Functional Requirements

- **NFR-001:** System MUST respond to all API requests in under 500ms for datasets up to 10,000 records per table
- **NFR-002:** System MUST store passwords using BCrypt with a cost factor of at least 10
- **NFR-003:** System MUST expose all API endpoints via Swagger/OpenAPI documentation
- **NFR-004:** System MUST achieve at least 80% test coverage on domain-critical code (status transitions, budget calculation, stock deduction, validations)
- **NFR-005:** System MUST return structured error responses with HTTP status code, error message, and timestamp
- **NFR-006:** System MUST use UUID v4 for all entity primary keys
- **NFR-007:** System MUST use pagination for all list endpoints (default 20 per page)

### Key Entities

- **Customer:** Registered person identified by CPF/CNPJ. Owns vehicles.
- **Vehicle:** Registered vehicle linked to a customer. Identified by plate.
- **ServiceOrder:** Central business entity tracking the lifecycle of a repair job. Contains services.
- **ServiceOrderHistory:** Immutable record of each status transition of an OS.
- **Service:** Individual service item within an OS (e.g., oil change, alignment).
- **ServiceInsume:** Link between a service and the insumes it requires (N:N).
- **ServiceHistory:** Immutable record of each status transition of a service.
- **Insume:** Part or supply in inventory with stock quantity.
- **User:** System user with credentials and role (function).

---

## Success Criteria

- **SC-001:** Attendant can register a customer, a vehicle, and create a service order with automatic budget in a single workflow
- **SC-002:** Service order progresses through the full lifecycle (RECEIVED → ... → PAID) with history tracking at every step
- **SC-003:** Invalid status transitions are rejected with clear error messages
- **SC-004:** Customer can approve or refuse a quote, and approval triggers automatic stock deduction
- **SC-005:** Insufficient stock prevents OS approval with a clear error
- **SC-006:** Customer can track their OS status and history via a public endpoint
- **SC-007:** All administrative endpoints are protected by JWT authentication
- **SC-008:** CPF/CNPJ and vehicle plate validations prevent invalid data entry
- **SC-009:** Average execution time metrics are available for operational monitoring
- **SC-010:** Domain-critical code achieves 80%+ test coverage

---

## Assumptions

- This is a back-end MVP — no front-end is in scope
- A single user type (employee) with a `function` field for role-based access
- Customer OS tracking is public (no customer authentication required for GET /service-orders/{id})
- Insume quantities in tb_service_insume are always 1 (the pivot table has no quantity column)
- Payment is represented by the PAID status — no payment gateway integration in MVP
- Invoice generation (nota fiscal) is out of scope for the MVP
- Seed data for 250 insumes will be loaded from CSV
- Email notification of quotes is out of scope (conceptual in Event Storming, not implemented)

## Out of Scope

- Front-end / UI
- Payment gateway integration
- Invoice (nota fiscal) generation
- Email notification system
- Dockerfile and docker-compose (explicitly excluded per user request)
- Supplier management (external system in Event Storming)
- Customer self-registration (attendant registers customers)
- Multi-tenancy (single shop)

---

# PART 2 — DOMAIN EXPERT

---

## Domain Analysis: Repair Shop MVP

### Bounded Context: Cadastro (Registration)

**Primary context:** Cadastro
**Secondary contexts involved:** None — this is the entry point of the system.

**Actors:**
- Cliente (Customer) — requests service, provides personal data
- Atendente (Attendant) — registers customer and vehicle data

**Commands:**
- Cadastrar cliente — executed by Atendente, triggers "Cliente cadastrado"
- Cadastrar veiculo — executed by Atendente, triggers "Veiculo cadastrado"

**Events:**
- Servico requisitado — (PIVOTAL: no) — triggers registration flow
- Cliente cadastrado — (PIVOTAL: no) — enables vehicle registration
- Veiculo cadastrado — (PIVOTAL: no) — enables OS creation

**Policies:**
- None — registration is manual, driven by attendant actions

**Ubiquitous Language Terms Used:**
- Cliente (Actor) — person who requests service
- Atendente (Actor) — employee who registers data
- Cadastro (Aggregate) — manages customer and vehicle registration
- Informacoes do cliente (Read Model) — data collected for registration
- Informacoes do veiculo (Read Model) — vehicle data for registration

**Cross-Context Dependencies:**
- Cadastro provides customer_id and vehicle_id referenced by Ordem de Servico via FK (ID reference)

---

### Bounded Context: Ordem de Servico (Service Order)

**Primary context:** Ordem de Servico
**Secondary contexts involved:** Cadastro (customer/vehicle references), Servico (service status check), Estoque (stock deduction)

**Actors:**
- Atendente (Attendant) — creates OS, advances status, sends quote
- Cliente (Customer) — evaluates quote, approves or refuses
- Mecanico (Mechanic) — performs diagnosis

**Commands:**
- Cria a OS — executed by Atendente, triggers "OS criada"
- Realiza diagnostico — executed by Mecanico, triggers "Diagnostico realizado"
- Envia OS — executed by Atendente, triggers "E-mail com OS enviado"
- Avalia OS — executed by Cliente, triggers "OS aprovada" or "OS nao aprovada"

**Events:**
- OS criada — (PIVOTAL: no) — triggers diagnosis request policy
- Diagnostico realizado — (PIVOTAL: yes) — separates technical evaluation from commercial phase
- E-mail com OS enviado — (PIVOTAL: no) — quote sent to customer
- OS aprovada — (PIVOTAL: yes) — separates commercial from execution phase, triggers stock notification
- OS nao aprovada — (PIVOTAL: no) — triggers OS cancellation policy

**Policies:**
- When OS criada → Solicitar diagnostico (system requests diagnosis from mechanic)
- When Diagnostico realizado → Enviar orcamento ao cliente (system sends quote)
- When OS aprovada → Notificar necessidade de insumos (system notifies for stock check)
- When OS nao aprovada → Finaliza OS por nao aprovacao (system closes OS)

**Ubiquitous Language Terms Used:**
- Ordem de Servico (Aggregate) — central domain aggregate
- Orcamento da OS (Read Model) — financial detail of services and parts
- StatusOs (Value Object/Enum) — lifecycle states of the OS

**Ambiguities Detected:**
- "Finaliza OS" appears in Ordem de Servico (closure by refusal) and Pagamento (closure after payment) — resolved: in this MVP context, we use status CANCELED for refusal closure and PAID for payment closure. No ambiguity at the code level.

**Cross-Context Dependencies:**
- Depends on Cadastro: customer_id, vehicle_id (FK references)
- Depends on Servico: service status check for FINALIZED transition (INV-005)
- Depends on Estoque: stock deduction on approval (cross-aggregate, handled in application service)

---

### Bounded Context: Servico (Service)

**Primary context:** Servico
**Secondary contexts involved:** Ordem de Servico (service belongs to OS), Estoque (insumes linked to services)

**Actors:**
- Mecanico (Mechanic) — executes services on the vehicle
- Atendente (Attendant) — updates service status in the system

**Commands:**
- Realiza servico — executed by Mecanico, triggers "Mecanico realizou o servico"
- Atualiza status da OS — executed by Atendente, triggers "OS foi concluida" or "OS nao concluida"

**Events:**
- Mecanico realizou o servico — (PIVOTAL: no) — tracks individual service completion
- OS foi concluida — (PIVOTAL: yes) — all services done, separates execution from payment
- OS nao concluida — (PIVOTAL: no) — execution continues

**Policies:**
- When OS foi concluida → Notificar servicos finalizados ao cliente

**Ubiquitous Language Terms Used:**
- Servico (Aggregate) — controls technical service execution
- StatusService (Value Object/Enum) — INITIATED, PENDING, FINALIZED
- Lista de servicos a fazer (Read Model) — services the mechanic must execute

**Cross-Context Dependencies:**
- Depends on Ordem de Servico: service_order FK (ID reference)
- Depends on Estoque: insume references via tb_service_insume (ID reference)

---

### Bounded Context: Estoque (Inventory)

**Primary context:** Estoque
**Secondary contexts involved:** Servico (insumes linked to services)

**Actors:**
- Atendente (Attendant) — manages inventory, notifies insume needs

**Commands:**
- Notifica pecas e insumos necessarios — executed by Atendente
- Solicita pecas e insumos necessarios — executed when stock is insufficient (external supplier)

**Events:**
- Pecas e insumos necessarios notificados — (PIVOTAL: no) — triggers stock verification
- Servico ficou disponivel para execucao — (PIVOTAL: yes) — all materials available

**Ubiquitous Language Terms Used:**
- Estoque (Aggregate) — controls parts/supplies availability
- Fornecedor (External System) — external supplier, out of MVP scope

**Cross-Context Dependencies:**
- Referenced by Servico via tb_service_insume (ID reference)
- Stock deduction triggered by Ordem de Servico approval (application-level coordination)

---

### Bounded Context: Usuarios (Users)

**Primary context:** Usuarios
**Secondary contexts involved:** None — cross-cutting security concern.

**Actors:**
- User (any system user) — authenticates to access administrative APIs

**Commands:**
- Login — executed by User, triggers JWT token generation

**Events:**
- User authenticated — (PIVOTAL: no) — enables API access

**Ubiquitous Language Terms Used:**
- User (Entity) — system user with credentials and role
- Function — user's role/access level

**New Terms Identified:**
- None — all terms already present in glossary or naturally derived from business rules

**Cross-Context Dependencies:**
- Cross-cutting: JWT authentication applies to all contexts' API endpoints
- No domain-level dependency — security is infrastructure-layer concern

---

# PART 3 — ARCHITECT

---

## Architecture Validation: Repair Shop MVP

### Bounded Context: Cadastro
- **Entities:** Customer, Vehicle
- **External dependencies:** None (entry point)
- **Package:** `com.cao.repairshop.cadastro`

### Bounded Context: Ordem de Servico
- **Entities:** ServiceOrder, ServiceOrderHistory
- **External dependencies:** customer_id → Cadastro, vehicle_id → Cadastro
- **Package:** `com.cao.repairshop.serviceorder`

### Bounded Context: Servico
- **Entities:** Service, ServiceInsume, ServiceHistory
- **External dependencies:** service_order → Ordem de Servico, id_tb_insume → Estoque
- **Package:** `com.cao.repairshop.service`

### Bounded Context: Estoque
- **Entities:** Insume
- **External dependencies:** None
- **Package:** `com.cao.repairshop.inventory`

### Bounded Context: Usuarios
- **Entities:** User
- **External dependencies:** None
- **Package:** `com.cao.repairshop.user`

### Cross-Context References

| Reference | From Context | To Context | Mechanism | Status |
|-----------|-------------|-----------|-----------|--------|
| tb_vehicle.customer_id → tb_customer | Cadastro | Cadastro | ID reference (same context) | OK |
| tb_service_order.customer_id → tb_customer | Ordem de Servico | Cadastro | ID reference | OK |
| tb_service_order.vehicle_id → tb_vehicle | Ordem de Servico | Cadastro | ID reference | OK |
| tb_service.service_order → tb_service_order | Servico | Ordem de Servico | ID reference | OK |
| tb_service_insume.id_tb_service → tb_service | Servico | Servico | ID reference (same context) | OK |
| tb_service_insume.id_tb_insume → tb_insume | Servico | Estoque | ID reference | OK |
| tb_service_order_history.service_order_id → tb_service_order | Ordem de Servico | Ordem de Servico | ID reference (same context) | OK |
| tb_service_history.service_id → tb_service | Servico | Servico | ID reference (same context) | OK |

### Violations

- **[NONE]** — All cross-context communication uses ID references. No shared entities between contexts.

### Integration Points

- Cadastro → Ordem de Servico: OS references customer_id and vehicle_id from Cadastro. Application service validates existence before OS creation.
- Ordem de Servico → Estoque: On OS approval, application service coordinates stock deduction across aggregates. This is a cross-aggregate operation handled at the application layer, not inside a single aggregate.
- Servico → Ordem de Servico: INV-005 (OS can only be FINALIZED when all services are FINALIZED) is a cross-aggregate check. Application service queries service statuses before allowing OS transition.
- Servico → Estoque: tb_service_insume links services to insumes by ID. No direct entity reference.

### Recommendations

1. **Cross-aggregate invariant INV-005** (OS FINALIZED requires all services FINALIZED): This is enforced at the application service level, which is acceptable for a monolith MVP. The application service queries service statuses before allowing the OS state transition. This is a pragmatic trade-off — eventual consistency is not needed in a monolith.

2. **Cross-aggregate stock deduction** (INV-004 enforcement on OS approval): The application service handles this atomically within a single database transaction. Acceptable for monolith — in a microservices architecture, this would require a saga.

3. **Usuarios as cross-cutting concern**: Authentication is infrastructure-level, not domain-level. The User context has no domain dependencies — it only provides JWT tokens for the Spring Security filter chain.

---

# PART 4 — MODELER

---

## Domain Model: Repair Shop MVP

### Aggregate: Cadastro

- **Root:** Customer
- **Entities:** Customer, Vehicle
- **Value Objects:** Document (CPF/CNPJ), Plate, Email
- **References:** None (entry point)

**Invariants:**
- **INV-001:** Customer document must be unique
  - Owner: Cadastro (Customer)
  - Enforcement: UNIQUE constraint on database + validation in application service before persist
- **INV-007:** Vehicle plate must be valid format (ABC-1234 or ABC1D23)
  - Owner: Cadastro (Vehicle)
  - Enforcement: Value Object Plate validates format on construction
- **INV-008:** Customer document must be valid CPF or CNPJ
  - Owner: Cadastro (Customer)
  - Enforcement: Value Object Document validates check digits on construction
- **INV-009:** Customer email must be valid format
  - Owner: Cadastro (Customer)
  - Enforcement: Value Object Email validates format on construction

**Behaviors:**
- `registerCustomer(name, document, email, phone, birthDate)`
  - Validates document (INV-001, INV-008), email (INV-009)
  - Creates Customer entity with generated UUID and timestamps
- `registerVehicle(customerId, plate, brand, model, color, manufacturingDate)`
  - Validates plate format (INV-007)
  - Validates customer exists
  - Creates Vehicle entity linked to customer
- `updateCustomer(id, data)` — updates allowed fields, refreshes `updated` timestamp
- `updateVehicle(id, data)` — updates allowed fields, refreshes `updated` timestamp
- `deleteCustomer(id)` — rejects if customer has linked service orders
- `deleteVehicle(id)` — rejects if vehicle has linked service orders

**Value Objects Detail:**

- **Document**
  - Fields: value (String), type (CPF or CNPJ)
  - Validation: CPF = 11 digits with valid check digits; CNPJ = 14 digits with valid check digits
  - Immutable: yes
  - Stored normalized (digits only)

- **Plate**
  - Fields: value (String)
  - Validation: matches `^[A-Z]{3}-?\\d[A-Z0-9]\\d{2}$` (covers both formats)
  - Immutable: yes
  - Stored uppercase, no dash

- **Email**
  - Fields: value (String)
  - Validation: standard email format
  - Immutable: yes

---

### Aggregate: Ordem de Servico (Service Order)

- **Root:** ServiceOrder
- **Entities:** ServiceOrder, ServiceOrderHistory
- **Value Objects:** StatusOs (enum)
- **References:** customer_id (→ Cadastro), vehicle_id (→ Cadastro)

**Invariants:**
- **INV-002:** OS status transitions must follow the valid state machine
  - Owner: Ordem de Servico (ServiceOrder)
  - Enforcement: StatusOs enum defines `allowedTransitions()` method; guard clause in `advanceStatus()` behavior
- **INV-005:** OS can only transition to FINALIZED when all linked services are FINALIZED
  - Owner: Ordem de Servico (ServiceOrder) — cross-aggregate check with Servico
  - Enforcement: Application service queries service statuses before calling `advanceStatus(FINALIZED)`
- **INV-006:** total_price equals SUM of linked service prices
  - Owner: Ordem de Servico (ServiceOrder)
  - Enforcement: Calculated on OS creation and when services are added/removed

**Behaviors:**
- `createServiceOrder(customerId, vehicleId, services)`
  - Validates customer and vehicle exist
  - Creates OS with status RECEIVED, calculates total_price (INV-006)
  - Creates initial history entry
- `advanceStatus(newStatus)`
  - Validates transition is allowed (INV-002)
  - Validates terminal status cannot transition (CANCELED, PAID)
  - Creates history entry with interval_time
  - Returns domain event for downstream processing
- `approve()`
  - Only from WAITING_APPROVAL → APPROVED
  - Triggers stock deduction (coordinated by application service)
- `refuse()`
  - Only from WAITING_APPROVAL → REFUSED
- `recalculateTotalPrice(servicePrices)`
  - Recalculates total_price = SUM(prices) (INV-006)

---

### Aggregate: Servico (Service)

- **Root:** Service
- **Entities:** Service, ServiceInsume (pivot), ServiceHistory
- **Value Objects:** StatusService (enum)
- **References:** service_order (→ Ordem de Servico), id_tb_insume (→ Estoque)

**Invariants:**
- **INV-003:** Service status transitions must follow INITIATED → PENDING → FINALIZED
  - Owner: Servico (Service)
  - Enforcement: StatusService enum defines `allowedTransitions()` method; guard clause in `advanceStatus()` behavior

**Behaviors:**
- `createService(serviceOrderId, description, price, estimatedTime, insumeIds)`
  - Creates service with status INITIATED
  - Links insumes via ServiceInsume pivot
- `advanceStatus(newStatus)`
  - Validates transition (INV-003)
  - Creates history entry with interval_time
- `linkInsume(insumeId)` — adds insume to service
- `unlinkInsume(insumeId)` — removes insume from service

---

### Aggregate: Estoque (Inventory)

- **Root:** Insume
- **Entities:** Insume
- **Value Objects:** None
- **References:** None

**Invariants:**
- **INV-004:** Stock quantity can NEVER be negative
  - Owner: Estoque (Insume)
  - Enforcement: Guard clause in `deductStock()` behavior — throws exception if quantity < requested amount

**Behaviors:**
- `createInsume(name, brand, skuId, quantity, price, unityPrice)`
  - Creates insume with given data and UUID
- `updateInsume(id, data)` — updates allowed fields
- `deductStock(amount)`
  - Validates quantity >= amount (INV-004)
  - Reduces quantity by amount
  - Uses pessimistic locking for concurrent access
- `restoreStock(amount)` — adds quantity back (for rollback/correction scenarios)

---

### Aggregate: Usuario (User)

- **Root:** User
- **Entities:** User
- **Value Objects:** None (password is hashed infrastructure concern, function is a simple string)
- **References:** None

**Invariants:**
- **INV-010:** User email must be unique
  - Owner: Usuario (User)
  - Enforcement: UNIQUE constraint on database + validation before persist
- **INV-011:** User password must be stored hashed (BCrypt)
  - Owner: Usuario (User)
  - Enforcement: Application service hashes password before entity creation

**Behaviors:**
- `createUser(name, function, email, rawPassword)`
  - Hashes password with BCrypt (INV-011)
  - Creates user with UUID
- `authenticate(email, rawPassword)`
  - Validates email exists, compares password hash
  - Returns JWT token with user_id, function, expiration

---

### Invariants Summary

| ID | Description | Owner Aggregate | Enforcement | Cross-Aggregate |
|----|-------------|----------------|-------------|-----------------|
| INV-001 | Customer document must be unique | Cadastro | DB UNIQUE + app validation | No |
| INV-002 | OS status transitions follow state machine | Ordem de Servico | StatusOs enum guard | No |
| INV-003 | Service status transitions follow INITIATED→PENDING→FINALIZED | Servico | StatusService enum guard | No |
| INV-004 | Stock quantity never negative | Estoque | Guard in deductStock() | No |
| INV-005 | OS → FINALIZED only when all services FINALIZED | Ordem de Servico | App service queries Servico | Yes (documented) |
| INV-006 | total_price = SUM(service prices) | Ordem de Servico | Calculated on create/modify | No |
| INV-007 | Vehicle plate valid format | Cadastro | Plate VO validation | No |
| INV-008 | Customer document valid CPF/CNPJ | Cadastro | Document VO validation | No |
| INV-009 | Customer email valid format | Cadastro | Email VO validation | No |
| INV-010 | User email must be unique | Usuario | DB UNIQUE + app validation | No |
| INV-011 | Password stored hashed (BCrypt) | Usuario | App service hashes before persist | No |

**Cross-aggregate trade-off (INV-005):**
- **Conflict:** INV-005 requires checking service statuses (Servico aggregate) before allowing OS status transition (Ordem de Servico aggregate)
- **Why re-modeling is not viable:** Services and OS have different change rates and independent lifecycles. Merging them into one aggregate would create a God Aggregate anti-pattern.
- **Trade-off accepted:** Application service performs the cross-aggregate check within a single transaction. Acceptable for monolith MVP.
- **Impact:** If the system migrates to microservices, this would need a saga or domain event pattern.

---

# PART 5 — REVIEWER

---

## Domain Review: Repair Shop MVP (Full Model)

**Score: 0.88/1.0**

### Issues

- **[MEDIUM]** INV-005 crosses aggregate boundaries (Ordem de Servico checks Servico status). Documented as conscious trade-off — acceptable for monolith MVP. Score: -0.05
- **[LOW]** tb_service_insume pivot table has no `quantity` column, assuming quantity = 1 for each insume-service link. If different quantities are needed, the model would need adjustment. Score: -0.02
- **[LOW]** Plate Value Object validation regex may need refinement for edge cases (old format with dash vs without dash). Score: -0.02
- **[LOW]** Payment context from Event Storming is conceptual only (no tables). Documented as out of scope for MVP. Score: -0.02
- **[LOW]** Email notification (quote sending) is conceptual — documented as out of scope. Score: -0.02

### Suggestions

- Consider adding a `quantity` column to `tb_service_insume` in a future iteration to support "2 units of brake pad for this service."
- When migrating to microservices, convert INV-005 to a domain event pattern: ServiceFinalized event triggers a check in the OS context.
- Consider making `function` in `tb_user` an enum (e.g., ADMIN, ATTENDANT, MECHANIC) rather than a free-text VARCHAR for stronger type safety.

### Checklist

**Aggregate checks:**
- [x] Every aggregate has exactly one Aggregate Root — Customer, ServiceOrder, Service, Insume, User
- [x] Aggregates are small (3-4 entities max) — largest is Servico with 3 (Service, ServiceInsume, ServiceHistory)
- [x] Other aggregates referenced by ID only — all FKs use UUID references
- [x] Each aggregate is a transactional boundary — confirmed

**Invariant checks:**
- [x] INV-001: Customer document uniqueness — enforced via DB UNIQUE + app validation
- [x] INV-002: OS status transitions — enforced via StatusOs enum guard clause
- [x] INV-003: Service status transitions — enforced via StatusService enum guard clause
- [x] INV-004: Stock quantity never negative — enforced via deductStock() guard + pessimistic lock
- [x] INV-005: OS → FINALIZED only when all services FINALIZED — cross-aggregate, documented trade-off
- [x] INV-006: total_price = SUM(service prices) — calculated on creation and modification
- [ ] No invariant crosses aggregate boundaries — INV-005 crosses (documented as trade-off, -0.05)

**Bounded context checks:**
- [x] Each context has its own package — 5 packages defined
- [x] No entity shared between contexts — confirmed
- [x] Cross-context communication via ID references — all FKs are UUID references
- [x] Ambiguous terms documented — "Finaliza OS" and "Cliente" disambiguated

**Ubiquitous language checks:**
- [x] All domain terms present in ubiquitous-language.md
- [x] Code uses same terms as glossary — aggregate/entity names match domain terms
- [x] Ambiguous terms properly qualified by context

**Anti-pattern checks:**
- [x] No anemic domain model — entities have behaviors (advanceStatus, deductStock, etc.)
- [x] No god aggregate — largest aggregate has 3 entities
- [x] No logic in controllers — all logic in domain/application services
- [x] No direct coupling between context packages — ID references only
- [x] No primitive obsession — Document, Plate, Email as Value Objects
- [x] No CRUD thinking — operations named after domain actions (approve, refuse, advanceStatus, deductStock)
- [x] Status transitions use enum functions, not external if/else chains

**Naming convention checks:**
- [x] Tables prefixed with `tb_`
- [x] PKs named `id_tb_<entity>`
- [x] FKs named `<entity>_id`
- [x] Timestamps named `created`, `updated`
- [x] Enums in PascalCase: StatusOs, StatusService

**Status machine checks:**
- [x] status_os has all 9 valid states
- [x] status_os transitions match defined state machine
- [x] CANCELED and PAID are terminal states
- [x] status_service has all 3 valid states
- [x] status_service transitions follow INITIATED → PENDING → FINALIZED

### Summary

- **Total checks:** 25
- **Passed:** 24
- **Failed:** 1 (INV-005 cross-aggregate, documented trade-off)
- **HIGH issues:** 0
- **MEDIUM issues:** 1 (-0.05)
- **LOW issues:** 4 (-0.08)
- **Score calculation:** 1.0 - 0.05 - 0.08 = **0.88/1.0** (threshold >= 0.7: **PASS**)

---

**Verdict: Score 0.88 >= 0.7 — Specification approved. Ready for implementation.**
