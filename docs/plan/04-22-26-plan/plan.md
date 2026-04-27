# Implementation Plan: Repair Shop MVP — Back-end

**Date:** 2026-04-22
**Spec:** docs/spec/04-22-26-spec/spec.md
**Output directory:** docs/plan/04-22-26-plan/
**Status:** complete

---

## Summary

Build the back-end MVP for a vehicle repair shop management system covering 5 bounded contexts (Cadastro, Ordem de Servico, Servico, Estoque, Usuarios) with 15 user stories, 9-state OS lifecycle, automatic budget calculation, atomic stock deduction, and JWT authentication. The system is a Spring Boot 4.x monolith organized as domain packages, backed by PostgreSQL, with Flyway-managed migrations and 80%+ test coverage on domain-critical code.

---

## Technical Context

- **Language/Version:** Kotlin 2.2.21, Java 24
- **Primary Dependencies:** Spring Boot 4.0.5, Spring Data JPA, Spring Security, JJWT 0.12.x, Flyway, SpringDoc OpenAPI 2.8.6, Bean Validation (Jakarta)
- **Storage:** PostgreSQL (Spring Data JPA + Hibernate 7.x)
- **Testing:** JUnit 5 + MockK + SpringMockK + Spring Boot test slices (`@DataJpaTest`, `@WebMvcTest`)
- **Target Platform:** JVM 24 (server-side REST API)
- **Project Type:** web-service
- **Performance Goals:** All API responses < 500ms for datasets up to 10,000 records per table (NFR-001)
- **Constraints:** No Docker/docker-compose (explicitly excluded). No front-end. No payment gateway. No email notifications. Single-tenant (one shop).
- **Scale/Scope:** MVP — 9 tables, ~250 seed insumes, academic evaluation (POSTECH 15SOAT)

**Consultation points:**
- Implementation agent (`spring-kotlin/principles/spring-boot.md`) → Stack conventions, package structure, Spring Boot 4.x package changes
- Implementation agent (`spring-kotlin/principles/spring-data-jpa.md`) → JPA entity patterns, pagination, transactions
- Implementation agent (`spring-kotlin/principles/spring-security.md`) → JWT filter, SecurityFilterChain Kotlin DSL, password encoding
- Implementation agent (`spring-kotlin/capabilities/create-migration.md`) → Flyway convention, `ddl-auto=validate`
- Research (`research.md`) → R-001 through R-009 resolved all unknowns

---

## Constitution Check

**Constitution source:** `.claude/adapters/spec-kit-constitution.md`

| Gate | Result | Justification |
|------|--------|---------------|
| **Simplicity** | PASS | 5 packages (one per bounded context) + 1 config package. No unnecessary abstractions. No generic base classes beyond error handling. |
| **Anti-abstraction** | PASS | Uses Spring framework directly — no wrapper layers. Value Objects (Document, Plate, Email) are domain concepts, not abstractions. |
| **Tests first** | PASS | Test strategy defined (unit + integration). Coverage target 80% on domain-critical code. Test structure mirrors source. |
| **Real integration** | PASS | `@DataJpaTest` with real PostgreSQL (Testcontainers or embedded). No mocked repositories in integration tests. |
| **DDD boundaries** | PASS | All cross-context references use ID only (confirmed by architect agent). No shared entities between packages. |
| **Naming conventions** | PASS | Tables: `tb_` prefix. PKs: `id_tb_<entity>`. FKs: `<entity>_id`. Timestamps: `created`/`updated`. Enums: PascalCase. |

**Consultation points:**
- Architect agent (`architect/CONSTRAINTS.md`) → Cross-context references approved, package boundaries verified
- Modeler agent (`modeler/RULES.md`) → Entity conventions, enum conventions, aggregate boundaries confirmed

---

## Project Structure

