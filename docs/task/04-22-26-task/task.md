# Task Breakdown: Repair Shop MVP — Back-end

**Date:** 2026-04-22
**Plan:** docs/plan/04-22-26-plan/plan.md
**Spec:** docs/spec/04-22-26-spec/spec.md
**Output directory:** docs/task/04-22-26-task/
**Status:** complete

---

## Phase 1: Setup

**Purpose:** Configure project dependencies and application properties for all profiles.

- [ ] T001 Update pom.xml with all required dependencies (Spring Data JPA, PostgreSQL, Flyway, Spring Security, JJWT 0.12.6, SpringDoc OpenAPI, MockK, SpringMockK, spring-security-test) and Kotlin Maven plugin additions (spring + jpa compiler plugins, kotlin-maven-noarg) in `pom.xml`
- [ ] T002 [P] Create main application properties with datasource, JPA (ddl-auto=validate), Flyway, JWT, and pagination defaults in `src/main/resources/application.properties`
- [ ] T003 [P] Create dev profile properties with local PostgreSQL connection in `src/main/resources/application-dev.properties`
- [ ] T004 [P] Create test profile properties with test database configuration in `src/test/resources/application-test.properties`

**Checkpoint**: Project compiles with `./mvnw compile`. All dependencies resolve. Application starts (fails on missing DB — expected).

---

## Phase 2: Foundational

**Purpose:** Build all blocking prerequisites — error handling, database schema, domain core (enums, value objects, entities), and repositories. All user story work depends on this phase being 100% complete.

### Error handling

- [ ] T005 [P] Create DomainException hierarchy (EntityNotFoundException, DuplicateEntityException, InvalidStateTransitionException, InsufficientStockException, InvalidDocumentException, InvalidPlateException) in `src/main/kotlin/com/cao/repairshop/config/DomainException.kt`
- [ ] T006 Create GlobalExceptionHandler with @ControllerAdvice returning ProblemDetail (RFC 7807) for all domain exceptions in `src/main/kotlin/com/cao/repairshop/config/GlobalExceptionHandler.kt` (depends on T005)

### Flyway migrations

- [ ] T007 [P] Create migration V1__create_enums.sql defining status_os (9 values) and status_service (3 values) PostgreSQL enums in `src/main/resources/db/migration/V1__create_enums.sql`
- [ ] T008 [P] Create migration V2__create_tb_customer.sql with UUID PK, name, document (UNIQUE), email, phone, birth_date, created, updated in `src/main/resources/db/migration/V2__create_tb_customer.sql`
- [ ] T009 [P] Create migration V3__create_tb_vehicle.sql with UUID PK, customer_id FK, plate, brand, model, color, manufacturing_date, last_maintenance, created, updated in `src/main/resources/db/migration/V3__create_tb_vehicle.sql`
- [ ] T010 [P] Create migration V4__create_tb_insume.sql with UUID PK, name, brand, sku_id, quantity, price, unity_price in `src/main/resources/db/migration/V4__create_tb_insume.sql`
- [ ] T011 [P] Create migration V5__create_tb_user.sql with UUID PK, name, function, email (UNIQUE), password in `src/main/resources/db/migration/V5__create_tb_user.sql`
- [ ] T012 [P] Create migration V6__create_tb_service_order.sql with UUID PK, customer_id FK, vehicle_id FK, status DEFAULT 'RECEIVED', total_price, enter_time, end_time, valid_date, created, updated in `src/main/resources/db/migration/V6__create_tb_service_order.sql`
- [ ] T013 [P] Create migration V7__create_tb_service_order_history.sql with UUID PK, service_order_id FK, status, register_time, interval_time (INTERVAL) in `src/main/resources/db/migration/V7__create_tb_service_order_history.sql`
- [ ] T014 [P] Create migration V8__create_tb_service.sql with UUID PK, service_order FK, description, price, estimated_time, status DEFAULT 'INITIATED', created, updated in `src/main/resources/db/migration/V8__create_tb_service.sql`
- [ ] T015 [P] Create migration V9__create_tb_service_insume.sql with composite PK (id_tb_service, id_tb_insume), FKs to tb_service and tb_insume in `src/main/resources/db/migration/V9__create_tb_service_insume.sql`
- [ ] T016 [P] Create migration V10__create_tb_service_history.sql with UUID PK, service_id FK, status, register_time, interval_time (INTERVAL) in `src/main/resources/db/migration/V10__create_tb_service_history.sql`
- [ ] T017 Create migration V11__create_indexes.sql with indexes on all FK columns and frequently queried fields (depends on T008, T009, T010, T011, T012, T013, T014, T015, T016) in `src/main/resources/db/migration/V11__create_indexes.sql`
- [ ] T018 Create migration V12__seed_insumes.sql loading 250 insumes from CSV data (depends on T010) in `src/main/resources/db/migration/V12__seed_insumes.sql`

