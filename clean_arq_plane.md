# Plano de Migração - Clean Architecture (Customizado)

Este documento detalha o plano de refatoração para adequar o projeto `repairshop-fiap` aos princípios estritos da Clean Architecture, focando na separação clara de responsabilidades, seguindo o padrão já homologado nos domínios `user` e `register`.

Este documento serve como um **guia (prompt base)** para futuras refatorações em outros domínios do projeto.

---

## 1. Estrutura de Packages 

Para manter a coesão por contexto (Feature/Módulo), adotaremos uma estrutura onde cada contexto de negócio (`user`, `register`, `inventory`, `serviceorder`, `execution`, `payment`) conterá obrigatoriamente os três sub-pacotes principais: `application`, `domain` e `infra`.

A estrutura exata de diretórios deve ser:

```text
com.cao.repairshop.[contexto]
├── application/
│   ├── gateways/         # Interfaces que definem as portas de saída (ex: persistência, serviços externos).
│   └── usecases/         # Interfaces dos Casos de Uso (Application Business Rules).
│       └── [feature]/    # (Opcional) Agrupamento por entidade do domínio.
│           └── impl/     # Implementação concreta dos Casos de Uso.
├── domain/
│   ├── entities/         # Enterprise Business Rules (Classes Kotlin puras, DTOs de domínio).
│   │   └── mapper/       # Mappers que convertem entre a Entidade Pura e os DTOs de Controller (Web).
│   └── [services]/       # (Opcional) Serviços de domínio puros, lógicas comuns.
└── infra/
    ├── controller/       # Recebe requests HTTP, mapeia para Input dos UseCases. Contém também os DTOs da API.
    │   └── interfaces/   # Interfaces de API (ex: Swagger/OpenAPI Definitions).
    ├── gateways/         # Implementações concretas de acesso a banco ou integrações externas.
    └── persistence/
        ├── models/       # Entidades JPA (anotadas com @Entity, @Table).
        │   └── converter/# Conversores JPA (ex: AttributeConverters para Value Objects).
        └── repositories/ # Interfaces do Spring Data JPA (ex: JpaRepository).
```

---

## 2. Padrões de Nomenclatura (Naming Conventions)

Para garantir padronização em todos os domínios, as seguintes regras de nomenclatura devem ser aplicadas durante a refatoração:

### 2.1. Casos de Uso (UseCases)
- **Interfaces**: Perdem o sufixo `UseCase`. Devem focar na ação (Verbo + Substantivo). 
  - *Exemplo*: `CreateCustomerUseCase` vira apenas `CreateCustomer`.
- **Implementações**: Ficam dentro da pasta `impl` e recebem o sufixo `Impl`. 
  - *Exemplo*: `CreateCustomerImpl`.

### 2.2. Entidades Puras vs Persistência
- **Entidades de Domínio**: Devem ter o nome limpo e ficar dentro de `domain/entities`. Não possuem anotações JPA.
  - *Exemplo*: `Customer`, `Vehicle`.
- **Modelos de Persistência (JPA)**: Devem receber o sufixo `Entity` para diferenciar claramente das entidades de domínio, e ficar em `infra/persistence/models`.
  - *Exemplo*: `CustomerDataModel` ou `CustomerJPA` passa a se chamar `CustomerEntity`.

### 2.3. Gateways
- **Interfaces**: Ficam em `application/gateways` com o sufixo `Gateway`.
  - *Exemplo*: `CustomerGateway`.
- **Implementações**: Ficam em `infra/gateways` e recebem o sufixo `ImplJPA` (se for banco relacional) para explicitar a tecnologia utilizada na porta.
  - *Exemplo*: `CustomerGatewayImplJPA`.

### 2.4. Controladores e Mappers
- **Mappers**: Ficam em `domain/entities/mapper` e usam o sufixo `Mapper`. Usam extension functions do Kotlin para mapear `EntidadePura -> DTO` ou `EntidadePura -> EntidadeJPA`.
  - *Exemplo*: `CustomerMapper.kt`.
- **DTOs**: Ficam diretamente na raiz de `infra/controller` ou em subpastas caso haja muitos. Recebem sufixo `Request`, `Response` ou `Dto`.

---

## 3. Passo a Passo para Refatoração de um Novo Domínio

Sempre que for aplicar este padrão em um novo módulo (ex: `inventory`), siga os passos na seguinte ordem:

1. **Criação de Pastas:** Crie a estrutura base de pastas (`application/gateways`, `application/usecases/impl`, `domain/entities/mapper`, `infra/controller/interfaces`, `infra/gateways`, `infra/persistence/models`, `infra/persistence/repositories`).
2. **Entidades Puras:** Extraia a essência de negócios das antigas entidades `@Entity` do JPA e crie classes limpas em `domain/entities/`.
3. **Modelos de Persistência:** Mova as antigas `@Entity` do JPA para `infra/persistence/models`, renomeando-as para o sufixo `Entity` (ex: `InsumeEntity`). Remova regras de negócio delas. Ajuste o repositório (`infra/persistence/repositories`) para usar o modelo recém-renomeado.
4. **Gateways:** Crie a interface em `application/gateways` utilizando **apenas** as entidades puras de domínio. Crie a implementação `*ImplJPA` em `infra/gateways`, injetando o repositório do Spring.
5. **Casos de Uso:** Desmembre o antigo `*Service.kt` gigante. Para cada ação, crie uma Interface no pacote de `usecases` e a respectiva classe concreta em `usecases/impl`. Utilize **apenas** o Gateway (via Inversão de Controle) e a Entidade Pura.
6. **Mappers:** Crie os arquivos em `domain/entities/mapper/` para ensinar a aplicação a transformar a Entidade Pura em DTO e na Entidade JPA (e vice-versa).
7. **Controladores:** Mova-os para `infra/controller`, renove os DTOs e injete as interfaces dos Casos de Uso em vez do antigo `Service` monolítico.
8. **Revisão de Pacotes e Imports:** Substitua a referência do antigo `Service` globalmente pelo sistema. Rode o `mvn clean compile` para sanar todos os erros de importação e tipagem remanescentes.
