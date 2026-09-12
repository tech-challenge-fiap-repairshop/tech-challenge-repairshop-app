# RFC – Adoção de Kotlin e Spring Boot com Clean Architecture e DDD para o Core da Oficina

## DATA
15/08/2026

## STATUS
Encerrada – Aprovada

> **Nota de Encerramento:** Proposta amplamente discutida e aprovada pela equipe de engenharia, originando o [ADR-001](../ADRs/ADR-001-linguagem-e-framework-aplicacao-principal.md).

## RESUMO
Proposta de adoção da linguagem Kotlin 2.2 com o framework Spring Boot na JVM, estruturada sob os padrões arquiteturais de Clean Architecture (Arquitetura Limpa) e Domain-Driven Design (DDD) para o desenvolvimento do core transacional da oficina mecânica.

## PROBLEMA
O sistema de gestão da oficina mecânica (*RepairShop*) precisa gerenciar um ciclo operacional complexo e crítico: cadastro de clientes (com validação estrita de CPF/CNPJ), veículos (placas Mercosul e padrão cinza), estoque de insumos e peças, geração de orçamentos, máquina de estados finitos para ordens de serviço (OS) e faturamento.

No cenário inicial e nas primeiras discussões, identificaram-se os seguintes desafios técnicos:
1. **Risco de Inconsistências em Tempo de Execução:** Em linguagens dinâmicas ou no Java legado sem Null Safety nativo, falhas do tipo `NullPointerException` (NPE) podem quebrar fluxos em produção, corrompendo orçamentos ou travando ordens de serviço no meio de transições.
2. **Acoplamento de Regras de Negócio a Frameworks:** Projetos construídos no padrão tradicional "Anemic Domain Model" (Controller -> Service -> Repository do Spring Data) tendem a misturar anotações do framework (`@Entity`, `@Transactional`, `@Table`) com lógica pura de negócio. Isso dificulta a manutenção, impede a substituição de componentes de infraestrutura e torna a escrita de testes unitários lenta e pesada por exigir o contexto completo do Spring.
3. **Complexidade do Domínio da Oficina:** As regras de negócio possuem alta volatilidade e dependência de estados determinísticos (uma OS só pode ser executada após aprovação formal do cliente; uma OS cancelada não pode deduzir estoque).

## PROPOSTA TÉCNICA
Propõe-se a construção do monólito modular / core da aplicação utilizando **Kotlin 2.2** sobre a JVM, associado ao ecossistema **Spring Boot**, aplicando a separação estrita de camadas da **Clean Architecture** combinada com os conceitos táticos de **Domain-Driven Design (DDD)**.

```
                  +----------------------------------------------+
                  |         INFRASTRUCTURE / ADAPTERS            |
                  |  (Spring Web Controllers, Spring Data JPA,   |
                  |   Flyway, OpenAPI/Swagger, Feign Clients)    |
                  |                                              |
                  |     +----------------------------------+     |
                  |     |       APPLICATION LAYER          |     |
                  |     | (Use Cases, Gateways Interfaces) |     |
                  |     |                                  |     |
                  |     |     +----------------------+     |     |
                  |     |     |     DOMAIN LAYER     |     |     |
                  |     |     | (Entities, Value Obj,|     |     |
                  |     |     |  Domain Exceptions)  |     |     |
                  |     |     +----------------------+     |     |
                  |     +----------------------------------+     |
                  +----------------------------------------------+
```

### Componentes Principais da Proposta:

1. **Camada de Domínio Puro (`domain`):**
   - **Zero dependências externas:** Nenhuma anotação de Spring, Hibernate, Jackson ou bibliotecas de terceiros.
   - **Value Objects Imutáveis:** Implementação de `Cpf`, `Cnpj`, `Plate`, `Money` e `Status` como `data class` ou `inline value class` com auto-validação em tempo de instanciação.
   - **Agregados e Entidades:** Encapsulamento de regras invariantes e transições de estado controladas por métodos ricos (ex: `serviceOrder.approve()`, `serviceOrder.startExecution()`).
2. **Camada de Aplicação (`application`):**
   - **Casos de Uso (`usecases`):** Classes orquestradoras especializadas de responsabilidade única (ex: `CreateServiceOrderUseCase`, `ApproveQuoteUseCase`).
   - **Portas de Saída (`gateways`):** Interfaces puras de persistência e mensageria que aplicam o Princípio da Inversão de Dependência (DIP).
3. **Camada de Infraestrutura (`infra`):**
   - **Adaptadores de Entrada:** Rest Controllers do Spring MVC, DTOs de Request/Response e anotações OpenAPI (Swagger).
   - **Adaptadores de Saída:** Repositórios Spring Data JPA, entidades com mapeamento relacional (`@Entity`), migrations com Flyway e clientes HTTP.
4. **Linguagem Kotlin:**
   - Aproveitamento de Null Safety no sistema de tipos (`String?` vs `String`), *Smart Casts*, *Sealed Interfaces* para enumerações ricas de estado e *Extension Functions*.

## IMPACTO ESPERADO
- **Benefícios:**
  - Código limpo, expressivo e com menos da metade do boilerplate em relação a Java clássico.
  - Eliminação de NullPointerExceptions em tempo de execução nas regras de domínio.
  - Testes unitários com MockK extremamente rápidos (executam em milissegundos sem subir o contexto do Spring).
  - Facilidade de evolução e desacoplamento para futuras extrações em microsserviços.
- **Riscos e Mitigações:**
  - *Risco:* Curva de aprendizado da equipe com a síntaxe do Kotlin e os padrões de Clean Architecture.
  - *Mitigação:* Estabelecer templates de referência nos primeiros casos de uso e documentar convenções de código no Wiki.
- **Impacto Operacional e Custos:**
  - Execução estável na JVM com excelente suporte da comunidade e compatibilidade com ferramentas de observabilidade (OpenTelemetry Java Agent) e CI/CD.

## ALTERNATIVAS CONSIDERADAS
1. **Java Puro com Spring Boot Tradicional (Controller-Service-Repository):**
   - *Prós:* Arquitetura conhecida pela maioria dos desenvolvedores de mercado.
   - *Contras:* Risco elevado de modelos anêmicos, alto boilerplate (getters/setters/builders), vulnerabilidade a NPEs e forte acoplamento entre regras de negócio e o framework Spring/JPA.
2. **Node.js / TypeScript com NestJS:**
   - *Prós:* Rápido início de desenvolvimento e ecossistema JavaScript unificado.
   - *Contras:* Menor maturidade em transações complexas ACID com JPA/Hibernate, ecossistema corporativo de observabilidade e mensageria menos consolidado que a JVM.
3. **Golang com Clean Architecture:**
   - *Prós:* Baixo consumo de memória e inicialização instantânea.
   - *Contras:* Falta de ORMs maduros no nível do Hibernate para domínios relacionais ricos e suporte limitado a polimorfismo e modelagem tática avançada de DDD.

## PONTOS EM ABERTO
- [x] Definição da versão do Kotlin e compatibilidade com JDK 21 LTS (Alinhado: Kotlin 2.2 + Java 21).
- [x] Padrão de mapeamento entre entidades JPA, Domínio e DTOs (Alinhado: Mappers manuais declarativos puros em Kotlin para garantir previsibilidade e evitar reflexão pesada do MapStruct).