### Enums (state machines)

- [ ] T019 [P] Create StatusOs enum with 9 states (RECEIVED, IN_DIAGNOSIS, WAITING_APPROVAL, APPROVED, REFUSED, IN_EXECUTION, FINALIZED, PAID, CANCELED) and allowedTransitions() method in `src/main/kotlin/com/cao/repairshop/serviceorder/StatusOs.kt`
- [ ] T020 [P] Create StatusService enum with 3 states (INITIATED, PENDING, FINALIZED) and allowedTransitions() method in `src/main/kotlin/com/cao/repairshop/service/StatusService.kt`

### Value objects

- [ ] T021 [P] Create Document value object with CPF/CNPJ validation (check digits, reject all-same-digit), input normalization (strip formatting), and type detection by length in `src/main/kotlin/com/cao/repairshop/cadastro/Document.kt`
- [ ] T022 [P] Create Plate value object with regex validation for ABC-1234 and ABC1D23 formats, uppercase normalization, and dash removal in `src/main/kotlin/com/cao/repairshop/cadastro/Plate.kt`
- [ ] T023 [P] Create Email value object with format validation in `src/main/kotlin/com/cao/repairshop/cadastro/Email.kt`

### Entities

- [ ] T024 Create Customer JPA entity with UUID PK (id_tb_customer), name, Document VO, Email VO, phone, birth_date, created/updated timestamps, @Table("tb_customer") (depends on T021, T023) in `src/main/kotlin/com/cao/repairshop/cadastro/Customer.kt`
- [ ] T025 Create Vehicle JPA entity with UUID PK (id_tb_vehicle), customer_id FK, Plate VO, brand, model, color, manufacturing_date, last_maintenance, created/updated timestamps, @Table("tb_vehicle") (depends on T022, T024) in `src/main/kotlin/com/cao/repairshop/cadastro/Vehicle.kt`
- [ ] T026 [P] Create Insume JPA entity with UUID PK (id_tb_insume), name, brand, sku_id, quantity, price, unity_price, deductStock()/restoreStock() behaviors, @Table("tb_insume") in `src/main/kotlin/com/cao/repairshop/inventory/Insume.kt`
- [ ] T027 [P] Create User JPA entity with UUID PK (id_tb_user), name, function, email, password (hashed), @Table("tb_user") in `src/main/kotlin/com/cao/repairshop/user/User.kt`
- [ ] T028 Create ServiceOrder JPA entity with UUID PK (id_tb_service_order), customer_id FK, vehicle_id FK, StatusOs, total_price, enter_time, end_time, valid_date, created/updated, advanceStatus()/approve()/refuse()/recalculateTotalPrice() behaviors, @Table("tb_service_order") (depends on T019) in `src/main/kotlin/com/cao/repairshop/serviceorder/ServiceOrder.kt`
- [ ] T029 Create ServiceOrderHistory JPA entity with UUID PK (id_tb_service_order_history), service_order_id FK, status, register_time, interval_time (Duration), @Table("tb_service_order_history") (depends on T028) in `src/main/kotlin/com/cao/repairshop/serviceorder/ServiceOrderHistory.kt`
- [ ] T030 Create Service JPA entity with UUID PK (id_tb_service), service_order FK, description, price, estimated_time, StatusService, created/updated, advanceStatus() behavior, @Table("tb_service") (depends on T020) in `src/main/kotlin/com/cao/repairshop/service/Service.kt`
- [ ] T031 [P] Create ServiceInsumeId embeddable composite key class with id_tb_service and id_tb_insume UUIDs in `src/main/kotlin/com/cao/repairshop/service/ServiceInsumeId.kt`
- [ ] T032 Create ServiceInsume JPA entity with composite PK (ServiceInsumeId), @ManyToOne to Service and Insume, @Table("tb_service_insume") (depends on T030, T031) in `src/main/kotlin/com/cao/repairshop/service/ServiceInsume.kt`
- [ ] T033 Create ServiceHistory JPA entity with UUID PK (id_tb_service_history), service_id FK, status, register_time, interval_time (Duration), @Table("tb_service_history") (depends on T030) in `src/main/kotlin/com/cao/repairshop/service/ServiceHistory.kt`

