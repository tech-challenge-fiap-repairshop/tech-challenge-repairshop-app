# 🔧 Oficina API

**Sistema Integrado de Atendimento e Execução de Serviços para Oficina Mecânica**

[![Kotlin](https://img.shields.io/badge/Kotlin-2.2-7F52FF?logo=kotlin&logoColor=white)](https://kotlinlang.org/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-4.0-6DB33F?logo=springboot&logoColor=white)](https://spring.io/projects/spring-boot)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-17-4169E1?logo=postgresql&logoColor=white)](https://www.postgresql.org/)
[![Docker](https://img.shields.io/badge/Docker-Ready-2496ED?logo=docker&logoColor=white)](https://www.docker.com/)
[![Coverage](https://img.shields.io/badge/Coverage-80%25+-brightgreen)](.)
[![License](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)

*MVP do back-end para gestão de ordens de serviço, clientes, veículos e peças de uma oficina mecânica.*

**POSTECH 15SOAT — Tech Challenge Fase 1 — Grupo CAO**

---

## Sumário

- [Sobre o Projeto](#sobre-o-projeto)
- [Funcionalidades](#funcionalidades)
- [Arquitetura](#arquitetura)
- [Justificativa do Banco de Dados](#justificativa-do-banco-de-dados)
- [Tecnologias Utilizadas](#tecnologias-utilizadas)
- [Pré-requisitos](#pré-requisitos)
- [Como Executar](#como-executar)
- [Documentação da API](#documentação-da-api)
  - [Máquina de Estados (Status da OS)](#-máquina-de-estados-status-da-os)
  - [Máquina de Estados (Status da Execução de Serviço)](#-máquina-de-estados-status-da-execução-de-serviço)
- [Testes](#testes)
- [Documentação DDD](#documentação-ddd)
- [Segurança](#segurança)
- [Avisos do Projeto](#avisos-do-projeto)
- [Autores](#autores)

---

## Sobre o Projeto

Uma oficina mecânica de médio porte enfrenta desafios na gestão do fluxo de atendimento, diagnóstico, execução e entrega de veículos. Processos manuais baseados em planilhas geram erros de priorização, falhas no controle de estoque, perda de histórico e ineficiência em orçamentos.

Este projeto é o **MVP do back-end** de um **Sistema Integrado de Atendimento e Execução de Serviços**, desenvolvido como parte do **Tech Challenge Fase 1** da pós-graduação **POSTECH 15SOAT** em Arquitetura de Software.

### Objetivos

- Centralizar a gestão de ordens de serviço, clientes, veículos e peças
- Automatizar o fluxo de status das ordens de serviço
- Gerar orçamentos automaticamente com base nos serviços e peças
- Permitir que clientes acompanhem o andamento do serviço via API
- Garantir segurança com autenticação JWT e validação de dados sensíveis
- Aplicar Domain-Driven Design (DDD) com arquitetura em camadas

---

## Funcionalidades

- [x] CRUD de Clientes (identificação por CPF/CNPJ)
- [x] CRUD de Veículos (placa, marca, modelo, ano)
- [x] CRUD de Serviços (catálogo de serviços da oficina)
- [x] CRUD de Peças e Insumos com controle de estoque
- [x] Criação de Ordens de Serviço com orçamento automático
- [x] Fluxo de status da OS com transições automáticas
- [x] Consulta de OS pelo cliente para acompanhamento
- [x] Aprovação de orçamento pelo cliente
- [x] Monitoramento do tempo médio de execução dos serviços
- [x] Autenticação e autorização via JWT
- [x] Documentação interativa da API via Swagger/OpenAPI

---

## Arquitetura

O projeto aplica **Domain-Driven Design (DDD)** de forma integral — tanto no nível **estratégico** (Event Storming, Bounded Contexts, Linguagem Ubíqua) quanto no nível **tático** (Entities, Value Objects, Aggregates, Repositories, Domain Services). A estrutura segue uma **arquitetura monolítica em camadas**, onde o domínio é o núcleo isolado e independente de frameworks e infraestrutura.

---

## Justificativa do Banco de Dados

Optamos pelo **PostgreSQL** pelos seguintes motivos:

| Critério | Justificativa |
|----------|--------------|
| **Integridade relacional** | O domínio possui relações claras entre clientes, veículos, ordens de serviço, serviços e peças — um modelo relacional garante consistência via foreign keys e constraints |
| **Conformidade ACID** | Ordens de serviço envolvem transações com múltiplas entidades (serviços, peças, estoque) que precisam de atomicidade e consistência |
| **Controle de estoque** | Operações concorrentes de baixa de peças exigem isolamento transacional robusto |
| **Ecossistema** | Integração madura com Spring Data JPA/Hibernate |
| **Open source** | Sem custos de licenciamento, com comunidade ativa e documentação extensa |

> 📊 O **Diagrama de Entidade e Relacionamento (ERD)** do banco de dados pode ser encontrado em [`docs/spec/database-er-diagram.png`](docs/spec/database-er-diagram.png).

---

## Tecnologias Utilizadas

| Tecnologia | Finalidade |
|-----------|-----------|
| **Kotlin** | Linguagem principal |
| **Spring Boot** | Framework para construção da API REST |
| **Spring Security** | Autenticação e autorização com JWT |
| **Spring Data JPA** | Acesso a dados |
| **PostgreSQL** | Banco de dados relacional |
| **Docker / Docker Compose** | Containerização e orquestração do ambiente |

---

## Pré-requisitos

- [Docker](https://docs.docker.com/get-docker/) e [Docker Compose](https://docs.docker.com/compose/install/)
- **Java 24** (Obrigatório apenas caso deseje rodar a aplicação ou os testes localmente sem o Docker)

---

## Como Executar

O projeto utiliza um `Dockerfile` *multi-stage*, o que significa que a imagem Docker baixa o Maven e compila o `.jar` internamente. Portanto, ter o Maven ou o Java instalados na sua máquina **não é estritamente obrigatório** apenas para rodar a aplicação via Docker.

No entanto, o fluxo padrão recomendado para desenvolvedores é compilar e validar os testes localmente antes de subir a imagem:

```bash
# 1. Clone o repositório
git clone https://github.com/Alexandre-AGAMIN/tech-challenge-FIAP.git
cd tech-challenge-FIAP

# 2. (Opcional) Faça o build e rode os testes locais usando o Maven Wrapper
./mvnw clean install

# 3. Suba a infraestrutura completa via Docker
docker compose up --build -d

# 4. Aguarde os logs indicarem que a aplicação subiu
docker compose logs -f app
# Procure pela mensagem: "Started RepairshopApplication"
```

O `docker-compose.yml` provisiona **quatro serviços** simultaneamente:

| Serviço | Porta | Descrição |
|---------|-------|-----------|
| `postgres` | 5432 | PostgreSQL 17 com healthcheck |
| `app` | 8080 | Aplicação Spring Boot (aguarda o banco ficar healthy) |
| `mailhog` | 8025 / 1025 | Interceptador de e-mails locais (Dashboard web em 8025) |
| `sonarqube` | 9000 | Plataforma de análise contínua de qualidade de código |

> Na primeira execução, o **Flyway** da aplicação aplica automaticamente todas as migrations no banco de dados (criação de tabelas, indexes e seed de insumos).

### 🌐 Acessos e Painéis

Com os containers rodando, as interfaces estão disponíveis nos seguintes endereços:

| Recurso | URL |
|---------|-----|
| Swagger UI (Recomendado) | http://localhost:8080/swagger-ui/index.html |
| API REST (Base) | http://localhost:8080 |
| Caixa de E-mails (MailHog) | http://localhost:8025 |
| Dashboard SonarQube | http://localhost:9000 |

### 🚀 Primeiro uso (Via Swagger UI ou Postman)

A grande maioria dos endpoints exige autenticação JWT. Recomendamos realizar o primeiro uso diretamente pelo **Swagger UI** (`http://localhost:8080/swagger-ui/index.html`) para evitar problemas de formatação de JSON no terminal do Windows (como no caso do `curl`):

> 💡 **Dica para o Postman:** Se preferir, temos uma collection pronta para importação! Basta utilizar os arquivos localizados no diretório [`docs/postman-collections/`](docs/postman-collections/).

1. Vá até o endpoint `POST /auth/register` no Swagger e cadastre o usuário inicial:
   ```json
   {
     "name": "Admin",
     "function": "ATTENDANT", // Roles aceitas: CUSTOMER ou ATTENDANT
     "email": "admin@shop.com",
     "password": "SecurePass123"
   }
   ```
2. Vá até `POST /auth/login`, envie o e-mail e senha cadastrados para receber o token JWT.
3. Copie o valor do `token` (sem aspas) da resposta.
4. Clique no botão **Authorize** (cadeado verde no topo do Swagger) e cole o token. A partir de agora, o Swagger injetará o header `Authorization: Bearer <token>` em todas as suas requisições automaticamente!

### 🛠️ Comandos úteis

```bash
# Rodar análise de código local e enviar pro SonarQube container (requer Java 24)
./mvnw clean verify sonar:sonar -Dsonar.projectKey=repairshop -Dsonar.host.url=http://localhost:9000

# Parar os containers (mantém os dados)
docker compose down

# Parar e resetar tudo (remove o banco de dados e as análises do sonar)
docker compose down -v
```

---

## Documentação da API

A API é documentada via **OpenAPI 3.0** e pode ser acessada interativamente pelo **Swagger UI**:

```
http://localhost:8080/swagger-ui.html
```

### Principais Endpoints

| Domínio | Método | Endpoint | Descrição |
|---------|--------|----------|-----------|
| **Auth** | `POST` | `/auth/login` | Autenticação e geração de token JWT |
| | `POST` | `/auth/register` | Registrar novo usuário |
| **Customers** | `POST` | `/customers` | Cadastrar cliente |
| | `GET` | `/customers` | Listar clientes (paginado) |
| | `GET` | `/customers/{id}` | Buscar cliente por ID |
| | `PUT` | `/customers/{id}` | Atualizar cliente |
| | `DELETE` | `/customers/{id}` | Remover cliente |
| **Vehicles** | `POST` | `/vehicles` | Cadastrar veículo |
| | `GET` | `/vehicles` | Listar veículos (paginado) |
| | `GET` | `/vehicles/{id}` | Buscar veículo por ID |
| | `PUT` | `/vehicles/{id}` | Atualizar veículo |
| | `DELETE` | `/vehicles/{id}` | Remover veículo |
| **Insumes** | `POST` | `/insumes` | Cadastrar insumo/peça |
| | `GET` | `/insumes` | Listar insumos (paginado) |
| | `GET` | `/insumes/{id}` | Buscar insumo por ID |
| | `PUT` | `/insumes/{id}` | Atualizar insumo |
| | `DELETE` | `/insumes/{id}` | Remover insumo |
| **Service Orders** | `POST` | `/service-orders` | Criar OS com orçamento automático |
| | `GET` | `/service-orders` | Listar ordens de serviço (paginado) |
| | `GET` | `/service-orders/{id}` | Consultar OS (público, sem auth) |
| | `PATCH` | `/service-orders/{id}/status` | Avançar status da OS |
| | `POST` | `/service-orders/{id}/approve` | Aprovar/recusar orçamento |
| | `GET` | `/service-orders/metrics` | Métricas gerais das ordens de serviço |
| | `GET` | `/service-orders/executions/metrics`| Métricas de tempo de execução |
| **Executions** | `POST` | `/service-orders/{id}/executions` | Adicionar serviço/execução à OS |
| | `POST` | `/service-orders/{id}/executions/batch` | Adicionar múltiplos serviços à OS |
| | `GET` | `/service-orders/{id}/executions/{execId}` | Buscar execução de serviço na OS |
| | `PUT` | `/service-orders/{id}/executions/{execId}` | Atualizar execução |
| | `PATCH` | `/service-orders/{id}/executions/{execId}/status`| Avançar status da execução |
| | `DELETE` | `/service-orders/{id}/executions/{execId}` | Remover execução da OS |
| **Invoices** | `POST` | `/invoices` | Gerar fatura para pagamento |
| | `GET` | `/invoices` | Listar faturas (paginado) |
| | `GET` | `/invoices/{id}` | Consultar fatura por ID |

#### Valores Úteis: Descrições de Execução (`BasicExecution`)
Ao adicionar execuções (serviços) à uma OS (`POST /service-orders/{id}/executions`), o campo `basicDescription` deve conter um dos seguintes valores padronizados:
- `OIL_CHANGE`
- `SUSPENSION_REPLACEMENT`
- `WHEEL_ALIGNMENT`
- `BRAKE_INSPECTION`
- `ENGINE_DIAGNOSIS`
- `OTHER`

### ⚙️ Máquina de Estados (Status da OS)

O fluxo de atendimento da Ordem de Serviço segue um controle de status rigoroso. Você pode avançar o status chamando o endpoint `PATCH /service-orders/{id}/status`. As transições sequenciais permitidas são:

1. `RECEIVED` ➡️ `IN_DIAGNOSIS`
2. `IN_DIAGNOSIS` ➡️ `WAITING_APPROVAL`
3. `WAITING_APPROVAL` ➡️ `APPROVED` ou `REFUSED`
   - ⚠️ **Atenção:** A transição a partir de `WAITING_APPROVAL` **não pode** ser feita pelo `PATCH`. É obrigatório chamar o endpoint específico `POST /service-orders/{id}/approve`, enviando a decisão do cliente.
4. `APPROVED` ➡️ `IN_EXECUTION`
5. `REFUSED` ➡️ `CANCELED`
6. `IN_EXECUTION` ➡️ `FINALIZED`
7. `FINALIZED` ➡️ `PAID`
   - ⚠️ **Atenção:** Para que a OS avance para `PAID`, é necessário faturá-la chamando a API de Invoices (`POST /invoices`) para criar a nota fiscal.

> 🖼️ **Fluxo de Estados da OS:**
> 
> ![Status Chain](docs/spec/status_chain.png)
> 

### 🛠️ Máquina de Estados (Status da Execução de Serviço)

Cada serviço individual dentro de uma Ordem de Serviço possui seu próprio controle de progresso. Você pode avançar o status de uma execução específica chamando o endpoint `PATCH /service-orders/{id}/executions/{execId}/status`.

As transições permitidas são:
1. `INITIATED` ➡️ `PENDING`
2. `PENDING` ➡️ `FINALIZED`

---

## Testes

**118 testes** (unitários + integração), todos passando:

```bash
# Rodar os testes (sem necessidade de banco — usam MockK)
./mvnw test
```

| Camada | Testes | O que cobre |
|--------|--------|-------------|
| Value Objects & Enums | 32 | CPF/CNPJ, placa, status OS (9 estados), status serviço |
| Services (MockK) | 54 | CRUD, state machine, stock, budget, auth |
| Controllers (MockMvc) | 32 | Endpoints, validação, erros, paginação |

Cobertura de **80%+** nos domínios críticos (transições de status, cálculo de orçamento, dedução de estoque, validações de CPF/CNPJ/placa).

> 📊 Os relatórios de análise de qualidade de código e cobertura gerados pelo **SonarQube** estão disponíveis na pasta [`docs/sonar/`](docs/sonar/).

---

## Documentação DDD

A documentação de Domain-Driven Design do projeto inclui:

- **Event Storming** — Fluxos de criação/acompanhamento da OS e gestão de peças/insumos
- **Linguagem Ubíqua** — Glossário de termos do domínio
- **Diagramas** — Bounded Contexts, Aggregates e fluxos de domínio

> 📄 O **Dicionário de Linguagem Ubíqua** está disponível em [`docs/delivery/dicionario-linguagem-ubiqua.md`](docs/delivery/dicionario-linguagem-ubiqua.md).
> 🗺️ Os quadros do **Miro** (Event Storming e Storytelling) exportados em PDF estão na pasta [`docs/miro/`](docs/miro/).

---

## Segurança

| Aspecto | Implementação |
|---------|--------------|
| **Autenticação** | JWT (JSON Web Tokens) para APIs administrativas |
| **Validação** | Dados sensíveis validados (CPF/CNPJ, placa de veículo) |
| **Testes** | Unitários e de integração para os principais fluxos |

> 🛡️ O relatório de escaneamento de vulnerabilidades gerado pelo **OWASP ZAP** está disponível na pasta [`docs/owaspzap/`](docs/owaspzap/).

---

## Avisos do Projeto

### Endpoint `/register` (Autenticação)

O serviço `/register` (disponível na API de autenticação) existe **apenas para fins didáticos e de demonstração** no escopo deste projeto acadêmico (Tech Challenge). 

Em um cenário de produção real, o cadastro de novos usuários administradores/funcionários do sistema não seria exposto em um endpoint de uso aberto. A criação de usuários seria feita de forma controlada, através de um painel administrativo com os devidos controles de acesso, ou por um processo interno de provisionamento.

---

## Autores

**Grupo CAO** — POSTECH 15SOAT

| Nome           | RM     | GitHub               |
|----------------|--------|----------------------|
| Alexandre      | 374016 | [Alexandre-AGAMIN](https://github.com/Alexandre-AGAMIN) |
| Otávio Luiz    | 370552 | [otaviolms](https://github.com/otaviolms) |
