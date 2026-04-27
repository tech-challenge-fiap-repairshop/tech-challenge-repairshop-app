# Data Model: Repair Shop MVP

**Date:** 2026-04-22
**Plan:** docs/plan/04-22-26-plan/plan.md
**Spec:** docs/spec/04-22-26-spec/spec.md

---

## Bounded Context: Cadastro

### Customer

**Aggregate:** Cadastro
**Role:** Aggregate root
**Table:** `tb_customer`

| Field | Type | Constraints | Description |
|-------|------|-------------|-------------|
| id_tb_customer | UUID | PK | Unique identifier |
| name | VARCHAR(150) | NOT NULL | Full name |
| document | VARCHAR(14) | NOT NULL, UNIQUE | CPF (11 digits) or CNPJ (14 digits), stored normalized (digits only) |
| email | VARCHAR(255) | | Email address, validated format |
| phone | VARCHAR(20) | | Phone number |
| birth_date | DATE | NULL | Date of birth |
| created | TIMESTAMP | NOT NULL, DEFAULT CURRENT_TIMESTAMP | Creation timestamp |
| updated | TIMESTAMP | NOT NULL, DEFAULT CURRENT_TIMESTAMP | Last update timestamp |

**Invariants protected:**
- INV-001: Customer document must be unique (DB UNIQUE + app validation)
- INV-008: Customer document must be valid CPF or CNPJ (Value Object Document validates on construction)
- INV-009: Customer email must be valid format (Value Object Email validates on construction)

---

### Vehicle

**Aggregate:** Cadastro
**Role:** Child entity
**Table:** `tb_vehicle`

| Field | Type | Constraints | Description |
|-------|------|-------------|-------------|
| id_tb_vehicle | UUID | PK | Unique identifier |
| customer_id | UUID | NOT NULL, FK → tb_customer(id_tb_customer) | Owning customer |
| plate | VARCHAR(7) | NOT NULL | Vehicle plate, stored uppercase without dash |
| brand | VARCHAR(50) | NOT NULL | Manufacturer brand |
| model | VARCHAR(80) | NOT NULL | Vehicle model |
| color | VARCHAR(30) | NULL | Vehicle color |
| manufacturing_date | DATE | NULL | Manufacturing date (must not be future) |
| last_maintenance | TIMESTAMP | NULL | Last maintenance timestamp |
| created | TIMESTAMP | NOT NULL, DEFAULT CURRENT_TIMESTAMP | Creation timestamp |
| updated | TIMESTAMP | NOT NULL, DEFAULT CURRENT_TIMESTAMP | Last update timestamp |

**Relationships:**
- Vehicle → Customer (N:1) via `customer_id`

**Invariants protected:**
- INV-007: Vehicle plate must be valid format — ABC-1234 or ABC1D23 (Value Object Plate validates on construction)

---

### Value Object: Document

| Property | Type | Description |
|----------|------|-------------|
| value | String | Digits only (11 for CPF, 14 for CNPJ) |
| type | DocumentType | CPF or CNPJ (derived from length) |

**Validation:** CPF — 11 digits with valid check digits (modulo 11 algorithm). CNPJ — 14 digits with valid check digits. Rejects known invalid patterns (all same digit).
**Storage:** Persisted as `VARCHAR(14)` in `document` column. Stored normalized (digits only). Input accepted with or without formatting (dots, dashes, slashes).

---

### Value Object: Plate

| Property | Type | Description |
|----------|------|-------------|
| value | String | Uppercase, no dash, 7 characters |

**Validation:** Regex covers old format (ABC-1234) and Mercosul (ABC1D23). Input normalized to uppercase.
**Storage:** Persisted as `VARCHAR(7)` in `plate` column.

---

### Value Object: Email

| Property | Type | Description |
|----------|------|-------------|
| value | String | Valid email address |

**Validation:** Standard email format regex. Not null, not blank.
**Storage:** Persisted as `VARCHAR(255)` in `email` column.

---

## Bounded Context: Ordem de Servico

### ServiceOrder

**Aggregate:** Ordem de Servico
**Role:** Aggregate root
**Table:** `tb_service_order`