### Repositories

- [ ] T034 [P] Create CustomerRepository interface extending JpaRepository with findByDocument() query method in `src/main/kotlin/com/cao/repairshop/cadastro/CustomerRepository.kt` (depends on T024)
- [ ] T035 [P] Create VehicleRepository interface extending JpaRepository in `src/main/kotlin/com/cao/repairshop/cadastro/VehicleRepository.kt` (depends on T025)
- [ ] T036 [P] Create InsumeRepository interface extending JpaRepository with @Lock(PESSIMISTIC_WRITE) findByIdForUpdate() method in `src/main/kotlin/com/cao/repairshop/inventory/InsumeRepository.kt` (depends on T026)
- [ ] T037 [P] Create UserRepository interface extending JpaRepository with findByEmail() query method in `src/main/kotlin/com/cao/repairshop/user/UserRepository.kt` (depends on T027)
- [ ] T038 [P] Create ServiceOrderRepository interface extending JpaRepository in `src/main/kotlin/com/cao/repairshop/serviceorder/ServiceOrderRepository.kt` (depends on T028)
- [ ] T039 [P] Create ServiceOrderHistoryRepository interface extending JpaRepository with findByServiceOrderId() method in `src/main/kotlin/com/cao/repairshop/serviceorder/ServiceOrderHistoryRepository.kt` (depends on T029)
- [ ] T040 [P] Create ServiceRepository interface extending JpaRepository with findByServiceOrder() method in `src/main/kotlin/com/cao/repairshop/service/ServiceRepository.kt` (depends on T030)
- [ ] T041 [P] Create ServiceHistoryRepository interface extending JpaRepository with findByServiceId() method in `src/main/kotlin/com/cao/repairshop/service/ServiceHistoryRepository.kt` (depends on T033)

**Checkpoint**: Application starts with PostgreSQL, Flyway applies all 12 migrations successfully, all entities compile, `./mvnw compile` passes. Schema matches ER diagram. All foundational infrastructure complete — user story work can begin.

---

## Phase 3: Register & Manage Customers (US-1, US-2)

**Purpose:** Implement customer registration with document validation and full CRUD management. Cadastro context is independent — this phase can run in parallel with Phase 4 and Phase 5.

- [ ] T042 [P] [US-1] Create CustomerDto with CreateCustomerRequest, UpdateCustomerRequest, and CustomerResponse data classes in `src/main/kotlin/com/cao/repairshop/cadastro/CustomerDto.kt` — satisfies FR-001, FR-004
- [ ] T043 [US-1] Create CustomerService with create (document uniqueness INV-001, CPF/CNPJ validation INV-008, email validation INV-009), findById, findAll (paginated), update, and delete (reject if linked OS exist) methods in `src/main/kotlin/com/cao/repairshop/cadastro/CustomerService.kt` (depends on T042) — satisfies AC-1, AC-2, AC-3, AC-4, AC-5, AC-6, AC-7, AC-8, AC-9, AC-10, FR-001, FR-002, FR-003, FR-004, FR-027
- [ ] T044 [US-1] Create CustomerController with POST /customers, GET /customers, GET /customers/{id}, PUT /customers/{id}, DELETE /customers/{id} endpoints in `src/main/kotlin/com/cao/repairshop/cadastro/CustomerController.kt` (depends on T043) — satisfies AC-1, AC-2, AC-3, AC-4, AC-5, AC-6, AC-7, AC-8, AC-9, AC-10, FR-001, FR-002, FR-003, FR-004, NFR-005, NFR-006, NFR-007

**Checkpoint**: Customer CRUD fully functional via API. Document validation rejects invalid CPF/CNPJ. Duplicate documents return 409. Paginated list returns customers. Delete blocked when OS exist.

---

## Phase 4: Register & Manage Vehicles (US-3, US-4)

**Purpose:** Implement vehicle registration with plate validation and full CRUD management. Can run in parallel with Phase 3 and Phase 5 (independent files within Cadastro).

