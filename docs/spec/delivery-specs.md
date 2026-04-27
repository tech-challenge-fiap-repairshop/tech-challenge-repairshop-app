# Especificacao de Entrega — Tech Challenge Fase 1

**POSTECH 15SOAT — Grupo CAO**
**Peso:** 90% da nota da fase

---

## 1. Contexto

Uma oficina mecanica de medio porte utiliza anotacoes manuais e planilhas para gerir atendimento, diagnostico, execucao e entrega de veiculos. Isso gera:

- Erros na priorizacao dos atendimentos
- Falhas no controle de pecas e insumos
- Dificuldade em acompanhar o status dos servicos
- Perda de historico de clientes e veiculos
- Ineficiencia no fluxo de orcamentos e autorizacoes

**Objetivo:** Construir o MVP do back-end de um Sistema Integrado de Atendimento e Execucao de Servicos, aplicando DDD, qualidade de software e seguranca.

---

## 2. Requisitos tecnicos

- [ ] Back-end monolitico com arquitetura em camadas
- [ ] Banco de dados com justificativa documentada (PostgreSQL — justificativa ja consta no README)
- [ ] APIs RESTful documentadas via Swagger/OpenAPI
- [ ] Dockerfile para build da aplicacao
- [ ] docker-compose.yml para orquestrar ambiente completo (app + banco)
- [ ] Testes automatizados com cobertura minima de 80% nos dominios criticos
- [ ] README.md completo com instrucoes de uso e objetivos
- [ ] Repositorio privado com acesso ao usuario `soat-architecture`

---

## 3. Funcionalidades obrigatorias

### 3.1 Criacao da Ordem de Servico (OS)

- [ ] Identificacao do cliente por CPF/CNPJ
- [ ] Cadastro de veiculo (placa, marca, modelo, ano)
- [ ] Inclusao dos servicos solicitados (ex: troca de oleo, alinhamento)
- [ ] Possibilidade de incluir pecas e insumos necessarios
- [ ] Orcamento gerado automaticamente com base nos servicos e pecas
- [ ] Envio do orcamento ao cliente para aprovacao

### 3.2 Acompanhamento da OS

- [ ] Status da OS com transicoes (enum `status_os`):
  - RECEIVED (Recebida)
  - IN_DIAGNOSIS (Em diagnostico)
  - WAITING_APPROVAL (Aguardando aprovacao)
  - APPROVED (Aprovada)
  - REFUSED (Recusada)
  - IN_EXECUTION (Em execucao)
  - FINALIZED (Finalizada)
  - PAID (Paga)
  - CANCELED (Cancelada)
- [ ] Alteracao automatica dos status conforme acoes no sistema
- [ ] Consulta por parte do cliente via API para acompanhar o progresso

### 3.3 Gestao administrativa

- [ ] CRUD de Clientes
- [ ] CRUD de Veiculos
- [ ] CRUD de Servicos
- [ ] CRUD de Pecas e Insumos com controle de estoque
- [ ] Listagem e detalhamento de Ordens de Servico
- [ ] Monitoramento do tempo medio de execucao dos servicos

### 3.4 Seguranca e qualidade

- [ ] Autenticacao JWT para APIs administrativas
- [ ] Validacao de dados sensiveis (CPF/CNPJ, placa de veiculo)
- [ ] Testes unitarios e de integracao para os principais fluxos

---

## 4. Modelo de dados

### 4.1 Tabelas (9) por Bounded Context

**Cadastro:**
- `tb_customer` — id_tb_customer (UUID PK), nome, document (UK), email, phone, birth_date, created, updated
- `tb_vehicle` — id_tb_vehicle (UUID PK), customer_id (FK), plate, brand, model, color, manufacturing_date, last_maintenance, created, updated

**Ordem de Servico:**
- `tb_service_order` — id_tb_service_order (UUID PK), customer_id (FK), vehicle_id (FK), status (ENUM), total_price, enter_time, end_time, valid_date, created, updated
- `tb_service_order_history` — id_tb_service_order_history (UUID PK), service_order_id (FK), status, register_time, interval_time

**Servico:**
- `tb_service` — id_tb_service (UUID PK), service_order (FK), description, price, estimated_time, status (ENUM), created, updated
- `tb_service_insume` — id_tb_service (PK FK), id_tb_insume (PK FK) — tabela pivot N:N
- `tb_service_history` — id_tb_service_history (UUID PK), service_id (FK), status, register_time, interval_time

**Estoque:**
- `tb_insume` — id_tb_insume (UUID PK), name, brand, sku_id, quantity, price, unity_price

**Usuarios:**
- `tb_user` — id_tb_user (UUID PK), name, function, email, password

### 4.2 Enums

| Enum | Valores |
|------|---------|
| `status_os` | RECEIVED, IN_DIAGNOSIS, WAITING_APPROVAL, APPROVED, REFUSED, IN_EXECUTION, FINALIZED, PAID, CANCELED |
| `status_service` | INITIATED, PENDING, FINALIZED |