| Field | Type | Constraints | Description |
|-------|------|-------------|-------------|
| id_tb_service_order | UUID | PK | Unique identifier |
| customer_id | UUID | NOT NULL, FK → tb_customer(id_tb_customer) | Customer requesting service |
| vehicle_id | UUID | NOT NULL, FK → tb_vehicle(id_tb_vehicle) | Vehicle being serviced |
| status | VARCHAR(30) | NOT NULL, DEFAULT 'RECEIVED' | Current OS status (StatusOs enum) |
| total_price | DECIMAL(12,2) | NOT NULL, DEFAULT 0.00 | Calculated budget = SUM(service prices) |
| enter_time | TIMESTAMP | NULL | Entry timestamp |
| end_time | TIMESTAMP | NULL | Completion timestamp |
| valid_date | DATE | NULL | Quote validity date |
| created | TIMESTAMP | NOT NULL, DEFAULT CURRENT_TIMESTAMP | Creation timestamp |
| updated | TIMESTAMP | NOT NULL, DEFAULT CURRENT_TIMESTAMP | Last update timestamp |

**Relationships:**
- ServiceOrder → Customer (N:1) via `customer_id`
- ServiceOrder → Vehicle (N:1) via `vehicle_id`
- ServiceOrder → ServiceOrderHistory (1:N) inverse side

**Invariants protected:**
- INV-002: OS status transitions must follow the valid state machine
- INV-005: OS can only transition to FINALIZED when all linked services are FINALIZED (cross-aggregate, enforced at application service)
- INV-006: total_price = SUM(service prices), calculated on creation and when services change

---

### ServiceOrderHistory

**Aggregate:** Ordem de Servico
**Role:** Child entity (immutable record)
**Table:** `tb_service_order_history`

| Field | Type | Constraints | Description |
|-------|------|-------------|-------------|
| id_tb_service_order_history | UUID | PK | Unique identifier |
| service_order_id | UUID | NOT NULL, FK → tb_service_order(id_tb_service_order) | Parent service order |
| status | VARCHAR(30) | NOT NULL | Status at this transition point |
| register_time | TIMESTAMP | NOT NULL, DEFAULT CURRENT_TIMESTAMP | When this transition happened |
| interval_time | INTERVAL | NULL | Duration since previous transition (null for first entry). Maps to `java.time.Duration` in Kotlin via Hibernate 7.x native support. |

**Relationships:**
- ServiceOrderHistory → ServiceOrder (N:1) via `service_order_id`

---

### Enum: StatusOs

| Value | Description | Allowed transitions |
|-------|-------------|-------------------|
| RECEIVED | OS created, awaiting diagnosis | → IN_DIAGNOSIS |
| IN_DIAGNOSIS | Mechanic performing diagnosis | → WAITING_APPROVAL |
| WAITING_APPROVAL | Quote sent, awaiting customer decision | → APPROVED, → REFUSED |
| APPROVED | Customer approved the quote | → IN_EXECUTION |
| REFUSED | Customer refused the quote | → CANCELED |
| IN_EXECUTION | Services being executed | → FINALIZED |
| FINALIZED | All services completed | → PAID |
| PAID | Payment received (terminal) | — (none) |
| CANCELED | OS canceled (terminal) | — (none) |

**State machine implementation:** Kotlin enum with `allowedTransitions(): Set<StatusOs>` method. Guard clause in `advanceStatus()` checks the set before allowing transition. Terminal states (PAID, CANCELED) return empty set.

---

## Bounded Context: Servico

### Service

**Aggregate:** Servico
**Role:** Aggregate root
**Table:** `tb_service`

| Field | Type | Constraints | Description |
|-------|------|-------------|-------------|
| id_tb_service | UUID | PK | Unique identifier |
| service_order | UUID | NOT NULL, FK → tb_service_order(id_tb_service_order) | Parent service order |
| description | TEXT | NOT NULL | Service description (e.g., "Oil change") |
| price | DECIMAL(10,2) | NOT NULL | Service price |
| estimated_time | DECIMAL(5,2) | NULL | Estimated hours |
| status | VARCHAR(20) | NOT NULL, DEFAULT 'INITIATED' | Current service status (StatusService enum) |
| created | TIMESTAMP | NOT NULL, DEFAULT CURRENT_TIMESTAMP | Creation timestamp |
| updated | TIMESTAMP | NOT NULL, DEFAULT CURRENT_TIMESTAMP | Last update timestamp |

**Relationships:**
- Service → ServiceOrder (N:1) via `service_order`
- Service → ServiceInsume (1:N) inverse side
- Service → ServiceHistory (1:N) inverse side

**Invariants protected:**
- INV-003: Service status transitions must follow INITIATED → PENDING → FINALIZED

---

### ServiceInsume

**Aggregate:** Servico
**Role:** Pivot table (N:N between Service and Insume)
**Table:** `tb_service_insume`

| Field | Type | Constraints | Description |
|-------|------|-------------|-------------|
| id_tb_service | UUID | PK, FK → tb_service(id_tb_service) | Service side of the relationship |
| id_tb_insume | UUID | PK, FK → tb_insume(id_tb_insume) | Insume side of the relationship |