- [ ] T045 [P] [US-3] Create VehicleDto with CreateVehicleRequest, UpdateVehicleRequest, and VehicleResponse data classes in `src/main/kotlin/com/cao/repairshop/cadastro/VehicleDto.kt` — satisfies FR-005, FR-007
- [ ] T046 [US-3] Create VehicleService with create (plate validation INV-007, customer existence check, future manufacturing_date rejection), findById, findAll (paginated), update, and delete (reject if linked OS exist) methods in `src/main/kotlin/com/cao/repairshop/cadastro/VehicleService.kt` (depends on T045) — satisfies AC-11, AC-12, AC-13, AC-14, AC-15, AC-16, AC-17, FR-005, FR-006, FR-007
- [ ] T047 [US-3] Create VehicleController with POST /vehicles, GET /vehicles, GET /vehicles/{id}, PUT /vehicles/{id}, DELETE /vehicles/{id} endpoints in `src/main/kotlin/com/cao/repairshop/cadastro/VehicleController.kt` (depends on T046) — satisfies AC-11, AC-12, AC-13, AC-14, AC-15, AC-16, AC-17, FR-005, FR-006, FR-007, NFR-005, NFR-006, NFR-007

**Checkpoint**: Vehicle CRUD fully functional via API. Plate validation accepts ABC-1234 and ABC1D23, rejects invalid formats. Customer existence validated. Future dates rejected.

---

## Phase 5: Insume Inventory Management (US-12)

**Purpose:** Implement insume CRUD with stock management behaviors. Estoque context is independent — can run in parallel with Phase 3 and Phase 4.

- [ ] T048 [P] [US-12] Create InsumeDto with CreateInsumeRequest, UpdateInsumeRequest, and InsumeResponse data classes in `src/main/kotlin/com/cao/repairshop/inventory/InsumeDto.kt` — satisfies FR-020
- [ ] T049 [US-12] Create InsumeService with create, findById, findAll (paginated), update, delete (reject if linked to active service), deductStock (INV-004 with pessimistic lock), and restoreStock methods in `src/main/kotlin/com/cao/repairshop/inventory/InsumeService.kt` (depends on T048) — satisfies AC-42, AC-43, AC-44, AC-45, FR-020, FR-013, FR-014
- [ ] T050 [US-12] Create InsumeController with POST /insumes, GET /insumes, GET /insumes/{id}, PUT /insumes/{id}, DELETE /insumes/{id} endpoints in `src/main/kotlin/com/cao/repairshop/inventory/InsumeController.kt` (depends on T049) — satisfies AC-42, AC-43, AC-44, AC-45, FR-020, NFR-005, NFR-006, NFR-007

**Checkpoint**: Insume CRUD fully functional via API. Stock management (deduct/restore) works. Delete blocked when linked to active service.

---

## Phase 6: Service Catalog & Status Tracking (US-10, US-11)

**Purpose:** Implement service CRUD with status state machine (INITIATED -> PENDING -> FINALIZED) and history tracking. Depends on Phase 5 (InsumeService for insume linking).

- [ ] T051 [P] [US-10] Create ServiceDto with CreateServiceRequest, UpdateServiceRequest, ServiceResponse, and StatusUpdateRequest data classes in `src/main/kotlin/com/cao/repairshop/service/ServiceDto.kt` — satisfies FR-017, FR-018
- [ ] T052 [US-10] Create ServiceService with create (status INITIATED, link insumes via ServiceInsume), findById, findAll (paginated), update, delete (reject if linked to active OS), advanceStatus (INV-003 state machine guard), and history recording methods in `src/main/kotlin/com/cao/repairshop/service/ServiceService.kt` (depends on T051, T049) — satisfies AC-35, AC-36, AC-37, AC-38, AC-39, AC-40, AC-41, FR-017, FR-018, FR-019
- [ ] T053 [US-10] Create ServiceController with POST /services, GET /services, GET /services/{id}, PUT /services/{id}, DELETE /services/{id}, PATCH /services/{id}/status endpoints in `src/main/kotlin/com/cao/repairshop/service/ServiceController.kt` (depends on T052) — satisfies AC-35, AC-36, AC-37, AC-38, AC-39, AC-40, FR-017, FR-018, FR-019, NFR-005, NFR-006, NFR-007

**Checkpoint**: Service CRUD fully functional via API. Status transitions follow INITIATED -> PENDING -> FINALIZED. Invalid transitions rejected with 422. History recorded at each transition.

---

## Phase 7: Service Order Lifecycle (US-5, US-6, US-7, US-8, US-9, US-13)

**Purpose:** Implement the central business aggregate — service order creation with automatic budget, 9-state lifecycle, customer approval/refusal with atomic stock deduction, OS tracking, and metrics. This is the most complex phase, integrating all contexts.