```
src/
  main/
    kotlin/
      com/cao/repairshop/
        RepairshopApplication.kt
        cadastro/                        # Bounded Context: Cadastro
          Customer.kt                    # Entity (aggregate root)
          Vehicle.kt                     # Entity (child of Cadastro aggregate)
          Document.kt                    # Value Object (CPF/CNPJ)
          Plate.kt                       # Value Object (vehicle plate)
          Email.kt                       # Value Object (email)
          CustomerRepository.kt
          VehicleRepository.kt
          CustomerService.kt
          VehicleService.kt
          CustomerController.kt
          VehicleController.kt
          CustomerDto.kt                 # Request/Response DTOs
          VehicleDto.kt
        serviceorder/                    # Bounded Context: Ordem de Servico
          ServiceOrder.kt                # Entity (aggregate root)
          ServiceOrderHistory.kt         # Entity (child)
          StatusOs.kt                    # Enum with state machine
          ServiceOrderRepository.kt
          ServiceOrderHistoryRepository.kt
          ServiceOrderService.kt
          ServiceOrderController.kt
          ServiceOrderDto.kt
        service/                         # Bounded Context: Servico
          Service.kt                     # Entity (aggregate root)
          ServiceInsume.kt               # Entity (pivot N:N)
          ServiceInsumeId.kt             # Composite key class
          ServiceHistory.kt              # Entity (child)
          StatusService.kt               # Enum with state machine
          ServiceRepository.kt
          ServiceHistoryRepository.kt
          ServiceService.kt
          ServiceController.kt
          ServiceDto.kt
        inventory/                       # Bounded Context: Estoque
          Insume.kt                      # Entity (aggregate root)
          InsumeRepository.kt
          InsumeService.kt
          InsumeController.kt
          InsumeDto.kt
        user/                            # Bounded Context: Usuarios
          User.kt                        # Entity (aggregate root)
          UserRepository.kt
          UserService.kt
          AuthController.kt
          UserDto.kt
        config/                          # Cross-cutting configuration
          SecurityConfig.kt              # SecurityFilterChain + JWT filter setup
          JwtService.kt                  # JWT generation/validation
          JwtAuthenticationFilter.kt     # OncePerRequestFilter
          JwtProperties.kt               # @ConfigurationProperties for JWT
          OpenApiConfig.kt               # SpringDoc customization
          GlobalExceptionHandler.kt      # @ControllerAdvice with ProblemDetail
          DomainException.kt             # Base exception hierarchy
    resources/
      application.properties             # Main config (datasource, JPA, JWT, pagination)
      application-dev.properties         # Dev profile
      application-test.properties        # Test profile
      db/
        migration/
          V1__create_enums.sql
          V2__create_tb_customer.sql
          V3__create_tb_vehicle.sql
          V4__create_tb_insume.sql
          V5__create_tb_user.sql
          V6__create_tb_service_order.sql
          V7__create_tb_service_order_history.sql
          V8__create_tb_service.sql
          V9__create_tb_service_insume.sql
          V10__create_tb_service_history.sql
          V11__create_indexes.sql
          V12__seed_insumes.sql
  test/
    kotlin/
      com/cao/repairshop/
        cadastro/
          CustomerServiceTest.kt
          VehicleServiceTest.kt
          CustomerControllerTest.kt
          VehicleControllerTest.kt
          DocumentTest.kt                # Value Object unit tests
          PlateTest.kt
        serviceorder/
          ServiceOrderServiceTest.kt
          ServiceOrderControllerTest.kt
          StatusOsTest.kt                # State machine unit tests
        service/
          ServiceServiceTest.kt
          ServiceControllerTest.kt
          StatusServiceTest.kt
        inventory/
          InsumeServiceTest.kt
          InsumeControllerTest.kt
        user/
          UserServiceTest.kt
          AuthControllerTest.kt
        config/
          JwtServiceTest.kt
    resources/
      application-test.properties
```

