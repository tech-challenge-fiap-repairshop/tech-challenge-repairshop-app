# API Contracts: Usuarios (Authentication)

**Plan:** docs/plan/04-22-26-plan/plan.md
**Spec:** docs/spec/04-22-26-spec/spec.md

---

## POST /auth/login

**Description:** Authenticate user and return JWT token
**Story:** US-14 (User login with JWT)
**Authentication:** **Public**

**Request body:**
```json
{
  "email": "string — required",
  "password": "string — required"
}
```

**Responses:**

- **200 OK** (AC-49):
```json
{
  "token": "eyJhbGciOiJIUzI1NiIs...",
  "type": "Bearer",
  "expires_in": 86400
}
```
  - Token payload contains: `user_id` (UUID), `function` (role), `exp` (expiration timestamp)

- **401 Unauthorized** (AC-50, AC-51):
  - `"Invalid credentials."` — wrong password OR nonexistent email (same message to prevent user enumeration)

---

## Authentication behavior (all protected endpoints)

**Story:** US-15 (Protect administrative APIs)

### Protected endpoints (require valid JWT)

All endpoints EXCEPT:
- `POST /auth/login` — public
- `GET /service-orders/{id}` — public (AC-56, customer tracking)
- `GET /swagger-ui/**` — public (documentation)
- `GET /v3/api-docs/**` — public (OpenAPI spec)

### Authentication header format

```
Authorization: Bearer <jwt-token>
```

### Error responses

- **401 Unauthorized** (AC-53): No `Authorization` header present
- **401 Unauthorized** (AC-55): Malformed JWT token
- **401 Unauthorized** (AC-52, E13): Expired JWT token — `"Token expired."`

### Security implementation

- `SecurityFilterChain` with Kotlin DSL (`http { ... }`)
- Custom `JwtAuthenticationFilter` (extends `OncePerRequestFilter`)
- Stateless sessions (`SessionCreationPolicy.STATELESS`)
- CSRF disabled (stateless REST API)
- `PasswordEncoder` — `BCryptPasswordEncoder(10)` (NFR-002)