- [ ] T054 [P] [US-5] Create ServiceOrderDto with CreateServiceOrderRequest (with nested service definitions), ServiceOrderResponse, ServiceOrderSummaryResponse, StatusUpdateRequest, ApprovalRequest, and MetricsResponse data classes in `src/main/kotlin/com/cao/repairshop/serviceorder/ServiceOrderDto.kt` — satisfies FR-008, FR-015, FR-016, FR-024
- [ ] T055 [US-5] Create ServiceOrderService with createServiceOrder (validate customer/vehicle exist, create services with insumes, calculate total_price INV-006, initial history entry), advanceStatus (INV-002 state machine guard, INV-005 cross-aggregate FINALIZED check, history with interval_time), approve (WAITING_APPROVAL -> APPROVED with atomic stock deduction INV-004 via pessimistic lock), refuse (WAITING_APPROVAL -> REFUSED), findById (with full history), findAll (paginated), recalculateTotalPrice, and getMetrics (average execution time IN_EXECUTION -> FINALIZED) methods in `src/main/kotlin/com/cao/repairshop/serviceorder/ServiceOrderService.kt` (depends on T054, T043, T046, T049, T052) — satisfies AC-18, AC-19, AC-20, AC-21, AC-22, AC-23, AC-24, AC-25, AC-26, AC-27, AC-28, AC-29, AC-30, AC-31, AC-32, AC-33, AC-34, AC-46, AC-47, AC-48, AC-57, FR-008, FR-009, FR-010, FR-011, FR-012, FR-013, FR-014, FR-015, FR-016, FR-024, FR-025, FR-026
- [ ] T056 [US-5] Create ServiceOrderController with POST /service-orders, GET /service-orders, GET /service-orders/{id} (public), PATCH /service-orders/{id}/status, POST /service-orders/{id}/approve, GET /service-orders/metrics endpoints in `src/main/kotlin/com/cao/repairshop/serviceorder/ServiceOrderController.kt` (depends on T055) — satisfies AC-18, AC-19, AC-20, AC-21, AC-22, AC-23, AC-24, AC-25, AC-26, AC-27, AC-28, AC-29, AC-30, AC-31, AC-32, AC-33, AC-34, AC-46, AC-47, AC-48, AC-57, FR-008, FR-009, FR-010, FR-011, FR-012, FR-013, FR-014, FR-015, FR-016, FR-023, FR-024, FR-025, FR-026, NFR-005, NFR-006, NFR-007

**Checkpoint**: Full OS lifecycle works end-to-end. OS created with automatic budget. Status transitions follow state machine. Customer approval deducts stock atomically. Insufficient stock blocks approval. Metrics endpoint returns average execution time. Public tracking endpoint accessible without auth.

---

## Phase 8: User Authentication & Security (US-14, US-15)

**Purpose:** Implement JWT-based authentication, user management, and endpoint protection. Cross-cutting security applied to all previously built controllers.

- [ ] T057 [P] [US-14] Create JwtProperties as @ConfigurationProperties class binding jwt.secret, jwt.expiration from application.properties in `src/main/kotlin/com/cao/repairshop/config/JwtProperties.kt` — satisfies FR-021
- [ ] T058 [US-14] Create JwtService with generateToken(userId, function) and validateToken(token) methods returning claims (user_id, function, expiration) in `src/main/kotlin/com/cao/repairshop/config/JwtService.kt` (depends on T057) — satisfies AC-49, AC-52, FR-021
- [ ] T059 [US-14] Create JwtAuthenticationFilter extending OncePerRequestFilter that extracts Bearer token from Authorization header, validates via JwtService, and sets SecurityContext in `src/main/kotlin/com/cao/repairshop/config/JwtAuthenticationFilter.kt` (depends on T058) — satisfies AC-52, AC-53, AC-55, FR-022
- [ ] T060 [P] [US-14] Create UserDto with CreateUserRequest, LoginRequest, and TokenResponse data classes in `src/main/kotlin/com/cao/repairshop/user/UserDto.kt` — satisfies FR-021
- [ ] T061 [US-14] Create UserService with createUser (hash password with BCrypt INV-011, validate unique email INV-010) and authenticate (validate credentials, return JWT) methods in `src/main/kotlin/com/cao/repairshop/user/UserService.kt` (depends on T058, T060) — satisfies AC-49, AC-50, AC-51, FR-021, FR-028
- [ ] T062 [US-14] Create AuthController with POST /auth/login endpoint (public) in `src/main/kotlin/com/cao/repairshop/user/AuthController.kt` (depends on T061) — satisfies AC-49, AC-50, AC-51, FR-021
- [ ] T063 [US-15] Create SecurityConfig with SecurityFilterChain (Kotlin DSL), stateless sessions, CSRF disabled, public endpoints (POST /auth/login, GET /service-orders/{id}, Swagger), protected endpoints (all others), BCryptPasswordEncoder(10), and JwtAuthenticationFilter registration in `src/main/kotlin/com/cao/repairshop/config/SecurityConfig.kt` (depends on T059) — satisfies AC-53, AC-54, AC-55, AC-56, FR-022, FR-023, NFR-002

