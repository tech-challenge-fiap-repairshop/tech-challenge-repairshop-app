# RFC – Isolamento do Serviço de Autenticação em Microsserviço Serverless AWS Lambda e JWT Stateless

## DATA
22/08/2026

## STATUS
Encerrada – Aprovada

> **Nota de Encerramento:** Proposta aprovada por todo o time técnico, originando formalmente o [ADR-007](../ADRs/ADR-007-microsservico-serverless-de-autenticacao-aws-lambda-auth.md).

## RESUMO
Proposta de desacoplamento do serviço de autenticação de usuários para um microsserviço Serverless independente em AWS Lambda utilizando Java 21 LTS com Clean Architecture, emitindo e validando tokens JWT Stateless assinados com HMAC-SHA256.

## PROBLEMA
No contexto de segurança e controle de acesso ao ecossistema da oficina mecânica (*RepairShop*), identificamos os seguintes desafios:
1. **Padrão de Carga em Rajadas:** O fluxo de login (`POST /auth/login`) concentra chamadas intensas em curtos períodos do dia (início de expediente e turnos), permanecendo com tráfego muito menor no restante do tempo. Manter Pods dedicados no Kubernetes consumindo CPU/RAM 24/7 apenas para autenticação geraria desperdício financeiro.
2. **Segurança e Superfície de Ataque:** O módulo de autenticação é um alvo preferencial de ataques (força bruta, injeção de credenciais). Misturar a lógica de login dentro do monólito de ordens de serviço e estoque aumenta o risco de vazamento de segredos e dificulta a auditoria isolada de segurança.
3. **Escalabilidade com Estado Centralizado:** O uso de sessões em memória no servidor (*stateful sessions*) cria dependência de sticky sessions e impede que réplicas no Kubernetes atendam qualquer requisição autenticada de forma desacoplada.

## PROPOSTA TÉCNICA
Propõe-se a criação do microsserviço **`tech-challenge-repairshop-lambda-auth`** como uma função Serverless na AWS, acompanhada de um modelo de autenticação puramente **Stateless com JSON Web Tokens (JWT)**:

```
[ Cliente / Postman / Front ]
             |
             | POST /auth/login (CPF + Senha)
             v
+----------------------------+
|      AWS API Gateway       |
+----------------------------+
             |
             | (Payload 2.0)
             v
+-----------------------------------------------------------+
| AWS Lambda Auth (Java 21 / Clean Architecture)            |
| - Validação Defensiva de CPF e Credenciais                |
| - Consulta Segura de Usuário no Banco / Hash BCrypt       |
| - Emissão do Token JWT (HMAC-SHA256, roles, expiração)   |
| - Headers de Segurança OWASP                              |
+-----------------------------------------------------------+
             |
             | Retorna JWT Token: "eyJhbGciOiJIUzI1NiIsInR5..."
             v
[ Cliente realiza requisições subsequentes: Header 'Authorization: Bearer <token>' ]
             |
             v
+----------------------------+
|      AWS API Gateway       |
+----------------------------+
             |
             | Encaminha /{proxy+} para EKS
             v
+-----------------------------------------------------------+
| EKS Pods (Spring Security / JwtService)                   |
| - Validação Criptográfica Matemática do JWT (Stateless)   |
| - Extração de Roles e Claims sem chamada adicional        |
| - Execução da Regra de Domínio                            |
+-----------------------------------------------------------+
```

### Detalhes Técnicos da Proposta:

1. **Desenvolvimento da Lambda (`lambda-auth`):**
   - Implementada em **Java 21 LTS** seguindo **Clean Architecture** (camadas `domain`, `application`, `infra` e `adapter`), sem dependência de containers servlet pesados para garantir inicialização rápida.
   - Endpoint: `POST /auth/login`.
   - Headers OWASP em todas as respostas (`Strict-Transport-Security`, `X-Content-Type-Options`, `X-Frame-Options`, `Cache-Control: no-store`).
2. **Tokens JWT Stateless:**
   - O payload do JWT encapsula `sub` (identificador do usuário), `email`, `roles` (`CUSTOMER`, `ATTENDANT`, `MECHANIC`, `ADMIN`) e tempo de expiração (`exp`).
   - Assinatura criptográfica com chave secreta simétrica (`JWT_SECRET`).
3. **Validação Descentralizada na Aplicação Principal:**
   - O core no EKS (`tech-challenge-repairshop-app`) valida a integridade matemática da assinatura do token via Spring Security (`JwtService`) em microssegundos, sem realizar nenhuma consulta externa ou chamada de rede.
4. **Infraestrutura e Observabilidade:**
   - Provisionamento via Terraform alocado nas sub-redes privadas da VPC com Security Group próprio (`aws_security_group.lambda_sg`).
   - Suporte à camada **AWS Distro for OpenTelemetry (ADOT)** para envio de traces distribuídos ao OTel Collector.

## IMPACTO ESPERADO
- **Benefícios:**
  - Custo zero em momentos ociosos (cobrança estritamente por milissegundo de execução de login).
  - Escalabilidade elástica instantânea durante picos de login sem pré-aquecimento.
  - Segurança aprimorada e isolamento completo do código de autenticação em repositório próprio.
  - Performance máxima na aplicação principal através de validação stateless em memória.
- **Riscos e Mitigações:**
  - *Risco:* Latência de inicialização a frio (*Cold Start*) em execuções esporádicas no runtime Java.
  - *Mitigação:* Uso do Java 21, código limpo e enxuto sem injeção de dependências pesadas, e memória dimensionada em 512MB/1024MB.
- **Impacto Operacional:**
  - CI/CD independente com testes unitários rápidos.

## ALTERNATIVAS CONSIDERADAS
1. **AWS Cognito User Pools:**
   - *Prós:* Serviço totalmente gerenciado pela AWS.
   - *Contras:* Custo por usuário ativo, customização limitada de regras de validação brasileiras (como validação algorítmica de CPF/CNPJ) e acoplamento direto de autenticação a um serviço proprietário.
2. **Autenticação Embutida no Monólito com Spring Security Session (Cookies/Stateful):**
   - *Prós:* Fácil de implementar inicialmente.
   - *Contras:* Inviabiliza a escalabilidade horizontal das réplicas no Kubernetes sem uso de clusters Redis para replicação de sessão.

## PONTOS EM ABERTO
- [x] Formato do payload da requisição de login (Alinhado: `{"cpf": "...", "password": "..."}`).
- [x] Tempo de expiração padrão do JWT (Alinhado: 24 horas para conveniência de uso no turno da oficina).