**Consultation points:**
- Implementation agent (`spring-kotlin/principles/spring-boot.md`) → Domain-first package structure, stereotypes
- Modeling agent (`ddd/principles/boundaries.md`) → One package per bounded context, all layers within
- Architect agent (`architect/CONSTRAINTS.md`) → 5 bounded contexts confirmed: cadastro, serviceorder, service, inventory, user

---

## Dependency Order

### Phase 1 — Foundation (blocks everything)

| Item | Description | Dependencies |
|------|-------------|-------------|
| 1.1 | Add Maven dependencies (pom.xml) | None |
| 1.2 | Configure application.properties (datasource, JPA, Flyway, JWT, pagination) | None |
| 1.3 | Create DomainException hierarchy (config/DomainException.kt) | None |
| 1.4 | Create GlobalExceptionHandler (config/GlobalExceptionHandler.kt) | 1.3 |
| 1.5 | Create Flyway migrations V1-V11 (DDL for all 9 tables + indexes) | None |
| 1.6 | Create seed migration V12 (250 insumes from CSV) | 1.5 |

### Phase 2 — Domain core (enums + entities)

| Item | Description | Dependencies |
|------|-------------|-------------|
| 2.1 | Create StatusOs enum with state machine transitions | None |
| 2.2 | Create StatusService enum with state machine transitions | None |
| 2.3 | Create Value Objects: Document, Plate, Email | None |
| 2.4 | Create entities: Customer, Vehicle | 2.3 |
| 2.5 | Create entities: Insume | None |
| 2.6 | Create entities: User | None |
| 2.7 | Create entities: ServiceOrder, ServiceOrderHistory | 2.1 |
| 2.8 | Create entities: Service, ServiceInsume, ServiceInsumeId, ServiceHistory | 2.2 |

### Phase 3 — Persistence layer (repositories)

| Item | Description | Dependencies |
|------|-------------|-------------|
| 3.1 | CustomerRepository, VehicleRepository | 2.4 |
| 3.2 | InsumeRepository (with pessimistic lock method) | 2.5 |
| 3.3 | UserRepository | 2.6 |
| 3.4 | ServiceOrderRepository, ServiceOrderHistoryRepository | 2.7 |
| 3.5 | ServiceRepository, ServiceHistoryRepository | 2.8 |

### Phase 4A — Independent contexts (can be built in parallel)

**Cadastro:**

| Item | Description | Dependencies |
|------|-------------|-------------|
| 4A.1 | CustomerService + CustomerDto | 3.1 |
| 4A.2 | CustomerController | 4A.1 |
| 4A.3 | VehicleService + VehicleDto | 3.1 |
| 4A.4 | VehicleController | 4A.3 |

**Estoque:**

| Item | Description | Dependencies |
|------|-------------|-------------|
| 4A.5 | InsumeService + InsumeDto | 3.2 |
| 4A.6 | InsumeController | 4A.5 |

### Phase 4B — Dependent contexts (require Phase 4A)

**Servico:**

| Item | Description | Dependencies |
|------|-------------|-------------|
| 4B.1 | ServiceService + ServiceDto (CRUD + status transitions + history) | 3.5, 4A.5 |
| 4B.2 | ServiceController | 4B.1 |

**Ordem de Servico:**

| Item | Description | Dependencies |
|------|-------------|-------------|
| 4B.3 | ServiceOrderService + ServiceOrderDto (create with budget, status transitions, history, approve/refuse with stock deduction, metrics) | 3.4, 4A.1, 4A.3, 4A.5, 4B.1 |
| 4B.4 | ServiceOrderController | 4B.3 |

### Phase 5 — Security (cross-cutting)

| Item | Description | Dependencies |
|------|-------------|-------------|
| 5.1 | JwtProperties + JwtService (token generation/validation) | 1.2 |
| 5.2 | JwtAuthenticationFilter | 5.1 |
| 5.3 | UserService + UserDto (create user, authenticate) | 3.3, 5.1 |
| 5.4 | AuthController (login endpoint) | 5.3 |
| 5.5 | SecurityConfig (filter chain, public/protected endpoints) | 5.2 |