**Checkpoint**: Login returns valid JWT. Protected endpoints reject requests without token (401). Public endpoints (login, OS tracking, Swagger) accessible without token. Expired/malformed tokens rejected.

---

## Phase 9: Tests

**Purpose:** Comprehensive test suite covering domain-critical code (status transitions, budget calculation, stock deduction, validations) targeting 80%+ coverage on these areas.

### Unit tests — Value Objects & Enums

- [ ] T064 [P] Create DocumentTest with cases for valid CPF, valid CNPJ, invalid check digits, all-same-digit rejection, formatting normalization in `src/test/kotlin/com/cao/repairshop/cadastro/DocumentTest.kt` — validates INV-008
- [ ] T065 [P] Create PlateTest with cases for ABC-1234 format, ABC1D23 format, lowercase normalization, invalid format rejection in `src/test/kotlin/com/cao/repairshop/cadastro/PlateTest.kt` — validates INV-007
- [ ] T066 [P] Create StatusOsTest with cases for all 9 states, valid transitions, invalid transitions rejection, terminal states (PAID, CANCELED) return empty set in `src/test/kotlin/com/cao/repairshop/serviceorder/StatusOsTest.kt` — validates INV-002
- [ ] T067 [P] Create StatusServiceTest with cases for INITIATED -> PENDING -> FINALIZED transitions, invalid transitions rejection in `src/test/kotlin/com/cao/repairshop/service/StatusServiceTest.kt` — validates INV-003

### Unit tests — Services (MockK)

- [ ] T068 [P] Create CustomerServiceTest with mocked CustomerRepository testing create (success, duplicate document 409, invalid CPF/CNPJ 400, blank name 400), findById (success, not found 404), findAll (paginated), update (success, not found), delete (success, blocked by OS 409) in `src/test/kotlin/com/cao/repairshop/cadastro/CustomerServiceTest.kt` — validates AC-1 to AC-10
- [ ] T069 [P] Create VehicleServiceTest with mocked VehicleRepository/CustomerRepository testing create (success, invalid plate 400, customer not found 404, future date 400), findAll, update, delete (blocked by OS 409) in `src/test/kotlin/com/cao/repairshop/cadastro/VehicleServiceTest.kt` — validates AC-11 to AC-17
- [ ] T070 [P] Create InsumeServiceTest with mocked InsumeRepository testing CRUD operations, deductStock (success, insufficient stock rejection INV-004), restoreStock, delete (blocked if linked 409) in `src/test/kotlin/com/cao/repairshop/inventory/InsumeServiceTest.kt` — validates AC-42 to AC-48
- [ ] T071 [P] Create ServiceServiceTest with mocked ServiceRepository/ServiceHistoryRepository testing create (status INITIATED), advanceStatus (valid transitions, invalid rejection INV-003), history recording, delete (blocked if active OS 409) in `src/test/kotlin/com/cao/repairshop/service/ServiceServiceTest.kt` — validates AC-35 to AC-41
- [ ] T072 [P] Create ServiceOrderServiceTest with mocked repositories testing createServiceOrder (budget calculation INV-006, initial history), advanceStatus (valid transitions INV-002, FINALIZED requires all services FINALIZED INV-005), approve (stock deduction INV-004, atomic rollback on insufficient stock), refuse, findById, getMetrics in `src/test/kotlin/com/cao/repairshop/serviceorder/ServiceOrderServiceTest.kt` — validates AC-18 to AC-34, AC-46 to AC-48, AC-57
- [ ] T073 [P] Create UserServiceTest with mocked UserRepository/JwtService testing createUser (password hashing INV-011, duplicate email 409), authenticate (success, wrong password 401, nonexistent email 401) in `src/test/kotlin/com/cao/repairshop/user/UserServiceTest.kt` — validates AC-49 to AC-51
- [ ] T074 [P] Create JwtServiceTest testing token generation (contains user_id, function, expiration), token validation (valid, expired, malformed) in `src/test/kotlin/com/cao/repairshop/config/JwtServiceTest.kt` — validates AC-49, AC-52

### Integration tests — Controllers (@WebMvcTest + MockMvc)