### 4.3 Relacionamentos

- tb_customer 1:N tb_vehicle
- tb_customer 1:N tb_service_order
- tb_vehicle 1:N tb_service_order
- tb_service_order 1:N tb_service
- tb_service_order 1:N tb_service_order_history
- tb_service 1:N tb_service_insume
- tb_insume 1:N tb_service_insume
- tb_service 1:N tb_service_history

---

## 5. Endpoints da API

### 5.1 Autenticacao

| Metodo | Endpoint | Descricao |
|--------|----------|-----------|
| POST | `/auth/login` | Autenticacao e geracao de token JWT |

### 5.2 Clientes

| Metodo | Endpoint | Descricao |
|--------|----------|-----------|
| GET | `/customers` | Listar clientes |
| POST | `/customers` | Cadastrar cliente |
| GET | `/customers/{id}` | Buscar cliente por ID |
| PUT | `/customers/{id}` | Atualizar cliente |
| DELETE | `/customers/{id}` | Remover cliente |

### 5.3 Veiculos

| Metodo | Endpoint | Descricao |
|--------|----------|-----------|
| GET | `/vehicles` | Listar veiculos |
| POST | `/vehicles` | Cadastrar veiculo |
| GET | `/vehicles/{id}` | Buscar veiculo por ID |
| PUT | `/vehicles/{id}` | Atualizar veiculo |
| DELETE | `/vehicles/{id}` | Remover veiculo |

### 5.4 Servicos

| Metodo | Endpoint | Descricao |
|--------|----------|-----------|
| GET | `/services` | Listar servicos |
| POST | `/services` | Cadastrar servico |
| GET | `/services/{id}` | Buscar servico por ID |
| PUT | `/services/{id}` | Atualizar servico |
| DELETE | `/services/{id}` | Remover servico |

### 5.5 Insumos

| Metodo | Endpoint | Descricao |
|--------|----------|-----------|
| GET | `/insumes` | Listar insumos |
| POST | `/insumes` | Cadastrar insumo |
| GET | `/insumes/{id}` | Buscar insumo por ID |
| PUT | `/insumes/{id}` | Atualizar insumo |
| DELETE | `/insumes/{id}` | Remover insumo |

### 5.6 Ordens de Servico

| Metodo | Endpoint | Descricao |
|--------|----------|-----------|
| POST | `/service-orders` | Criar OS com orcamento automatico |
| GET | `/service-orders` | Listar todas as OS |
| GET | `/service-orders/{id}` | Consultar OS (acompanhamento) |
| PATCH | `/service-orders/{id}/status` | Avancar status da OS |
| POST | `/service-orders/{id}/approve` | Cliente aprova orcamento |
| GET | `/service-orders/metrics` | Tempo medio de execucao |

---

## 6. Regras de negocio principais

### 6.1 Orcamento automatico

Ao criar uma OS, o sistema deve calcular o `total_price` somando:
- `SUM(tb_service.price)` — preco de cada servico vinculado a OS
- Os insumos vinculados a cada servico (via `tb_service_insume`) compoem o custo mas o preco final do servico ja os contempla

### 6.2 Transicoes de status da OS (`status_os`)

```
RECEIVED --> IN_DIAGNOSIS --> WAITING_APPROVAL --> APPROVED --> IN_EXECUTION --> FINALIZED --> PAID
                                    |
                                    +--> REFUSED --> CANCELED
```

Transicoes validas:
- RECEIVED -> IN_DIAGNOSIS (mecanico inicia diagnostico)
- IN_DIAGNOSIS -> WAITING_APPROVAL (diagnostico concluido, orcamento enviado)
- WAITING_APPROVAL -> APPROVED (cliente aprova)
- WAITING_APPROVAL -> REFUSED (cliente recusa)
- REFUSED -> CANCELED (OS cancelada)
- APPROVED -> IN_EXECUTION (inicio da execucao dos servicos)
- IN_EXECUTION -> FINALIZED (todos os servicos concluidos)
- FINALIZED -> PAID (pagamento realizado, veiculo entregue)

Cada transicao gera um registro em `tb_service_order_history` com o status, `register_time` e `interval_time` (tempo entre transicoes).

### 6.3 Transicoes de status do servico (`status_service`)

```
INITIATED --> PENDING --> FINALIZED
```

- INITIATED: servico criado/vinculado a OS
- PENDING: servico em andamento pelo mecanico
- FINALIZED: servico concluido

Cada transicao gera um registro em `tb_service_history` com o status, `register_time` e `interval_time`.

### 6.4 Controle de insumos

- Insumos sao vinculados a servicos via tabela pivot `tb_service_insume` (relacao N:N)
- O campo `quantity` em `tb_insume` representa o estoque atual
- Ao vincular insumos a um servico de OS aprovada, o sistema deve dar baixa no estoque (`quantity -= quantidade usada`)
- Quantidade em estoque nunca pode ser negativa

