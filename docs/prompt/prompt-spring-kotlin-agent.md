# Prompt: Agente Spring Boot + Kotlin para Sistema Cognitivo

Voce e um engenheiro de software senior, especialista em Spring Boot, Kotlin e arquitetura de aplicacoes JVM. Voce tambem e especialista em Claude Code e em criar documentos de referencia otimizados para consumo por LLMs.

Sua tarefa e gerar o agente `agents/spring-kotlin/` dentro de `.claude/` no repositorio do projeto. Este agente fornece fundamentos de implementacao em Spring Boot + Kotlin que outros agentes e o spec-kit consomem durante a geracao de codigo.

---

## Stack alvo

- **Kotlin 2.x** + **Spring Boot 4.x**
- Plugins Kotlin essenciais para Spring: `kotlin-spring` (allopen) e `-Xjsr305=strict`
- O agente cobre: Spring Data JPA, Spring Security, Bean Validation, SpringDoc OpenAPI, JUnit 5

---

## Contexto do sistema cognitivo

Este agente faz parte de um sistema cognitivo com multiplos agentes. Existem dois tipos:
- **Agentes genericos (agnosticos):** Conhecimento puro reutilizavel entre projetos. Este agente (`spring-kotlin/`) e um deles.
- **Agentes especificos:** Aplicam o conhecimento generico ao dominio concreto de cada projeto.

O agente `spring-kotlin/` e **generico e agnostico** — NAO deve conter referencias a nenhum dominio, tabela, enum ou regra de negocio de nenhum projeto especifico. Ele sabe sobre Spring Boot + Kotlin. O dominio vem dos agentes especificos e dos arquivos de contexto de cada projeto.

---

## Objetivo

Criar o agente `agents/spring-kotlin/` com:
- Principios de implementacao em Spring Boot + Kotlin
- Capacidades atomicas de "como fazer" para tarefas comuns
- Formato de saida padrao
- Exemplos minimalistas (mecanica, nao dominio)

O agente sera consumido pelo spec-kit durante a fase `/speckit.implement` para gerar codigo Kotlin/Spring Boot idiomatico e correto.

---

## Restricoes

- **NAO** incluir referencias a nenhum dominio especifico (tabelas, enums, regras de negocio de qualquer projeto)
- **NAO** usar `data class` para entities JPA — usar `class` (explicar o motivo nos principios)
- **NAO** gerar JSON como formato de saida — usar Markdown estruturado
- **NAO** incluir Co-Authored-By ou atribuicao ao Claude/IA
- Todos os exemplos devem ser **minimalistas** — ilustrar a mecanica, nao construir um dominio
- O conteudo deve ser baseado na documentacao oficial do Spring Boot 4.x e Kotlin 2.x

---

## Estrutura de arquivos a gerar

```
.claude/agents/spring-kotlin/
  AGENT.md
  principles/
    kotlin-idioms.md
    spring-boot.md
    spring-data-jpa.md
    spring-security.md
    testing.md
    openapi.md
    error-handling.md
    validation.md
  capabilities/
    create-entity.md
    create-repository.md
    create-service.md
    create-controller.md
    create-test.md
    create-dto.md
    create-migration.md
    create-enum.md
    create-config.md
  OUTPUT.md
  EXAMPLES.md
```

**Total:** 20 arquivos.

---

## Conteudo detalhado de cada arquivo

### AGENT.md

Deve descrever:
- O que e este agente e qual seu proposito
- Que ele e generico e agnostico (nao sabe nada do dominio)
- Como ele se relaciona com os outros agentes (e referenciado, nao invocado diretamente)
- Indice dos principios e capabilities disponiveis
- Que ele e consumido pelo spec-kit durante a fase de implementacao

---

### principles/kotlin-idioms.md

Idiomas e padroes da linguagem Kotlin relevantes para Spring Boot. Deve cobrir:

- **data class:** Usar para DTOs, Value Objects e ConfigurationProperties. NAO usar para entities JPA (nao permite heranca, gera equals/hashCode em todos os campos, copy() cria nova instancia com novo ID, toString() pode disparar lazy loading)
- **sealed class/sealed interface:** Usar para hierarquias fechadas com `when` exhaustivo (sem `else` necessario desde Kotlin 2.1). Ideal para representar estados, resultados de operacoes, tipos de erro
- **@JvmInline value class:** Wrappers de tipo com zero overhead em runtime. Ideal para IDs tipados (`CustomerId`, `ServiceOrderId`), evitando confusao de parametros
- **Null safety:** `?.let {}` para operacoes em nullables, `?:` (elvis) para defaults, evitar `!!` — preferir `requireNotNull()` quando a ausencia e um bug
- **Scope functions:** `let` (transformar nullable), `apply` (configurar objeto), `also` (efeitos colaterais/logging), `run` (executar bloco com contexto), `with` (operacoes em objeto)
- **Extension functions:** Para adicionar comportamento a classes existentes sem heranca
- **Injecao por construtor:** `class MyService(private val repo: MyRepo)` — sem `@Autowired`, sem `lateinit var`
- **Companion objects:** Para factory methods e constantes
- **Destructuring:** `val (_, status) = getResult()` — util para ignorar componentes
- **String templates:** `"Customer $id not found"` em vez de concatenacao
- **Colecoes imutaveis:** Usar `List`, `Set`, `Map` em assinaturas, nao `MutableList`. Mutabilidade so quando necessario internamente
- **Sequence vs List:** Para cadeias longas de `map/filter/flatMap` em colecoes grandes, usar `asSequence()` para avaliacao lazy
- **Delegation:** `class MyList(list: List<String>) : List<String> by list` — delegar implementacao sem heranca, alternativa nativa do Kotlin

---

### principles/spring-boot.md

Principios e convencoes de Spring Boot 4.x. Deve cobrir:

- **Estrutura de projeto:** Package por dominio (`com.example.customer/`, `com.example.order/`), cada package com Entity, Controller, Service, Repository
- **Injecao de dependencias:** Preferir construtor (Kotlin torna natural). `@Service`, `@Repository`, `@RestController` como stereotypes
- **Auto-configuration:** Spring Boot detecta entities (`@Entity`), repositories (`JpaRepository`), controllers automaticamente via component scanning
- **Application class:** `runApplication<MyApplication>(*args)` — funcao top-level Kotlin
- **Externalized configuration:** `application.properties`/`application.yml`, profiles (`application-{profile}`), `@ConfigurationProperties` com data classes
- **Plugin kotlin-spring (allopen):** Abre classes anotadas com `@Component`, `@Service`, `@Configuration`, `@Entity` etc. para proxies do Spring/Hibernate
- **Plugin -Xjsr305=strict:** Null safety rigorosa — tipos de retorno de APIs Java sao tratados como non-null por padrao
- **Profiles:** `spring.profiles.active`, `@Profile("production")`, multi-document properties com `#---`
- **spring-boot-docker-compose:** Modulo que auto-detecta docker-compose.yml e gerencia containers em dev
- **Mudanca de pacotes no 4.x:** Spring Boot 4.x reestruturou pacotes internos. Documentar os imports corretos e essencial — imports do 3.x causam erros de compilacao. Exemplos: `@DataJpaTest` mudou de `org.springframework.boot.test.autoconfigure.orm.jpa` para `org.springframework.boot.data.jpa.test.autoconfigure`

---

### principles/spring-data-jpa.md

Principios de acesso a dados com Spring Data JPA + Kotlin. Deve cobrir:

- **Entities em Kotlin:** Usar `class` (nao `data class`). Properties com `var` para campos mutaveis. Plugin `kotlin-spring` (allopen) abre a classe automaticamente
- **Construtor no-args:** JPA spec exige. Com o plugin `no-arg` do Kotlin (configurado via `kotlin-spring`), o construtor e gerado automaticamente
- **Primary keys:** `@Id` com `@GeneratedValue` ou UUID gerado na aplicacao. Campo como `val` (imutavel apos criacao)
- **Repositories:** Interface extends `JpaRepository<Entity, UUID>`. Queries por method name (`findByName`, `findAllByStatus`). `@Query` para JPQL customizado
- **Transacoes:** `@Transactional` em services, nao em repositories (repositories ja sao transacionais). Transacao nao deve cruzar boundaries de agregados
- **Relacionamentos:** `@ManyToOne`, `@OneToMany(mappedBy = "...")`, `@ManyToMany` com `@JoinTable`. Preferir LAZY fetching. Cuidado com N+1 queries
- **Naming strategy:** Spring Boot usa `SpringPhysicalNamingStrategy` por padrao (camelCase → snake_case)
- **Auditoria:** `@CreatedDate`, `@LastModifiedDate` com `@EntityListeners(AuditingEntityListener::class)` e `@EnableJpaAuditing`
- **Paginacao:** `Pageable` como parametro em queries, retorno `Page<T>`
- **Projecoes:** Interface-based projections para queries que nao precisam da entity inteira

---

### principles/spring-security.md

Principios de seguranca com Spring Security + JWT em Kotlin. Deve cobrir:

- **SecurityFilterChain:** Configuracao via Kotlin DSL (`http { authorizeHttpRequests { ... } }`). Requer import `org.springframework.security.config.annotation.web.invoke`
- **JWT como Resource Server:** `oauth2ResourceServer { jwt { } }` para validacao de tokens
- **Custom JWT filter:** Quando nao usar OAuth2 Resource Server — criar filter que extrai e valida o token manualmente
- **Password encoding:** `BCryptPasswordEncoder` como `@Bean`. Nunca armazenar senha em texto plano
- **Authorization:** `authorize("/public/**", permitAll)`, `authorize("/admin/**", hasRole("ADMIN"))`, `authorize(anyRequest, authenticated)`
- **CORS:** `@CrossOrigin` por controller ou `WebMvcConfigurer.addCorsMappings()` global
- **CSRF:** Desabilitar para APIs stateless (`csrf { disable() }`)
- **Kotlin DSL vs Java DSL:** Kotlin DSL e mais idiomatico e conciso. Sempre preferir
- **AuthenticationManager customizado:** `DaoAuthenticationProvider(userDetailsService)` + `ProviderManager` como `@Bean`
- **Custom UserDetailsService:** `@Bean fun userDetailsService() = CustomUserDetailsService()` — implementar `UserDetailsService` interface para carregar usuario do banco
- **PasswordEncoder:** `PasswordEncoderFactories.createDelegatingPasswordEncoder()` como alternativa moderna ao `BCryptPasswordEncoder` direto — suporta multiplos algoritmos e migracao

---

### principles/testing.md

Principios de teste com JUnit 5 em Kotlin + Spring Boot. Deve cobrir:

- **Test slices:** Usar a annotation mais especifica possivel:
  - `@DataJpaTest` — testa repositories com banco em memoria (ou real com `@AutoConfigureTestDatabase(replace = NONE)`)
  - `@WebMvcTest` — testa controllers isolados (sem carregar o contexto inteiro)
  - `@JsonTest` — testa serializacao/deserializacao JSON
  - `@SpringBootTest` — teste de integracao completo (usar com parcimonia)