- [ ] T075 [P] Create CustomerControllerTest with MockMvc testing all CRUD endpoints, request validation, error responses (400, 404, 409), pagination in `src/test/kotlin/com/cao/repairshop/cadastro/CustomerControllerTest.kt` — validates AC-1 to AC-10
- [ ] T076 [P] Create VehicleControllerTest with MockMvc testing all CRUD endpoints, plate format validation, customer not found, pagination in `src/test/kotlin/com/cao/repairshop/cadastro/VehicleControllerTest.kt` — validates AC-11 to AC-17
- [ ] T077 [P] Create InsumeControllerTest with MockMvc testing all CRUD endpoints, validation, pagination in `src/test/kotlin/com/cao/repairshop/inventory/InsumeControllerTest.kt` — validates AC-42 to AC-45
- [ ] T078 [P] Create ServiceControllerTest with MockMvc testing CRUD + status advancement endpoints, invalid transitions in `src/test/kotlin/com/cao/repairshop/service/ServiceControllerTest.kt` — validates AC-35 to AC-41
- [ ] T079 [P] Create ServiceOrderControllerTest with MockMvc testing creation, status advancement, approval/refusal, tracking (public), metrics endpoints in `src/test/kotlin/com/cao/repairshop/serviceorder/ServiceOrderControllerTest.kt` — validates AC-18 to AC-34, AC-46 to AC-48, AC-57
- [ ] T080 [P] Create AuthControllerTest with MockMvc testing login (success, invalid credentials), protected endpoint access (with/without token), public endpoint access, expired token handling in `src/test/kotlin/com/cao/repairshop/user/AuthControllerTest.kt` — validates AC-49 to AC-56

**Checkpoint**: All tests pass with `./mvnw test`. Domain-critical code (status transitions, budget calculation, stock deduction, validations) achieves 80%+ coverage (NFR-004).

---

## Phase 10: Polish

**Purpose:** Final cross-cutting concerns — API documentation and delivery readiness.

- [ ] T081 [P] Create OpenApiConfig with Swagger UI customization (title, description, version), JWT security scheme (Bearer token), and grouped API definitions by bounded context in `src/main/kotlin/com/cao/repairshop/config/OpenApiConfig.kt` — satisfies NFR-003
- [ ] T082 Validate quickstart scenarios from docs/plan/04-22-26-plan/quickstart.md — run the 5 validation scenarios to confirm end-to-end functionality

**Checkpoint**: Swagger UI accessible at /swagger-ui.html. All API endpoints documented with request/response schemas. Security scheme configured. All quickstart scenarios pass. Ready for delivery.

---

## Dependency Graph

```
Phase 1 (Setup) → blocks everything
  |
Phase 2 (Foundational) → blocks all user stories
  |
  +---> Phase 3 (US-1, US-2: Customer) ----+
  |                                          |
  +---> Phase 4 (US-3, US-4: Vehicle) [P] --+--> Phase 7 (US-5-9, US-13: Service Order)
  |                                          |         |
  +---> Phase 5 (US-12: Insume) [P] --------+         |
           |                                           |
           +--> Phase 6 (US-10, US-11: Service) ------+
                                                       |
Phase 8 (US-14, US-15: Security) <--------------------+
  |
Phase 9 (Tests) → depends on all story + security phases
  |
Phase 10 (Polish) → depends on everything
```

---

## Coverage Summary

| Story | Tasks | ACs covered | FRs covered | Status |
|-------|-------|-------------|-------------|--------|
| US-1 | T042, T043, T044 | AC-1, AC-2, AC-3, AC-4, AC-5 | FR-001, FR-002, FR-003, FR-027 | Complete |
| US-2 | T043, T044 | AC-6, AC-7, AC-8, AC-9, AC-10 | FR-004 | Complete |
| US-3 | T045, T046, T047 | AC-11, AC-12, AC-13, AC-14 | FR-005, FR-006 | Complete |
| US-4 | T046, T047 | AC-15, AC-16, AC-17 | FR-007 | Complete |
| US-5 | T054, T055, T056 | AC-18, AC-19, AC-20, AC-21, AC-22 | FR-008, FR-009, FR-026 | Complete |
| US-6 | T055, T056 | AC-23, AC-24, AC-25, AC-26, AC-57 | FR-010, FR-011, FR-025 | Complete |
| US-7 | T055, T056 | AC-27, AC-28, AC-29, AC-30 | FR-012 | Complete |
| US-8 | T055, T056 | AC-31, AC-32 | FR-015, FR-016, FR-023 | Complete |
| US-9 | T055, T056 | AC-33, AC-34 | FR-024 | Complete |
| US-10 | T051, T052, T053 | AC-35, AC-36, AC-37 | FR-017 | Complete |
| US-11 | T052, T053 | AC-38, AC-39, AC-40, AC-41 | FR-018, FR-019 | Complete |
| US-12 | T048, T049, T050 | AC-42, AC-43, AC-44, AC-45 | FR-020 | Complete |
| US-13 | T055, T056 | AC-46, AC-47, AC-48 | FR-013, FR-014 | Complete |
| US-14 | T057, T058, T059, T060, T061, T062 | AC-49, AC-50, AC-51, AC-52 | FR-021, FR-028 | Complete |
| US-15 | T063 | AC-53, AC-54, AC-55, AC-56 | FR-022, FR-023 | Complete |