### 6.5 Validacoes

- **Document (CPF/CNPJ):** campo `document` em `tb_customer`, unico, com validacao de digitos verificadores
- **Placa:** formato brasileiro (ABC-1234 ou ABC1D23 Mercosul)
- **Email:** formato valido

### 6.6 Seguranca (JWT)

- Login com email + senha retorna token JWT
- Token deve conter: user_id, function, expiration
- APIs administrativas (CRUD, gestao de OS) exigem token valido
- Consulta de OS pelo cliente pode ser publica ou com token de cliente
- O campo `function` em `tb_user` define o nivel de acesso do usuario

---

## 7. Plano de implementacao

### Fase A — Infraestrutura base
- [ ] Adicionar dependencias ao pom.xml (Spring Data JPA, PostgreSQL, Spring Security, JWT lib)
- [ ] Configurar application.properties / application.yml (datasource, JPA, JWT secret)
- [ ] Criar Dockerfile (multi-stage build)
- [ ] Criar docker-compose.yml (app + PostgreSQL)
- [ ] Corrigir .gitattributes (trocar `/gradlew` por `/mvnw`)

### Fase B — Dominio e persistencia
- [ ] Criar enums Kotlin (StatusOs, StatusService)
- [ ] Criar entidades JPA para as 9 tabelas (tb_customer, tb_vehicle, tb_service_order, tb_service_order_history, tb_service, tb_service_insume, tb_service_history, tb_insume, tb_user)
- [ ] Criar repositories Spring Data JPA
- [ ] Criar migration ou schema.sql com DDL
- [ ] Script de carga inicial com dados do CSV de estoque (250 insumos)

### Fase C — CRUDs basicos
- [ ] CustomerController + CustomerService + DTOs + validacao CPF/CNPJ
- [ ] VehicleController + VehicleService + DTOs + validacao placa
- [ ] ServiceController + ServiceService + DTOs
- [ ] InsumeController + InsumeService + DTOs + logica de estoque

### Fase D — Ordens de Servico
- [ ] ServiceOrderController + ServiceOrderService + DTOs
- [ ] Logica de criacao de OS com calculo automatico de orcamento (total_price)
- [ ] Maquina de estados para transicoes de status_os
- [ ] Registro automatico de historico em tb_service_order_history a cada transicao
- [ ] Maquina de estados para transicoes de status_service
- [ ] Registro automatico de historico em tb_service_history a cada transicao
- [ ] Endpoint de aprovacao/recusa pelo cliente
- [ ] Endpoint de metricas (tempo medio de execucao via interval_time)
- [ ] Baixa automatica de estoque de insumos ao aprovar OS

### Fase E — Seguranca
- [ ] Entidade User (tb_user) com password hash (BCrypt)
- [ ] Configuracao Spring Security
- [ ] Endpoint /auth/login com geracao de JWT
- [ ] Filtro de autenticacao JWT
- [ ] Autorizacao por function do usuario

### Fase F — Testes
- [ ] Testes unitarios das regras de dominio (transicoes de status_os e status_service, calculo de orcamento, validacoes CPF/CNPJ/placa)
- [ ] Testes unitarios dos services
- [ ] Testes de integracao dos controllers (MockMvc ou WebTestClient)
- [ ] Testes de integracao do repositorio
- [ ] Atingir cobertura de 80%+ nos dominios criticos

### Fase G — Entregaveis finais
- [ ] Rodar scan de vulnerabilidades (OWASP Dependency-Check ou similar)
- [ ] Escrever relatorio de analise de vulnerabilidades
- [ ] Revisar e finalizar README.md
- [ ] Criar documento de entrega (PDF) com: nome do grupo, participantes, links, relatorio
- [ ] Dar acesso ao usuario `soat-architecture` no repositorio
- [ ] Gravar video de ate 15 minutos demonstrando tudo

---

## 8. Stack tecnologica

| Tecnologia | Finalidade |
|-----------|-----------|
| Kotlin | Linguagem principal |
| Spring Boot 4.0 | Framework REST |
| Spring Data JPA | Acesso a dados |
| Spring Security | Autenticacao e autorizacao |
| PostgreSQL | Banco de dados relacional |
| JWT (jjwt) | Tokens de autenticacao |
| SpringDoc OpenAPI | Documentacao Swagger |
| JUnit 5 + MockK | Testes |
| Docker / Docker Compose | Containerizacao |
| Maven | Build tool |

---

## 9. Dados de referencia

- **Diagrama ER (PNG):** `docs/database-er-diagram.png`
- **Diagrama ER (HTML interativo):** `docs/database-er-diagram.html`
- **Linguagem Ubiqua:** `docs/dictionary-ubiquitous-language.md`
- **Dados de estoque (seed):** `docs/estoque-oficina-250.csv` (250 insumos)
- **Enunciado:** `docs/15SOAT - Fase 1 - Tech Challenge.pdf`