**Composite primary key:** (`id_tb_service`, `id_tb_insume`)

**Note:** No `quantity` column — per spec assumption, quantity is 1 per insume-service link.

**Relationships:**
- ServiceInsume → Service (N:1) via `id_tb_service`
- ServiceInsume → Insume (N:1) via `id_tb_insume`

---

### ServiceHistory

**Aggregate:** Servico
**Role:** Child entity (immutable record)
**Table:** `tb_service_history`

| Field | Type | Constraints | Description |
|-------|------|-------------|-------------|
| id_tb_service_history | UUID | PK | Unique identifier |
| service_id | UUID | NOT NULL, FK → tb_service(id_tb_service) | Parent service |
| status | VARCHAR(20) | NOT NULL | Status at this transition point |
| register_time | TIMESTAMP | NOT NULL, DEFAULT CURRENT_TIMESTAMP | When this transition happened |
| interval_time | INTERVAL | NULL | Duration since previous transition. Maps to `java.time.Duration` in Kotlin via Hibernate 7.x native support. |

**Relationships:**
- ServiceHistory → Service (N:1) via `service_id`

---

### Enum: StatusService

| Value | Description | Allowed transitions |
|-------|-------------|-------------------|
| INITIATED | Service created, not yet started | → PENDING |
| PENDING | Service in progress by mechanic | → FINALIZED |
| FINALIZED | Service completed (terminal) | — (none) |

**State machine implementation:** Kotlin enum with `allowedTransitions(): Set<StatusService>` method.

---

## Bounded Context: Estoque

### Insume

**Aggregate:** Estoque
**Role:** Aggregate root
**Table:** `tb_insume`

| Field | Type | Constraints | Description |
|-------|------|-------------|-------------|
| id_tb_insume | UUID | PK | Unique identifier |
| name | VARCHAR(150) | NOT NULL | Insume name (e.g., "Brake pad") |
| brand | VARCHAR(100) | NULL | Manufacturer brand |
| sku_id | VARCHAR(50) | NULL | SKU identifier |
| quantity | INTEGER | NOT NULL, DEFAULT 0 | Current stock quantity |
| price | DECIMAL(10,2) | NOT NULL | Sale price |
| unity_price | DECIMAL(10,2) | NOT NULL | Unit cost price |

**Invariants protected:**
- INV-004: Stock quantity can NEVER be negative. Guard clause in `deductStock(amount)`: throws `InsufficientStockException` if `quantity < amount`. Pessimistic lock (`SELECT ... FOR UPDATE`) used during deduction.

**Behaviors:**
- `deductStock(amount: Int)` — validates quantity >= amount, then quantity -= amount
- `restoreStock(amount: Int)` — quantity += amount (for rollback scenarios)

---

## Bounded Context: Usuarios

### User

**Aggregate:** Usuario
**Role:** Aggregate root
**Table:** `tb_user`

| Field | Type | Constraints | Description |
|-------|------|-------------|-------------|
| id_tb_user | UUID | PK | Unique identifier |
| name | VARCHAR(150) | NOT NULL | User display name |
| function | VARCHAR(50) | NOT NULL | User role/access level (e.g., ADMIN, ATTENDANT) |
| email | VARCHAR(255) | NOT NULL, UNIQUE | Login email |
| password | VARCHAR(255) | NOT NULL | BCrypt-hashed password (cost factor >= 10) |

**Invariants protected:**
- INV-010: User email must be unique (DB UNIQUE + app validation)
- INV-011: User password must be stored hashed with BCrypt (application service hashes before persist)

---

## Relationships Summary

| From | To | Type | FK Column | Notes |
|------|----|------|-----------|-------|
| tb_vehicle | tb_customer | N:1 | customer_id | Same context (Cadastro) |
| tb_service_order | tb_customer | N:1 | customer_id | Cross-context (Ordem de Servico → Cadastro) |
| tb_service_order | tb_vehicle | N:1 | vehicle_id | Cross-context (Ordem de Servico → Cadastro) |
| tb_service_order_history | tb_service_order | N:1 | service_order_id | Same context (Ordem de Servico) |
| tb_service | tb_service_order | N:1 | service_order | Cross-context (Servico → Ordem de Servico) |
| tb_service_insume | tb_service | N:1 | id_tb_service | Same context (Servico) |
| tb_service_insume | tb_insume | N:1 | id_tb_insume | Cross-context (Servico → Estoque) |
| tb_service_history | tb_service | N:1 | service_id | Same context (Servico) |

All cross-context relationships use ID references only — no shared entities.
