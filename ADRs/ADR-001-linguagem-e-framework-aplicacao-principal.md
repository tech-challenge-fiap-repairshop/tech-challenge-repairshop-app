# ADR-001: Adoção de Kotlin e Spring Boot com Clean Architecture e Domain-Driven Design (DDD)

**Data:** 2026-09-01  
**Status:** Aceito  
**Autor:** Grupo CAO (POSTECH 15SOAT)  

---

## Contexto

O sistema de gestão da oficina mecânica (*RepairShop*) tem como objetivo automatizar e gerenciar o fluxo operacional completo do estabelecimento: cadastro e identificação de clientes (por CPF/CNPJ), veículos (placas Mercosul e antigas), catálogo de serviços, controle rigoroso de estoque de insumos/peças, emissão e cálculo dinâmico de orçamentos, máquina de estados finitos para ordens de serviço (OS) e execuções de serviços, além de faturamento e notas fiscais.

Durante a concepção e evolução arquitetural da aplicação, foram identificados os seguintes requisitos e restrições:
1. **Expressividade e Segurança de Tipagem:** A lógica de negócio envolve regras complexas de validação (formatos de documentos fiscais, cálculos financeiros de orçamento, transições de estado permitidas e bloqueios de ordens não aprovadas). Falhas do tipo `NullPointerException` (NPE) em tempo de execução poderiam interromper atendimentos na oficina ou causar cálculos incorretos de insumos.
2. **Independência Tecnológica e Testabilidade:** As regras de negócio não podem ser acopladas a frameworks de infraestrutura (Spring, Hibernate, bancos de dados específicos). O modelo de domínio deve ser puro e testável de forma isolada, sem necessidade de carregar contextos pesados de aplicação ou bancos de dados em testes unitários.
3. **Maturidade e Ecossistema:** Necessidade de integração simplificada com ferramentas consolidadas no mercado corporativo (Spring Data, Spring Security, OpenTelemetry, Testcontainers, Actuator, Flyway, Swagger/OpenAPI).

---

## Decisão

Adotamos a linguagem **Kotlin 2.2** em conjunto com a plataforma **Spring Boot** (JVM), estruturando o código-fonte sob os princípios de **Clean Architecture** (Arquitetura Limpa) e **Domain-Driven Design (DDD)** tático e estratégico.

### Detalhes Técnicos e Estruturais:

1. **Linguagem Kotlin:**
   - Aproveitamento nativo de *Null Safety* em tempo de compilação, eliminando o risco de `NullPointerExceptions` em regras de negócio críticas.
   - Utilização de *Data Classes* e *Value Objects* imutáveis para representação de CPF, CNPJ, Placas de Veículos e Valores Monetários, encapsulando validações no momento da instanciação.
   - Sintaxe concisa, reduzindo código *boilerplate* sem abrir mão da total interoperabilidade com o ecossistema Java.

2. **Clean Architecture e Segregação de Camadas:**
   O projeto é dividido em contextos delimitados (*Bounded Contexts* como `customer`, `vehicle`, `insume`, `serviceorder`, `execution`, `invoice`), e cada módulo possui três camadas concêntricas estritas:
   - **`domain` (Domínio Puro):** Contém entidades de negócio (`entities`), agregados, exceções de negócio e mappers puros. É 100% agnóstico a bibliotecas externas e annotations de frameworks (zero dependência de Spring ou JPA).
   - **`application` (Aplicação / Casos de Uso):** Contém os casos de uso (`usecases`) que orquestram a execução dos fluxos de negócio, além das interfaces de portas de saída (`gateways`), aplicando o Princípio da Inversão de Dependência (DIP).
   - **`infra` (Infraestrutura e Adaptadores):** Camada mais externa onde residem os adaptadores de entrada (`controllers` REST com Spring MVC, DTOs e validações OpenAPI) e adaptadores de saída (`persistence` com Spring Data JPA, entidades do Hibernate e integrações externas).

3. **Domain-Driven Design (DDD):**
   - Aplicação de Linguagem Ubíqua acordada com os especialistas de domínio.
   - Implementação de Agregados e Máquinas de Estado determinísticas para o ciclo de vida da Ordem de Serviço (`RECEIVED` ➡️ `IN_DIAGNOSIS` ➡️ `WAITING_APPROVAL` ➡️ `APPROVED`/`REFUSED` ➡️ `IN_EXECUTION` ➡️ `FINALIZED` ➡️ `PAID`).

---

## Consequências

### Positivas
- **Alta Robustez e Qualidade de Código:** A verificação de nulidade em tempo de compilação do Kotlin e o encapsulamento de invariantes em Value Objects eliminaram categorias inteiras de bugs de produção.
- **Isolamento de Regras de Negócio:** Mudanças de infraestrutura (troca de banco de dados, atualização de versão do Spring, alteração de gateways HTTP) não afetam as regras de domínio nem os casos de uso.
- **Altíssima Testabilidade:** Facilidade para criação de testes unitários rápidos utilizando `MockK` nas camadas de domínio e aplicação sem subir nenhum contexto Spring, permitindo atingir mais de 80% de cobertura com execução em segundos.
- **Facilidade de Manutenção:** A separação por contextos e camadas direciona de forma clara onde cada alteração deve residir.

### Negativas / Trade-offs
- **Maior Quantidade de Classes e Mappers:** A separação estrita entre modelos de banco de dados (JPA entities), entidades de domínio e DTOs de API exige a manutenção de mappers explícitos entre as camadas.
- **Curva de Aprendizado Inicial:** Desenvolvedores acostumados a arquiteturas tradicionais em camadas anêmicas (Controller-Service-Repository do Spring) precisam assimilar o fluxo de portas e adaptadores e a inversão de dependência.

### Riscos e Mitigações
- **Risco:** Desvio arquitetural onde desenvolvedores possam importar annotations ou classes do Spring dentro do pacote `domain`.
- **Mitigação:** Revisões contínuas de código via Pull Request obrigatório e adoção de testes de arquitetura estáticos (ArchUnit / convenções de linters).
