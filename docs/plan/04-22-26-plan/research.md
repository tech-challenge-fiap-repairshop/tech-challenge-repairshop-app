# Technical Research: Repair Shop MVP

**Date:** 2026-04-22
**Plan:** docs/plan/04-22-26-plan/plan.md
**Spec:** docs/spec/04-22-26-spec/spec.md

---

## R-001: Spring Data JPA + PostgreSQL for Spring Boot 4.0.5

**What was researched:** Compatible versions of Spring Data JPA and PostgreSQL driver for Spring Boot 4.0.5 + Kotlin 2.2.21 + Java 24.

**What was found:**
- Spring Boot 4.0.5 ships `spring-boot-starter-data-jpa` which pulls Spring Data JPA 4.x and Hibernate 7.x
- PostgreSQL JDBC driver is managed by Spring Boot's dependency management (no explicit version needed)
- The `kotlin-jpa` compiler plugin (no-arg) generates synthetic no-arg constructors for `@Entity` classes
- Hibernate 7.x requires Jakarta Persistence 3.2 (already included via Spring Boot 4.x)

**Decision:** Use `spring-boot-starter-data-jpa` and `postgresql` without explicit versions (managed by Spring Boot BOM).

**Rationale:** Spring Boot manages transitive dependency versions. Pinning versions manually risks incompatibility.

---

## R-002: JWT library for Spring Boot 4.x

**What was researched:** JWT generation and validation library compatible with Spring Boot 4.x and Jakarta EE 11.

**What was found:**
- `io.jsonwebtoken:jjwt` (JJWT) 0.12.x supports Java 17+ and Jakarta namespaces
- Spring Security's built-in `spring-boot-starter-oauth2-resource-server` provides JWT decoding but not generation (intended for external auth servers)
- For self-issued JWTs (our case: internal login, internal token generation), JJWT is the standard choice

**Decision:** Use JJWT 0.12.x (`jjwt-api`, `jjwt-impl`, `jjwt-jackson`) for JWT generation. Use a custom `OncePerRequestFilter` for JWT validation (not `oauth2-resource-server`).

**Rationale:** The spec requires internal JWT generation (login endpoint returns token). JJWT gives full control over claims (user_id, function, expiration) as specified.

**Alternatives rejected:**
- `spring-boot-starter-oauth2-resource-server` — designed for external auth servers, overkill for self-issued tokens, adds unnecessary complexity
- `com.auth0:java-jwt` — viable but less commonly used with Spring Boot, smaller community

---

## R-003: Flyway for database migrations

**What was researched:** Schema management strategy for PostgreSQL with Spring Boot 4.x.

**What was found:**
- Spring Boot 4.x supports Flyway 11.x via `spring-boot-starter-flyway` (new in Spring Boot 4.x, replaces manual Flyway dependency)
- Flyway manages schema versioning with `V{n}__{description}.sql` files
- Combined with `spring.jpa.hibernate.ddl-auto=validate`, Flyway owns the schema and Hibernate validates entity mappings

**Decision:** Use Flyway for all DDL changes. Set `ddl-auto=validate`. Place migrations in `src/main/resources/db/migration/`.

**Rationale:** Flyway provides reproducible, versioned schema changes. Hibernate DDL auto-generation is unreliable for production-grade schemas (misses indexes, constraints, enum types).

---

## R-004: Password hashing — BCrypt configuration

**What was researched:** BCrypt cost factor and Spring Security integration.

**What was found:**
- Spec requires BCrypt with cost factor >= 10 (NFR-002)
- Spring Security's `BCryptPasswordEncoder()` defaults to strength 10
- `PasswordEncoderFactories.createDelegatingPasswordEncoder()` also defaults to BCrypt but adds the `{bcrypt}` prefix to stored hashes (enables future algorithm migration)

**Decision:** Use `BCryptPasswordEncoder(10)` directly. Store raw bcrypt hashes without `{bcrypt}` prefix.

**Rationale:** The spec explicitly requires BCrypt. The delegating encoder adds unnecessary prefix complexity for an MVP with a single algorithm. If algorithm migration is needed later, a new migration can re-hash.

