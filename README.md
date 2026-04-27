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
- [Testes](#testes)
- [Documentação DDD](#documentação-ddd)
- [Segurança](#segurança)
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

---

## Como Executar

```bash
# 1. Clone o repositório
git clone https://github.com/seu-usuario/tech-challenge-fase-1.git
cd tech-challenge-fase-1/repairshop

# 2. Suba os containers (PostgreSQL + aplicação)
docker compose up --build -d

# 3. Aguarde os logs indicarem que a aplicação subiu
docker compose logs -f app
# Procure por: "Started RepairshopApplication"
```

O Docker Compose sobe dois serviços:

| Serviço | Porta | Descrição |
|---------|-------|-----------|
| `postgres` | 5432 | PostgreSQL 17 com healthcheck |
| `app` | 8080 | Aplicação Spring Boot (aguarda o banco ficar healthy) |

Na primeira execução, o **Flyway** aplica automaticamente as 12 migrations (criação de tabelas, indexes e seed de 250 insumos).

### Acessos

| Recurso | URL |
|---------|-----|
| API REST | http://localhost:8080 |
| Swagger UI | http://localhost:8080/swagger-ui/index.html |
| API Docs (JSON) | http://localhost:8080/v3/api-docs |

### Primeiro uso

Todos os endpoints (exceto login, consulta de OS e Swagger) exigem autenticação JWT. Para começar:

```bash
# 1. Registrar um usuário (endpoint público)
curl -s -X POST http://localhost:8080/auth/register \
  -H "Content-Type: application/json" \
  -d '{"name":"Admin","function":"ADMIN","email":"admin@shop.com","password":"SecurePass123"}'

# 2. Fazer login para obter o token JWT
curl -s -X POST http://localhost:8080/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"admin@shop.com","password":"SecurePass123"}'
# Retorna: {"token":"eyJ..."}

# 3. Usar o token nos demais endpoints
curl -s -H "Authorization: Bearer <token>" http://localhost:8080/customers
```

### Comandos úteis

```bash
# Parar os containers (mantém dados)
docker compose down

# Parar e limpar tudo (remove volume do banco)
docker compose down -v

# Ver logs da aplicação
docker compose logs -f app

# Rebuild após alterações no código
docker compose up --build -d
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
| **Services** | `POST` | `/services` | Cadastrar serviço |
| | `GET` | `/services` | Listar serviços (paginado) |
| | `GET` | `/services/{id}` | Buscar serviço por ID |
| | `PUT` | `/services/{id}` | Atualizar serviço |
| | `DELETE` | `/services/{id}` | Remover serviço |
| | `PATCH` | `/services/{id}/status` | Avançar status do serviço |
| **Service Orders** | `POST` | `/service-orders` | Criar OS com orçamento automático |
| | `GET` | `/service-orders` | Listar ordens de serviço (paginado) |
| | `GET` | `/service-orders/{id}` | Consultar OS (público, sem auth) |
| | `PATCH` | `/service-orders/{id}/status` | Avançar status da OS |
| | `POST` | `/service-orders/{id}/approve` | Aprovar/recusar orçamento |
| | `GET` | `/service-orders/metrics` | Tempo médio de execução |

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

---

## Documentação DDD

A documentação de Domain-Driven Design do projeto inclui:

- **Event Storming** — Fluxos de criação/acompanhamento da OS e gestão de peças/insumos
- **Linguagem Ubíqua** — Glossário de termos do domínio
- **Diagramas** — Bounded Contexts, Aggregates e fluxos de domínio

> 📄 Disponível na pasta [`docs/`](docs/) e no [Miro do projeto](link-do-miro).

---

## Segurança

| Aspecto | Implementação |
|---------|--------------|
| **Autenticação** | JWT (JSON Web Tokens) para APIs administrativas |
| **Validação** | Dados sensíveis validados (CPF/CNPJ, placa de veículo) |
| **Testes** | Unitários e de integração para os principais fluxos |

---

## Autores

**Grupo CAO** — POSTECH 15SOAT

| Nome           | RM     | GitHub |
|----------------|--------|--------|
| Alexandre      | 374016 | <!-- @username --> |
| Caio Crevelaro | 373877 | <!-- @username --> |
| Otávio Luiz    | 370552 | <!-- @username --> |
