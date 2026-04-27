# API Contracts: Servico (Service)

**Plan:** docs/plan/04-22-26-plan/plan.md
**Spec:** docs/spec/04-22-26-spec/spec.md

---

## POST /services

**Description:** Create a new service in the catalog
**Story:** US-10 (Manage service catalog)
**Authentication:** Required

**Request body:**
```json
{
  "description": "string — required",
  "price": "decimal — required",
  "estimated_time": "decimal — optional (hours)"
}
```

**Responses:**

- **201 Created** (AC-35):
```json
{
  "id": "UUID",
  "description": "Oil change",
  "price": 120.00,
  "estimated_time": 1.5,
  "status": "INITIATED",
  "created": "2026-04-22T10:00:00",
  "updated": "2026-04-22T10:00:00"
}
```

- **400 Bad Request**: Validation errors (missing required fields)

---

## GET /services

**Description:** List all services (paginated)
**Story:** US-10 (Manage service catalog)
**Authentication:** Required

**Query params:** `page`, `size`, `sort`

**Responses:**

- **200 OK** (AC-37): Paginated list of service objects

---

## GET /services/{id}

**Description:** Get service by ID
**Story:** US-10 (Manage service catalog)
**Authentication:** Required

**Path params:** `{id}` — UUID

**Responses:**

- **200 OK**: Full service object
- **404 Not Found**: `"Service not found."`

---

## PUT /services/{id}

**Description:** Update service data
**Story:** US-10 (Manage service catalog)
**Authentication:** Required

**Path params:** `{id}` — UUID

**Request body:**
```json
{
  "description": "string — required",
  "price": "decimal — required",
  "estimated_time": "decimal — optional"
}
```

**Responses:**

- **200 OK**: Updated service object with refreshed `updated` timestamp
- **400 Bad Request**: Validation errors
- **404 Not Found**: `"Service not found."`

---

## DELETE /services/{id}

**Description:** Delete a service
**Story:** US-10 (Manage service catalog)
**Authentication:** Required

**Path params:** `{id}` — UUID

**Responses:**

- **204 No Content**: Service deleted successfully
- **404 Not Found**: `"Service not found."`
- **409 Conflict** (AC-36): `"Cannot delete service linked to an active service order."`

---

## PATCH /services/{id}/status

**Description:** Advance the status of a service within an OS
**Story:** US-11 (Advance service status)
**Authentication:** Required

**Path params:** `{id}` — UUID

**Request body:**
```json
{
  "status": "string — required, target status (PENDING or FINALIZED)"
}
```

**Responses:**

- **200 OK** (AC-38, AC-39): Updated service with new status, history record created with interval_time
```json
{
  "id": "UUID",
  "description": "Oil change",
  "price": 120.00,
  "estimated_time": 1.5,
  "status": "PENDING",
  "created": "2026-04-22T10:00:00",
  "updated": "2026-04-22T11:00:00"
}
```

- **404 Not Found**: `"Service not found."`
- **422 Unprocessable Entity** (AC-40):
  - `"Invalid service status transition from INITIATED to FINALIZED."` — skipping PENDING

**Side effect (AC-41):**
- When the last service in an OS is finalized, the system allows the OS to transition to FINALIZED (INV-005 cross-aggregate check)