- **TestEntityManager:** Injetar em `@DataJpaTest` para persistir entities no teste
- **MockK vs Mockito:** MockK e mais idiomatico para Kotlin (`every { }`, `verify { }`, `mockk<MyService>()`). Mockito funciona mas e mais verboso
- **Injecao no construtor:** `class MyTests(@Autowired val repo: MyRepo)` — funciona em testes Kotlin
- **Assertions:** AssertJ (`assertThat(x).isEqualTo(y)`) — ja incluso no starter-test
- **Nomenclatura:** Metodos de teste com backticks: `` fun `should return 404 when customer not found`() ``
- **Cobertura:** JaCoCo para cobertura. Focar em dominios criticos
- **Teste de validacao:** Usar `@WebMvcTest` para testar que inputs invalidos retornam 400
- **MockMvcTester (Spring Boot 4.x):** Nova API fluente com AssertJ: `assertThat(mvc.get().uri("/")).hasStatusOk()` — preferir sobre MockMvc classico
- **RestTestClient (Spring Boot 4.x):** Alternativa ao WebTestClient para testes MVC com `@AutoConfigureRestTestClient`
- **Imports corretos no 4.x:** `@DataJpaTest` esta em `org.springframework.boot.data.jpa.test.autoconfigure`, `TestEntityManager` em `org.springframework.boot.jpa.test.autoconfigure`, `@AutoConfigureMockMvc` em `org.springframework.boot.webmvc.test.autoconfigure`

---

### principles/openapi.md

Principios de documentacao de API com SpringDoc OpenAPI. Deve cobrir:

- **Dependency:** `springdoc-openapi-starter-webmvc-ui` (ja no pom.xml)
- **Configuracao:** `application.yml` com `springdoc.swagger-ui.path`, `springdoc.packages-to-scan`, `springdoc.api-docs.path`
- **Annotations nos controllers:** `@Tag(name = "Customers")` para agrupar, `@Operation(summary = "...")` para descrever, `@ApiResponse` para status codes
- **Schemas:** `@Schema` em DTOs para documentar campos
- **Agrupamento por bounded context:** `GroupedOpenApi.builder().group("cadastro").pathsToMatch("/customers/**", "/vehicles/**")` — separar endpoints por dominio
- **Seguranca:** Configurar o Swagger para enviar JWT no header Authorization

---

### principles/error-handling.md

Principios de tratamento de erros em Spring Boot + Kotlin. Deve cobrir:

- **@ControllerAdvice:** Centralizar tratamento de exceptions num unico lugar. Estender `ResponseEntityExceptionHandler`
- **Hierarquia de exceptions de dominio:** Criar exceptions especificas do dominio (`CustomerNotFoundException`, `InsufficientStockException`) que NAO dependem de HTTP
- **Mapeamento exception → HTTP:** No `@ControllerAdvice`, mapear cada exception de dominio para o status HTTP correto (404, 409, 422, etc.)
- **ProblemDetail (RFC 7807):** Usar o formato padrao para respostas de erro. Spring Boot 4.x suporta nativamente
- **Nunca expor stack traces:** Em producao, retornar apenas mensagem amigavel. Stack traces so em logs
- **Kotlin sealed class para resultados:** `sealed class Result<out T>` com `data class Success<T>(val data: T)` e `data class Failure(val error: DomainError)` — alternativa a exceptions para erros esperados
- **Validacao vs erro de dominio:** Erro de validacao (400) e diferente de erro de dominio (409/422). Separar tratamento
- **ErrorAttributeOptions:** Spring Boot 4.x permite controlar quais atributos incluir na resposta de erro via `ErrorAttributeOptions.Include` (MESSAGE, BINDING_ERRORS, EXCEPTION, STACK_TRACE, STATUS)

---

### principles/validation.md

Principios de validacao de dados em Spring Boot + Kotlin. Deve cobrir:

- **Jakarta Bean Validation:** `@NotNull`, `@NotEmpty`, `@Size`, `@Pattern`, `@Email`, `@Min`, `@Max` nos campos de DTOs
- **@Valid em @RequestBody:** Dispara validacao automatica no controller. Erros retornados como 400 Bad Request
- **@Validated em services:** Para method-level validation (`@Size`, `@NotNull` em parametros de metodos)
- **Kotlin null safety vs @NotNull:** Campos non-null em Kotlin (`val name: String`) ja impedem null em tempo de compilacao. `@NotNull` e redundante nesses casos — usar `@NotNull` apenas em campos nullable (`val name: String?`)
- **Custom validators:** Implementar `ConstraintValidator<Annotation, T>` para validacoes customizadas (ex: CPF, CNPJ, placa de veiculo)
- **Cascade com @Valid:** Usar `@Valid` em campos de DTOs aninhados para propagar validacao
- **Mensagens de erro:** Customizar via `message` na annotation ou `messages.properties`

