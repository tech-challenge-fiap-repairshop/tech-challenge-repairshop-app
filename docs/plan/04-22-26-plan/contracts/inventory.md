# API Contracts: Estoque (Inventory)

**Plan:** docs/plan/04-22-26-plan/plan.md
**Spec:** docs/spec/04-22-26-spec/spec.md

---

## POST /insumes

**Description:** Create a new insume in inventory
**Story:** US-12 (Manage insume inventory)
**Authentication:** Required

**Request body:**
```json
{
  "name": "string — required",
  "brand": "string — optional",
  "sku_id": "string — optional",
  "quantity": "integer — required, >= 0",
  "price": "decimal — required",
  "unity_price": "decimal — required"
}
```

**Responses:**

- **201 Created** (AC-42):
```json
{
  "id": "UUID",
  "name": "Brake pad",
  "brand": "Bosch",
  "sku_id": "BP-001",
  "quantity": 50,
  "price": 89.90,
  "unity_price": 89.90
}
```

- **400 Bad Request**: Validation errors (missing required fields)

---

## GET /insumes

**Description:** List all insumes (paginated)
**Story:** US-12 (Manage insume inventory)
**Authentication:** Required

**Query params:** `page`, `size`, `sort`

**Responses:**

- **200 OK** (AC-45): Paginated list of insume objects

---

## GET /insumes/{id}

**Description:** Get insume by ID
**Story:** US-12 (Manage insume inventory)
**Authentication:** Required

**Path params:** `{id}` — UUID

**Responses:**

- **200 OK**: Full insume object
- **404 Not Found**: `"Insume not found."`

---

## PUT /insumes/{id}

**Description:** Update insume data
**Story:** US-12 (Manage insume inventory)
**Authentication:** Required

**Path params:** `{id}` — UUID

**Request body:**
```json
{
  "name": "string — required",
  "brand": "string — optional",
  "sku_id": "string — optional",
  "quantity": "integer — required, >= 0",
  "price": "decimal — required",
  "unity_price": "decimal — required"
}
```

**Responses:**

- **200 OK** (AC-43): Updated insume object
- **400 Bad Request**: Validation errors
- **404 Not Found**: `"Insume not found."`

---

## DELETE /insumes/{id}

**Description:** Delete an insume
**Story:** US-12 (Manage insume inventory)
**Authentication:** Required

**Path params:** `{id}` — UUID

**Responses:**

- **204 No Content**: Insume deleted successfully (allowed if not linked or quantity = 0 and not linked — E11)
- **404 Not Found**: `"Insume not found."`
- **409 Conflict** (AC-44): `"Cannot delete insume linked to an active service."`

---

## Stock deduction (no dedicated endpoint)

Stock deduction happens automatically when an OS is approved via `POST /service-orders/{id}/approve`. See `contracts/service-order.md` for details.

**Behavior (US-13):**
- AC-46: On approval, insume quantity is decremented for each linked insume
- AC-47: If stock < required, approval is rejected with 422
- AC-48: Deduction is atomic — all or nothing within a single transaction
- Uses `@Lock(LockModeType.PESSIMISTIC_WRITE)` to prevent concurrent race conditions (E6)
