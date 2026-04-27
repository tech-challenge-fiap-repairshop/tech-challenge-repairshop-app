# API Contracts: Cadastro (Customer + Vehicle)

**Plan:** docs/plan/04-22-26-plan/plan.md
**Spec:** docs/spec/04-22-26-spec/spec.md

---

## Error response format (all endpoints)

All error responses use RFC 7807 ProblemDetail:

```json
{
  "type": "about:blank",
  "title": "Error Title",
  "status": 400,
  "detail": "Human-readable error message",
  "instance": "/request/path"
}
```

## Pagination format (all list endpoints)

```json
{
  "content": [...],
  "totalElements": 100,
  "totalPages": 5,
  "number": 0,
  "size": 20
}
```

Query params: `page` (default 0), `size` (default 20), `sort` (e.g., `name,asc`)

---

## Customer Endpoints

### POST /customers

**Description:** Register a new customer
**Story:** US-1 (Register a new customer)
**Authentication:** Required

**Request body:**
```json
{
  "name": "string — required, not blank",
  "document": "string — required, CPF (11 digits) or CNPJ (14 digits), accepts formatted or raw",
  "email": "string — required, valid email format",
  "phone": "string — required",
  "birth_date": "string — optional, ISO date (YYYY-MM-DD)"
}
```

**Responses:**

- **201 Created** (AC-1):
```json
{
  "id": "UUID",
  "name": "Maria Silva",
  "document": "52998224725",
  "email": "maria@email.com",
  "phone": "(11)99999-0000",
  "birth_date": "1990-05-15",
  "created": "2026-04-22T10:00:00",
  "updated": "2026-04-22T10:00:00"
}
```

- **400 Bad Request** (AC-3, AC-4, AC-5):
  - `"Invalid CPF."` — invalid CPF check digits (AC-3)
  - `"Invalid CNPJ."` — invalid CNPJ check digits (AC-4)
  - `"Name is required."` — blank name (AC-5)
  - `"Invalid email format."` — invalid email (FR-027)

- **409 Conflict** (AC-2):
  - `"Customer with this document already exists."` — duplicate document

---

### GET /customers

**Description:** List all customers (paginated)
**Story:** US-2 (Manage customers)
**Authentication:** Required

**Query params:** `page`, `size`, `sort`

**Responses:**

- **200 OK** (AC-6): Paginated list of customer objects. Empty list (not error) when no customers exist (E1).

---

### GET /customers/{id}

**Description:** Get customer by ID
**Story:** US-2 (Manage customers)
**Authentication:** Required

**Path params:** `{id}` — UUID

**Responses:**

- **200 OK** (AC-7): Full customer object
- **404 Not Found** (AC-9): `"Customer not found."`

---

### PUT /customers/{id}

**Description:** Update customer data
**Story:** US-2 (Manage customers)
**Authentication:** Required

**Path params:** `{id}` — UUID

**Request body:**
```json
{
  "name": "string — required",
  "document": "string — required",
  "email": "string — required",
  "phone": "string — required",
  "birth_date": "string — optional"
}
```

**Responses:**

- **200 OK** (AC-8): Updated customer object with refreshed `updated` timestamp
- **400 Bad Request**: Validation errors (same as POST)
- **404 Not Found**: `"Customer not found."`
- **409 Conflict**: `"Customer with this document already exists."` (if document changed to existing one)

---

### DELETE /customers/{id}

**Description:** Delete a customer
**Story:** US-2 (Manage customers)
**Authentication:** Required

**Path params:** `{id}` — UUID

**Responses:**

- **204 No Content**: Customer deleted successfully
- **404 Not Found**: `"Customer not found."`
- **409 Conflict** (AC-10): `"Cannot delete customer with existing service orders."`

---

## Vehicle Endpoints

### POST /vehicles

**Description:** Register a vehicle linked to a customer
**Story:** US-3 (Register a vehicle)
**Authentication:** Required

**Request body:**
```json
{
  "customer_id": "UUID — required",
  "plate": "string — required, ABC-1234 or ABC1D23 format",
  "brand": "string — required",
  "model": "string — required",
  "color": "string — optional",
  "manufacturing_date": "string — optional, ISO date"
}
```

**Responses:**

- **201 Created** (AC-11, AC-14):
```json
{
  "id": "UUID",
  "customer_id": "UUID",
  "plate": "ABC1D23",
  "brand": "Toyota",
  "model": "Corolla",
  "color": "Silver",
  "manufacturing_date": "2022-01-01",
  "last_maintenance": null,
  "created": "2026-04-22T10:00:00",
  "updated": "2026-04-22T10:00:00"
}
```

- **400 Bad Request** (AC-12, E8, E16):
  - `"Invalid plate format. Expected ABC-1234 or ABC1D23."` — invalid plate (AC-12)
  - `"Manufacturing date cannot be in the future."` — future date (E16)
  - Lowercase plates are normalized to uppercase (E8) — not an error

- **404 Not Found** (AC-13): `"Customer not found."`

---

### GET /vehicles

**Description:** List all vehicles (paginated)
**Story:** US-4 (Manage vehicles)
**Authentication:** Required

**Query params:** `page`, `size`, `sort`

**Responses:**

- **200 OK** (AC-15): Paginated list of vehicle objects

---

### GET /vehicles/{id}

**Description:** Get vehicle by ID
**Story:** US-4 (Manage vehicles)
**Authentication:** Required

**Path params:** `{id}` — UUID

**Responses:**

- **200 OK**: Full vehicle object
- **404 Not Found**: `"Vehicle not found."`

---

### PUT /vehicles/{id}

**Description:** Update vehicle data
**Story:** US-4 (Manage vehicles)
**Authentication:** Required

**Path params:** `{id}` — UUID

**Request body:**
```json
{
  "customer_id": "UUID — required",
  "plate": "string — required",
  "brand": "string — required",
  "model": "string — required",
  "color": "string — optional",
  "manufacturing_date": "string — optional"
}
```

**Responses:**

- **200 OK** (AC-16): Updated vehicle object with refreshed `updated` timestamp
- **400 Bad Request**: Validation errors (same as POST)
- **404 Not Found**: `"Vehicle not found."`

---

### DELETE /vehicles/{id}

**Description:** Delete a vehicle
**Story:** US-4 (Manage vehicles)
**Authentication:** Required

**Path params:** `{id}` — UUID

**Responses:**

- **204 No Content**: Vehicle deleted successfully
- **404 Not Found**: `"Vehicle not found."`
- **409 Conflict** (AC-17): `"Cannot delete vehicle with existing service orders."`