---

## R-005: PostgreSQL enum types vs VARCHAR

**What was researched:** Whether to use PostgreSQL native ENUM types or VARCHAR for status columns.

**What was found:**
- PostgreSQL ENUM types (`CREATE TYPE ... AS ENUM`) provide database-level validation but are hard to modify (adding values requires `ALTER TYPE`)
- VARCHAR with `@Enumerated(EnumType.STRING)` is more flexible and standard across databases
- Hibernate 7.x maps `@Enumerated(EnumType.STRING)` to VARCHAR columns by default

**Decision:** Use VARCHAR columns with `@Enumerated(EnumType.STRING)` in JPA entities. Enum validation happens in the application layer (Kotlin enum), not the database.

**Rationale:** Application-layer enum validation is sufficient for an MVP monolith. VARCHAR avoids migration complexity when adding enum values. The Kotlin enum classes (StatusOs, StatusService) are the source of truth.

---

## R-006: Pessimistic locking for stock deduction

**What was researched:** Concurrency control strategy for insume stock deduction (INV-004, edge case E6).

**What was found:**
- Spring Data JPA supports `@Lock(LockModeType.PESSIMISTIC_WRITE)` on repository methods
- Combined with `@Transactional`, this issues `SELECT ... FOR UPDATE` in PostgreSQL
- Prevents concurrent OS approvals from creating a race condition on stock deduction

**Decision:** Use `@Lock(LockModeType.PESSIMISTIC_WRITE)` on the repository method that fetches insumes during OS approval.

**Rationale:** The spec explicitly requires pessimistic locking for concurrent stock deduction (edge case E6). Optimistic locking would require retry logic, adding complexity without benefit for an MVP.

---

## R-007: Pagination defaults

**What was researched:** Default pagination configuration for list endpoints (NFR-007).

**What was found:**
- Spring Data's `Pageable` parameter in controllers auto-resolves `page`, `size`, and `sort` query params
- Default page size configurable via `spring.data.web.pageable.default-page-size=20`
- Response format: Spring's `Page<T>` includes `content`, `totalElements`, `totalPages`, `number`, `size`

**Decision:** Use Spring Data's built-in `Pageable` with default page size 20. All list endpoints return `Page<ResponseDto>`.

**Rationale:** Spring's pagination is battle-tested and matches NFR-007 requirements exactly. No custom pagination needed.

---

## R-008: SpringDoc OpenAPI compatibility

**What was researched:** SpringDoc version compatibility with Spring Boot 4.x.

**What was found:**
- The pom.xml already includes `springdoc-openapi-starter-webmvc-ui:2.8.6`
- SpringDoc 2.8.x supports Spring Boot 4.x
- Swagger UI available at `/swagger-ui/index.html`, API docs at `/v3/api-docs`

**Decision:** Keep existing SpringDoc 2.8.6 dependency. No changes needed.

**Rationale:** Already configured and compatible.

---

## R-009: Test strategy and framework

**What was researched:** Testing approach for 80% coverage target on domain-critical code (NFR-004).

**What was found:**
- Spring Boot 4.x test packages were reorganized (see spring-kotlin agent)
- `@DataJpaTest` now in `org.springframework.boot.data.jpa.test.autoconfigure`
- `@AutoConfigureMockMvc` now in `org.springframework.boot.webmvc.test.autoconfigure`
- MockK is the idiomatic Kotlin mocking library (replaces Mockito)

**Decision:** Use JUnit 5 + MockK for unit tests. Use Spring Boot test slices (`@DataJpaTest`, `@WebMvcTest`) for integration tests. Add `com.ninja-squad:springmockk` for MockK integration with Spring.

**Rationale:** MockK is idiomatic Kotlin. Spring test slices provide fast, focused integration tests. The 80% coverage target focuses on: status transitions, budget calculation, stock deduction, CPF/CNPJ/plate validations.

**Dependencies to add:**
- `com.ninja-squad:springmockk:4.0.2` (test scope)
- `io.mockk:mockk:1.13.16` (test scope)