---

### capabilities/create-entity.md

Guia pratico de como criar uma entity JPA em Kotlin. Deve incluir:

- Usar `class` (nao `data class`) — explicar motivos (heranca, proxies, equals/hashCode)
- Annotations: `@Entity`, `@Table(name = "...")`, `@Id`, `@GeneratedValue` ou UUID manual
- Campos mutaveis com `var`, PK com `val`
- Relacionamentos: `@ManyToOne(fetch = LAZY)`, `@OneToMany(mappedBy = "...")`
- `equals()` e `hashCode()` manuais baseados apenas no ID
- `toString()` manual sem campos lazy-loaded
- Timestamps: `@CreatedDate`, `@LastModifiedDate` ou campos manuais
- Exemplo minimalista de entity com PK UUID, campos basicos e um relacionamento

---

### capabilities/create-repository.md

Guia pratico de como criar um repository Spring Data. Deve incluir:

- Interface extends `JpaRepository<Entity, UUID>`
- Queries por method name: `findByName`, `findAllByStatus`, `existsByDocument`
- `@Query` com JPQL para queries complexas
- Optional vs nullable em Kotlin: retornar `Entity?` em vez de `Optional<Entity>`
- Paginacao: `findAll(pageable: Pageable): Page<Entity>`
- Custom queries com `@Modifying` para updates/deletes
- Exemplo minimalista

---

### capabilities/create-service.md

Guia pratico de como criar um application service. Deve incluir:

- `@Service` com injecao por construtor
- Orquestrar use cases (nao conter logica de dominio — essa fica na entity/agregado)
- `@Transactional` no service, nao no repository
- Receber DTOs, converter para entities, chamar repository, retornar DTOs
- Tratar erros de dominio (lancar exceptions especificas)
- NAO injetar `HttpServletRequest` ou qualquer dependencia web — service e independente de transporte
- Exemplo minimalista

---

### capabilities/create-controller.md

Guia pratico de como criar um REST controller. Deve incluir:

- `@RestController` + `@RequestMapping("/base-path")`
- Injecao do service por construtor
- Endpoints: `@GetMapping`, `@PostMapping`, `@PutMapping`, `@DeleteMapping`, `@PatchMapping`
- `@Valid @RequestBody` para validacao de entrada
- `@PathVariable` e `@RequestParam` para parametros
- `ResponseEntity<T>` para controlar status code
- Annotations OpenAPI: `@Tag`, `@Operation`, `@ApiResponse`
- Nao conter logica de negocio — delegar para service
- Exemplo minimalista com CRUD basico

---

### capabilities/create-test.md

Guia pratico de como criar testes em Kotlin + Spring Boot. Deve incluir:

- **Teste de repository:** `@DataJpaTest`, `TestEntityManager`, assertions com AssertJ
- **Teste de controller:** `@WebMvcTest`, `MockMvc`, mockando o service
- **Teste de service:** Unit test com MockK, sem Spring context
- **Teste de integracao:** `@SpringBootTest` com banco real
- **Teste de validacao:** `@WebMvcTest` enviando payload invalido, esperando 400
- **Nomenclatura com backticks:** `` fun `should create customer when valid data`() ``
- Exemplo minimalista de cada tipo

---

### capabilities/create-dto.md

Guia pratico de como criar DTOs e mapeamento entity <-> DTO. Deve incluir:

- `data class` para request e response DTOs
- DTOs separados para request (input) e response (output)
- Annotations de validacao nos campos do request DTO (`@NotNull`, `@Size`, etc.)
- Funcoes de extensao para mapeamento: `fun CustomerRequest.toEntity(): Customer` e `fun Customer.toResponse(): CustomerResponse`
- NAO usar bibliotecas de mapeamento (MapStruct, ModelMapper) — funcoes de extensao sao mais idiomaticas em Kotlin
- Exemplo minimalista com request, response e funcoes de mapeamento

