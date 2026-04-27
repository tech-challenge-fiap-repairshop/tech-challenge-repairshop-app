# Quickstart: Validation Scenarios

**Date:** 2026-04-22
**Plan:** docs/plan/04-22-26-plan/plan.md
**Spec:** docs/spec/04-22-26-spec/spec.md

These are the first 5 scenarios to test after the initial setup is complete. They validate the golden path through the most critical acceptance criteria.

---

## QS-1: Register a customer with CPF validation

**Validates:** AC-1, AC-3, INV-001, INV-008
**Phase dependency:** Phase 4A (Cadastro)

```bash
# 1. Create a valid customer
curl -X POST http://localhost:8080/customers \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <token>" \
  -d '{"name":"Maria Silva","document":"529.982.247-25","email":"maria@email.com","phone":"(11)99999-0000","birth_date":"1990-05-15"}'
# Expected: 201, UUID returned, document stored as "52998224725"

# 2. Attempt duplicate document
curl -X POST http://localhost:8080/customers \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <token>" \
  -d '{"name":"Outra Maria","document":"52998224725","email":"outra@email.com","phone":"(11)88888-0000"}'
# Expected: 409, "Customer with this document already exists."

# 3. Attempt invalid CPF
curl -X POST http://localhost:8080/customers \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <token>" \
  -d '{"name":"Teste","document":"111.111.111-11","email":"t@email.com","phone":"(11)77777-0000"}'
# Expected: 400, "Invalid CPF."
```

---

## QS-2: Create OS with automatic budget and status history

**Validates:** AC-18, AC-21, AC-22, INV-006
**Phase dependency:** Phase 4B (Ordem de Servico)

```bash
# Prerequisites: customer, vehicle, and insumes already created

# 1. Create OS with 2 services
curl -X POST http://localhost:8080/service-orders \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <token>" \
  -d '{"customer_id":"<customer-uuid>","vehicle_id":"<vehicle-uuid>","services":[{"description":"Oil change","price":150.00,"estimated_time":1.5},{"description":"Alignment","price":250.00,"estimated_time":2.0}]}'
# Expected: 201, status=RECEIVED, total_price=400.00
# Expected: history array with 1 entry (status=RECEIVED, interval_time=null)
# Expected: services array with 2 entries (status=INITIATED each)
```

---

## QS-3: Advance OS through full lifecycle

**Validates:** AC-23, AC-24, AC-25, INV-002
**Phase dependency:** Phase 4B (Ordem de Servico + Servico)

```bash
# Prerequisites: OS created in RECEIVED status

# 1. Valid transition: RECEIVED -> IN_DIAGNOSIS
curl -X PATCH http://localhost:8080/service-orders/<os-uuid>/status \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <token>" \
  -d '{"status":"IN_DIAGNOSIS"}'
# Expected: 200, status=IN_DIAGNOSIS, new history entry with interval_time

# 2. Invalid transition: IN_DIAGNOSIS -> APPROVED (skipping WAITING_APPROVAL)
curl -X PATCH http://localhost:8080/service-orders/<os-uuid>/status \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <token>" \
  -d '{"status":"APPROVED"}'
# Expected: 422, "Invalid status transition from IN_DIAGNOSIS to APPROVED."
```

---

## QS-4: Approve OS with atomic stock deduction

**Validates:** AC-27, AC-30, AC-46, AC-48, INV-004
**Phase dependency:** Phase 4B (Ordem de Servico + Estoque)

```bash
# Prerequisites: OS in WAITING_APPROVAL with services linked to insumes

# 1. Approve with sufficient stock
curl -X POST http://localhost:8080/service-orders/<os-uuid>/approve \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <token>" \
  -d '{"approved":true}'
# Expected: 200, status=APPROVED
# Verify: insume stock decreased (GET /insumes/<id> shows reduced quantity)

# 2. Attempt approve with insufficient stock (separate OS)
# Setup: insume with quantity=0, service linked to that insume
curl -X POST http://localhost:8080/service-orders/<os2-uuid>/approve \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <token>" \
  -d '{"approved":true}'
# Expected: 422, "Insufficient stock for insume [name]. Available: 0, Required: 1."
```

---

## QS-5: Login and access control

**Validates:** AC-49, AC-50, AC-53, AC-56, INV-010
**Phase dependency:** Phase 5 (Security)

```bash
# Prerequisites: user created (via seed or direct DB insert for first test)

# 1. Login with valid credentials
curl -X POST http://localhost:8080/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"admin@shop.com","password":"SecurePass123"}'
# Expected: 200, JWT token returned

# 2. Login with wrong password
curl -X POST http://localhost:8080/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"admin@shop.com","password":"WrongPass"}'
# Expected: 401, "Invalid credentials."

# 3. Access protected endpoint without token
curl -X GET http://localhost:8080/customers
# Expected: 401

# 4. Access public tracking endpoint without token
curl -X GET http://localhost:8080/service-orders/<os-uuid>
# Expected: 200 (public endpoint, AC-56)
```