### Phase 6 — OpenAPI + final config

| Item | Description | Dependencies |
|------|-------------|-------------|
| 6.1 | OpenApiConfig (Swagger customization, security schemes) | 5.5 |

### Phase 7 — Tests

| Item | Description | Dependencies |
|------|-------------|-------------|
| 7.1 | Unit tests: Value Objects (Document, Plate, Email) | 2.3 |
| 7.2 | Unit tests: StatusOs, StatusService state machines | 2.1, 2.2 |
| 7.3 | Unit tests: Services (mocked repositories via MockK) | Phase 4A, 4B |
| 7.4 | Integration tests: Controllers (MockMvc) | Phase 4A, 4B, 5 |
| 7.5 | Integration tests: Repositories (@DataJpaTest) | Phase 3 |
| 7.6 | Unit tests: JwtService | 5.1 |
| 7.7 | Integration tests: Auth flow (login, protected endpoints) | Phase 5 |

**Consultation points:**
- Spec-planner agent (`spec-planner/principles/dependency-ordering.md`) → Inside-out: domain first, interfaces last
- Architect agent (`architect/CONSTRAINTS.md`) → Cross-context dependencies determine ordering
- Modeler agent (`modeler/RULES.md`) → Aggregate boundaries determine which entities can be built in parallel

---

## Artefacts

| File | Description |
|------|-------------|
| `data-model.md` | Formal entity definitions, types, constraints, relationships |
| `research.md` | Technical research findings (R-001 through R-009) |
| `quickstart.md` | 5 validation scenarios for initial setup verification |
| `contracts/cadastro.md` | API contracts for Customer + Vehicle endpoints |
| `contracts/service-order.md` | API contracts for Service Order endpoints |
| `contracts/service.md` | API contracts for Service endpoints |
| `contracts/inventory.md` | API contracts for Insume endpoints |
| `contracts/auth.md` | API contracts for Authentication endpoints |

---

## Dependencies to add (pom.xml)

```xml
<!-- Data -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-jpa</artifactId>
</dependency>
<dependency>
    <groupId>org.postgresql</groupId>
    <artifactId>postgresql</artifactId>
    <scope>runtime</scope>
</dependency>
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-flyway</artifactId>
</dependency>

<!-- Security -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-security</artifactId>
</dependency>
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-api</artifactId>
    <version>0.12.6</version>
</dependency>
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-impl</artifactId>
    <version>0.12.6</version>
    <scope>runtime</scope>
</dependency>
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-jackson</artifactId>
    <version>0.12.6</version>
    <scope>runtime</scope>
</dependency>

<!-- Kotlin JPA plugin (no-arg constructor) -->
<!-- Already in build plugins, ensure kotlin-jpa is listed -->

<!-- Test -->
<dependency>
    <groupId>io.mockk</groupId>
    <artifactId>mockk-jvm</artifactId>
    <version>1.13.16</version>
    <scope>test</scope>
</dependency>
<dependency>
    <groupId>com.ninja-squad</groupId>
    <artifactId>springmockk</artifactId>
    <version>4.0.2</version>
    <scope>test</scope>
</dependency>
<dependency>
    <groupId>org.springframework.security</groupId>
    <artifactId>spring-security-test</artifactId>
    <scope>test</scope>
</dependency>
```

**Kotlin Maven plugin additions:**

```xml
<compilerPlugins>
    <plugin>spring</plugin>
    <plugin>jpa</plugin>
</compilerPlugins>
<dependencies>
    <dependency>
        <groupId>org.jetbrains.kotlin</groupId>
        <artifactId>kotlin-maven-noarg</artifactId>
        <version>${kotlin.version}</version>
    </dependency>
</dependencies>
```
