# ADR-007: Isolamento do Serviço de Autenticação em Microsserviço Serverless AWS Lambda e JWT Stateless

**Data:** 2026-09-01  
**Status:** Aceito  
**Autor:** Grupo CAO (POSTECH 15SOAT)  

---

## Contexto

A segurança e autenticação do sistema de oficina mecânica exige a identificação segura de usuários (clientes e atendentes/administradores) para autorizar operações na API (criação de OS, aprovação de orçamentos, baixa de estoque, faturamento).

Ao analisar a arquitetura da Fase 3, identificamos os seguintes desafios:
1. **Padrão de Acesso Desproporcional:** O fluxo de login (`POST /auth/login`) possui um comportamento de acesso em rajadas (picos no início do expediente), mas não consome recursos computacionais constantes ao longo do dia. Manter Pods dedicados exclusivamente para autenticação no Kubernetes geraria custo ocioso.
2. **Desacoplamento e Segurança:** O microsserviço de autenticação deve ser isolado do core de negócio de ordens de serviço e estoque, reduzindo a superfície de ataque e garantindo que problemas em regras de negócio não afetem o serviço de login.
3. **Comunicação Stateless:** O sistema não deve manter sessões em memória no servidor (*session state*), para que qualquer réplica do Kubernetes consiga atender requisições autenticadas de forma independente.

---

## Decisão

Isolar a responsabilidade de autenticação em um **Microsserviço Serverless** desenvolvido em **Java 21 com Clean Architecture**, hospedado no **AWS Lambda** (`tech-challenge-repairshop-lambda-auth`), utilizando tokens **JWT (JSON Web Tokens) Stateless** assinados com algoritmo **HMAC-SHA256**.

### Detalhes Técnicos e Arquiteturais:

1. **Estrutura do Microsserviço Lambda (`lambda-auth`):**
   - Implementação em **Java 21 LTS** gerenciado com Clean Architecture (camadas `domain`, `application`, `infra` e `adapter`), garantindo total separação entre regras de validação de credenciais e a infraestrutura AWS.
   - Endpoint exposto: `POST /auth/login`, recebendo identificador (CPF) e Senha.
   - Validação defensiva de CPF e senhas antes de disparar o processamento.
   - Comunicação segura com o serviço de banco de dados/usuários via Feign Client e emissão do token JWT assinado criptograficamente com chave secreta compartilhada (`JWT_SECRET`).

2. **Segurança e Headers OWASP:**
   - As respostas HTTP do Lambda incorporam headers rigorosos de segurança recomendados pela OWASP:
     - `Strict-Transport-Security: max-age=31536000; includeSubDomains`
     - `X-Content-Type-Options: nosniff`
     - `X-Frame-Options: DENY`
     - `Cache-Control: no-store, no-cache, must-revalidate, max-age=0`

3. **Arquitetura de Tokens JWT Stateless:**
   - O payload do JWT gerado contém identificador do usuário (`sub`), e-mail, perfis de acesso (`roles`: `CUSTOMER`, `ATTENDANT`) e tempo de expiração (`exp`).
   - A aplicação principal no EKS (`tech-challenge-repairshop-app`) valida a assinatura criptográfica do JWT de forma puramente matemática e stateless através do Spring Security (`JwtService`), sem necessidade de consultar bancos de dados ou a Lambda a cada requisição HTTP subsequente.

4. **Infraestrutura e Redes:**
   - A função Lambda é provisionada via Terraform associada à VPC privada através de Security Group próprio (`aws_security_group.lambda_sg`).
   - Suporte integrado à camada **AWS Distro for OpenTelemetry (ADOT) Lambda Layer** para rastreamento distribuído de ponta a ponta.

---

## Consequências

### Positivas
- **Escala Elástica Instantânea e Custo Zero Ocioso:** A cobrança da AWS Lambda ocorre estritamente por milissegundo de execução de login, com capacidade de absorver milhares de requisições concorrentes sem necessidade de provisionamento prévio.
- **Arquitetura Stateless e Desacoplada:** A validação distribuída do JWT no EKS garante que a aplicação principal escale horizontalmente sem sincronização de sessões de usuário.
- **Isolamento de Falhas e Superfície de Ataque:** O módulo de autenticação possui ciclo de vida, repositório Git e esteira CI/CD completamente independentes do restante da aplicação.
- **Qualidade Estrutural:** Código em Clean Architecture no Java 21 permite testes unitários ultrarrápidos e excelente manutenibilidade.

### Negativas / Trade-offs
- **Latência de Inicialização a Frio (*Cold Start*):** Execuções esporádicas no runtime Java da AWS Lambda podem apresentar latência ligeiramente superior na primeira requisição após inatividade.
- **Gerenciamento de Segredos Compartilhados:** A chave criptográfica do JWT deve estar sincronizada entre a Lambda Auth e a aplicação principal (mitigado pelo uso de Secrets do GitHub Actions e Secrets do Kubernetes).

### Riscos e Mitigações
- **Risco:** Impacto de *cold start* em Java na percepção do usuário.
- **Mitigação:** Uso do Java 21 LTS com otimizações de JVM, dimensionamento de memória adequado na Lambda (512MB/1024MB) e dependências leves sem inicialização pesada de Spring Context na Lambda.
