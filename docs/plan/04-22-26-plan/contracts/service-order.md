# API Contracts: Ordem de Servico (Service Order)

**Plan:** docs/plan/04-22-26-plan/plan.md
**Spec:** docs/spec/04-22-26-spec/spec.md

---

## POST /service-orders

**Description:** Create a service order with automatic budget calculation
**Story:** US-5 (Create a service order with automatic budget)
**Authentication:** Required

**Request body:**
```json
{
  "customer_id": "UUID — required",
  "vehicle_id": "UUID — required",
  "services": [
    {
      "description": "string — required",
      "price": "decimal — required",
      "estimated_time": "decimal — optional (hours)",
      "insume_ids": ["UUID — optional, list of insume IDs to link"]
    }
  ]
}
```

**Responses:**

- **201 Created** (AC-18, AC-21, AC-22):
```json
{
  "id": "UUID",
  "customer_id": "UUID",
  "vehicle_id": "UUID",
  "status": "RECEIVED",
  "total_price": 400.00,
  "enter_time": null,
  "end_time": null,
  "valid_date": null,
  "created": "2026-04-22T10:00:00",
  "updated": "2026-04-22T10:00:00",
  "services": [
    {
      "id": "UUID",
      "description": "Oil change",
      "price": 150.00,
      "estimated_time": 1.5,
      "status": "INITIATED",
      "insume_ids": ["UUID"]
    }
  ],
  "history": [
    {
      "id": "UUID",
      "status": "RECEIVED",
      "register_time": "2026-04-22T10:00:00",
      "interval_time": null
    }
  ]
}
```

- **400 Bad Request** (E7): `"OS must have at least one service."`
- **404 Not Found** (AC-19, AC-20):
  - `"Customer not found."` (AC-19)
  - `"Vehicle not found."` (AC-20)

---

## GET /service-orders

**Description:** List all service orders (paginated)
**Story:** US-8 (Track service order progress), FR-016
**Authentication:** Required

**Query params:** `page`, `size`, `sort`

**Responses:**

- **200 OK**: Paginated list of service order summary objects (without full history)

---

## GET /service-orders/{id}

**Description:** Get service order by ID with full status history
**Story:** US-8 (Track service order progress)
**Authentication:** **Public** (AC-56 — customer tracking endpoint)

**Path params:** `{id}` — UUID

**Responses:**

- **200 OK** (AC-31): Full service order with current status, total_price, services, and complete ordered history
```json
{
  "id": "UUID",
  "customer_id": "UUID",
  "vehicle_id": "UUID",
  "status": "IN_EXECUTION",
  "total_price": 400.00,
  "enter_time": "2026-04-22T10:00:00",
  "end_time": null,
  "valid_date": null,
  "created": "2026-04-22T10:00:00",
  "updated": "2026-04-22T12:00:00",
  "services": [...],
  "history": [
    {
      "id": "UUID",
      "status": "RECEIVED",
      "register_time": "2026-04-22T10:00:00",
      "interval_time": null
    },
    {
      "id": "UUID",
      "status": "IN_DIAGNOSIS",
      "register_time": "2026-04-22T10:30:00",
      "interval_time": 1800000
    }
  ]
}
```

- **404 Not Found** (AC-32): `"Service order not found."`

---

## PATCH /service-orders/{id}/status

**Description:** Advance the service order status through the state machine
**Story:** US-6 (Advance service order status)
**Authentication:** Required

**Path params:** `{id}` — UUID

**Request body:**
```json
{
  "status": "string — required, target status (e.g., IN_DIAGNOSIS)"
}
```

**Responses:**

- **200 OK** (AC-23, AC-57): Updated service order with new status, history record created with interval_time
- **404 Not Found**: `"Service order not found."`
- **422 Unprocessable Entity** (AC-24, AC-25, AC-26, E10):
  - `"Invalid status transition from RECEIVED to APPROVED."` (AC-24)
  - `"Cannot transition from terminal status CANCELED."` (AC-25)
  - `"Cannot transition from terminal status PAID."` (AC-26)
  - `"Cannot transition to the same status."` (E10)

**Special rule for FINALIZED transition (INV-005):**
- **422 Unprocessable Entity**: `"Cannot finalize OS: not all services are FINALIZED."` — when attempting IN_EXECUTION → FINALIZED but linked services are not all FINALIZED (FR-025)

---

## POST /service-orders/{id}/approve

**Description:** Customer approves the service order quote
**Story:** US-7 (Customer approves or refuses the quote)
**Authentication:** Required

**Path params:** `{id}` — UUID

**Request body:**
```json
{
  "approved": "boolean — required (true = approve, false = refuse)"
}
```

**Responses:**

- **200 OK** (AC-27, AC-28):
  - If `approved: true` → Status changes to APPROVED, history record created, insume stock deducted atomically (AC-27)
  - If `approved: false` → Status changes to REFUSED, history record created (AC-28)

- **404 Not Found**: `"Service order not found."`
- **422 Unprocessable Entity** (AC-29, AC-30):
  - `"OS is not awaiting approval."` — status is not WAITING_APPROVAL (AC-29)
  - `"Insufficient stock for insume [name]. Available: [n], Required: [m]."` — not enough stock (AC-30)

**Side effects on approval (AC-46, AC-48):**
- All insumes linked to OS services via tb_service_insume have stock deducted
- Deduction is atomic — if any insume fails, none are deducted (AC-48)
- Uses pessimistic locking to prevent concurrent deduction race conditions (E6)

---

## GET /service-orders/metrics

**Description:** Get average execution time of service orders
**Story:** US-9 (View service order metrics)
**Authentication:** Required

**Responses:**

- **200 OK** (AC-33, AC-34):
```json
{
  "average_execution_time_ms": 7200000,
  "completed_orders": 15
}
```
  - `average_execution_time_ms`: average interval from IN_EXECUTION to FINALIZED (in milliseconds)
  - If no completed OS exist: `average_execution_time_ms: null, completed_orders: 0` (AC-34)