### NFR Coverage

| NFR | Covered by | Status |
|-----|-----------|--------|
| NFR-001 | Performance inherent to stack choices (pagination, indexes T017) | Complete |
| NFR-002 | T063 (BCryptPasswordEncoder with cost 10) | Complete |
| NFR-003 | T081 (OpenApiConfig) | Complete |
| NFR-004 | T064-T080 (test suite targeting 80%+ domain-critical coverage) | Complete |
| NFR-005 | T006 (GlobalExceptionHandler with ProblemDetail) | Complete |
| NFR-006 | T024-T033 (all entities use UUID v4 PKs) | Complete |
| NFR-007 | T044, T047, T050, T053, T056 (all controllers use Spring pagination) | Complete |

### Invariant Coverage

| Invariant | Enforced by | Tested by |
|-----------|------------|-----------|
| INV-001 | T043 (CustomerService) | T068 (CustomerServiceTest) |
| INV-002 | T019 (StatusOs), T055 (ServiceOrderService) | T066 (StatusOsTest), T072 (ServiceOrderServiceTest) |
| INV-003 | T020 (StatusService), T052 (ServiceService) | T067 (StatusServiceTest), T071 (ServiceServiceTest) |
| INV-004 | T026 (Insume.deductStock), T049 (InsumeService), T055 (ServiceOrderService) | T070 (InsumeServiceTest), T072 (ServiceOrderServiceTest) |
| INV-005 | T055 (ServiceOrderService cross-aggregate check) | T072 (ServiceOrderServiceTest) |
| INV-006 | T055 (ServiceOrderService.recalculateTotalPrice) | T072 (ServiceOrderServiceTest) |
| INV-007 | T022 (Plate VO) | T065 (PlateTest) |
| INV-008 | T021 (Document VO) | T064 (DocumentTest) |
| INV-009 | T023 (Email VO) | T068 (CustomerServiceTest) |
| INV-010 | T061 (UserService) | T073 (UserServiceTest) |
| INV-011 | T061 (UserService) | T073 (UserServiceTest) |

### Validation Checklist

**Completeness:**
- [X] Every user story from the spec has at least one task (US-1 to US-15)
- [X] Every acceptance criterion (AC) is satisfied by at least one task (AC-1 to AC-57)
- [X] Every functional requirement (FR) is satisfied by at least one task (FR-001 to FR-028)
- [X] Every artifact in the plan's project structure has a task that creates it

**Ordering:**
- [X] No task appears before its dependencies
- [X] No dependency cycles exist
- [X] Phases respect the order: Setup -> Foundational -> User Stories -> Polish
- [X] Within each story phase, layer order is respected (domain -> application -> presentation)

**Format:**
- [X] Every task has checkbox `- [ ]`
- [X] Every task has sequential ID (T001 to T082)
- [X] IDs are unique and continuous
- [X] Every story-phase task has `[US-N]` label
- [X] Parallel tasks correctly marked with `[P]`
- [X] Every task has a description with exact file path
- [X] Dependencies use parenthetical format
- [X] Story tasks have `— satisfies AC-X, FR-XXX`

**Traceability:**
- [X] Every story-phase task references at least one AC or FR
- [X] No orphan tasks
- [X] Coverage summary shows all stories, ACs, and FRs covered

**Phases:**
- [X] Phase 1 (Setup) has no `[US-N]` labels
- [X] Phase 2 (Foundational) has no `[US-N]` labels
- [X] Phase 2 contains ALL blocking prerequisites (migrations, error handling, enums, VOs, entities, repos)
- [X] Stories ordered by priority (all P1 first, P2 last)
- [X] Phase 10 (Polish) has no `[US-N]` labels
- [X] Every phase ends with a Checkpoint