---

### capabilities/create-migration.md

Guia pratico de como criar migrations de banco. Deve incluir:

- Flyway como ferramenta de migration
- Localizacao: `src/main/resources/db/migration/`
- Naming convention: `V1__create_customers_table.sql`, `V2__create_vehicles_table.sql`
- Cada migration e um arquivo SQL com DDL
- Migrations sao imutaveis — nunca editar uma migration ja aplicada, criar nova
- Seeds de dados: `V999__seed_initial_data.sql` ou via `CommandLineRunner` em Kotlin
- Exemplo minimalista de migration criando uma tabela com PK UUID, campos e constraints

---

### capabilities/create-enum.md

Guia pratico de como criar e usar enums com JPA + Kotlin. Deve incluir:

- `enum class` em Kotlin para listas finitas de valores
- Mapeamento JPA: `@Enumerated(EnumType.STRING)` — nunca usar `ORDINAL` (quebra se reordenar)
- Quando usar `enum class` vs `sealed class`: enum para listas simples sem dados associados, sealed class para hierarquias com dados diferentes por variante
- Funcoes dentro do enum para logica relacionada (ex: transicoes validas de status)
- Custom converter JPA se o valor no banco nao corresponder ao nome do enum
- Exemplo minimalista com enum, annotation JPA e funcao de transicao

---

### capabilities/create-config.md

Guia pratico de como configurar o projeto Spring Boot. Deve incluir:

- `application.properties` vs `application.yml` — escolher um e manter consistente
- Profiles: `application-dev.properties`, `application-prod.properties`, `application-test.properties`
- Datasource: `spring.datasource.url`, `spring.datasource.username`, `spring.datasource.password`
- JPA: `spring.jpa.hibernate.ddl-auto=validate` (em producao), `spring.jpa.show-sql=false`
- Flyway: `spring.flyway.enabled=true`, `spring.flyway.locations=classpath:db/migration`
- JWT: Properties customizadas via `@ConfigurationProperties` com `data class`
- Secrets: Nunca hardcodar em properties — usar environment variables (`${JWT_SECRET}`)
- SpringDoc: `springdoc.swagger-ui.path=/swagger-ui.html`
- Exemplo minimalista de application.properties completo para desenvolvimento

---

### OUTPUT.md

Formato padrao de saida para quando o agente spring-kotlin e referenciado durante a geracao de codigo. A saida deve ser em Markdown estruturado com as seguintes secoes:

- **File:** caminho do arquivo a ser criado
- **Layer:** camada (entity, repository, service, controller, dto, config, test, migration)
- **Dependencies:** dependencias de outros arquivos
- **Code:** bloco de codigo Kotlin com annotations e imports completos
- **Notes:** observacoes sobre decisoes de implementacao

---

### EXAMPLES.md

Exemplos **minimalistas** que ilustram a mecanica de implementacao em Spring Boot + Kotlin. NAO construir dominios completos. Usar entidades abstratas como `Item`, `Order`, `User` apenas para demonstrar o padrao.

Incluir um exemplo curto para cada capability:
- Entity com UUID e relacionamento
- Repository com query por method name
- Service com transacao
- Controller com validacao
- DTO com mapeamento
- Teste de repository
- Migration SQL
- Enum com JPA

Cada exemplo deve ter no maximo 20-30 linhas de codigo.

---

## Ordem de implementacao

Implementar na seguinte ordem:

1. **AGENT.md** (primeiro — define o agente e indexa tudo)
2. **principles/** (segundo — fundamentos que as capabilities referenciam)
   - kotlin-idioms.md
   - spring-boot.md
   - spring-data-jpa.md
   - spring-security.md
   - testing.md
   - openapi.md
   - error-handling.md
   - validation.md
3. **capabilities/** (terceiro — consomem os principios)
   - create-entity.md
   - create-repository.md
   - create-service.md
   - create-controller.md
   - create-test.md
   - create-dto.md
   - create-migration.md
   - create-enum.md
   - create-config.md
4. **OUTPUT.md** (quarto — formato de saida)
5. **EXAMPLES.md** (quinto — exemplos minimalistas de cada capability)

---

## Base de conhecimento para geracao

O conteudo dos arquivos deve ser baseado na documentacao oficial:

- **Spring Boot 4.x:** https://docs.spring.io/spring-boot/index.html
- **Kotlin:** https://kotlinlang.org/docs/home.html
- **Spring Security 6.5:** https://docs.spring.io/spring-security/reference/
- **SpringDoc OpenAPI:** https://springdoc.org/
- **JUnit 5:** https://junit.org/junit5/docs/current/user-guide/

Pontos criticos da documentacao oficial que devem ser refletidos nos principios:

### Kotlin + JPA
- Entities JPA em Kotlin usam `class`, nao `data class` (problemas com heranca, proxies, equals/hashCode, toString com lazy loading)
- Plugin `kotlin-spring` (allopen) abre classes anotadas com `@Component`, `@Service`, `@Entity`, `@Configuration` para proxies automaticamente
- `-Xjsr305=strict` faz null safety do Kotlin respeitar annotations de nulabilidade do Java
- `@ConfigurationProperties` com `data class` para configuracao type-safe imutavel
- `runApplication<T>(*args)` como funcao top-level Kotlin (nao usar `SpringApplication.run()`)

### Spring Boot 4.x — Mudancas de pacotes (CRITICO)
O Spring Boot 4.x reestruturou pacotes significativamente em relacao ao 3.x. Os imports corretos sao:
- `org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest` (antes: `org.springframework.boot.test.autoconfigure.orm.jpa`)
- `org.springframework.boot.jpa.test.autoconfigure.TestEntityManager`
- `org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc`
- `org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase`
- `org.springframework.boot.resttestclient.autoconfigure.AutoConfigureRestTestClient`
- `org.springframework.boot.webtestclient.autoconfigure.AutoConfigureWebTestClient`

O agente DEVE documentar os imports corretos do Spring Boot 4.x em cada capability. Imports do Spring Boot 3.x vao causar erros de compilacao.

### Spring Boot 4.x — Novas APIs de teste
- `MockMvcTester` com AssertJ: `assertThat(mvc.get().uri("/")).hasStatusOk().hasBodyTextEqualTo("...")` — mais fluente que MockMvc classico
- `RestTestClient` — alternativa ao WebTestClient para testes MVC
- `@AutoConfigureRestTestClient` — nova annotation do Spring Boot 4.x
- Injecao por construtor em classes de teste: `class MyTests(@Autowired val repo: MyRepo)`

### Spring Security em Kotlin
- Kotlin DSL nativo: `http { authorizeHttpRequests { ... } }` com import `org.springframework.security.config.annotation.web.invoke`
- `DaoAuthenticationProvider(userDetailsService)` com `setPasswordEncoder` para auth customizado
- `ProviderManager(authenticationProvider)` como `@Bean` para publicar AuthenticationManager
- `PasswordEncoderFactories.createDelegatingPasswordEncoder()` como alternativa moderna ao BCrypt puro
- Custom `UserDetailsService` como bean: `@Bean fun userDetailsService() = CustomUserDetailsService()`

### Kotlin — Boas praticas de colecoes
- Usar tipos imutaveis em assinaturas de funcoes (`List`, `Set`, `Map`), nao `MutableList`
- `Sequence` vs `List`: usar `asSequence()` para cadeias longas de transformacao em colecoes grandes (avaliacao lazy)
- Delegation pattern nativo do Kotlin (`by`) como alternativa a heranca de implementacao

### Kotlin — Testes
- Metodos de teste com backticks: `` fun `should return 404 when customer not found`() ``
- MockK como alternativa idiomatica ao Mockito: `every { }`, `verify { }`, `mockk<T>()`
- AssertJ como framework de assertions (ja incluso no spring-boot-starter-test)

---

Agora gere o agente completo.
