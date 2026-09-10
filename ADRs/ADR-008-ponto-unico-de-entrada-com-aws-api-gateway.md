# ADR-008: Ponto Único de Entrada com AWS API Gateway (HTTP API v2) e Roteamento Desacoplado

**Data:** 2026-09-01  
**Status:** Aceito  
**Autor:** Grupo CAO (POSTECH 15SOAT)  

---

## Contexto

Com a evolução da arquitetura para múltiplos serviços heterogêneos na AWS (Função Serverless AWS Lambda para autenticação e Cluster Kubernetes AWS EKS para a aplicação principal da oficina), era necessário definir como os clientes externos (Postman, Swagger UI, navegadores web e aplicações móveis) consumiriam os endpoints do sistema.

Desafios identificados:
1. **Exposição Direta de Endpoints Heterogêneos:** Expor múltiplos endpoints e URLs públicas diferentes (ex: a URL da AWS Lambda e o DNS do Load Balancer do EKS) aumentaria a complexidade do lado do cliente, geraria problemas de CORS e dificultaria a gestão de domínios e certificados SSL.
2. **Segurança e Ocultação de Topologia:** A infraestrutura interna da VPC (IPs dos nós, portas internas e nomes de Load Balancers do Kubernetes) não deve ser visível ou conhecida diretamente pelo consumidor final.
3. **Custo e Latência de Gateway:** A escolha da tecnologia de API Gateway na AWS precisava equilibrar performance (baixa latência), suporte a proxies HTTP dinâmicos e custo acessível para os 3 ambientes.

---

## Decisão

Adotar o **AWS API Gateway (HTTP API v2)** como o **Ponto Único de Ingress (Single Point of Entry)** de todo o ecossistema, gerenciado no repositório `tech-challenge-repairshop-infra-apigateway` via Terraform, com roteamento desacoplado para a AWS Lambda e para o cluster EKS.

### Detalhes Técnicos e Arquiteturais:

1. **Escolha do Tipo de API Gateway (HTTP API v2 vs REST API v1):**
   - Optamos pelo **HTTP API (v2)** em detrimento do REST API clássico (v1) por oferecer:
     - **Até 70% de redução de custo** por milhão de requisições.
     - **Menor latência média de resposta** (overhead de processamento do gateway reduzido em até 60%).
     - Suporte nativo a integrações com AWS Lambda (formato de payload 2.0) e integração HTTP Proxy com Load Balancers.

2. **Mapeamento Declarativo de Rotas:**
   - **Roteamento de Autenticação (`/auth/*`):**
     - Rota: `ANY /auth/{proxy+}` e `POST /auth/login`
     - Integração: AWS_PROXY conectada diretamente à função `tech-challenge-repairshop-lambda-auth`.
     - O tráfego de login é atendido em milissegundos pela camada Serverless.
   - **Roteamento da Aplicação Principal (`/{proxy+}`):**
     - Rota: `ANY /{proxy+}` (captura todas as rotas de clientes, veículos, insumos, ordens de serviço, execuções, faturas e Swagger).
     - Integração: `HTTP_PROXY` apontando diretamente para o Load Balancer do Kubernetes no EKS (`repairshop-app-service`).
   - **Roteamento de Desenvolvimento/Mailpit (`/mailpit/*`):**
     - Rota habilitada em ambiente `dev` para permitir acesso externo seguro à interface visual do interceptador de e-mails.

3. **Descoberta Dinâmica de Infraestrutura no Terraform:**
   - O repositório `infra-apigateway` utiliza data sources do Terraform (`aws_lb`) baseados em tags e convenções de nomenclatura para descobrir automaticamente o DNS do Load Balancer criado pelos manifestos Kubernetes no repositório do EKS, eliminando acoplamentos estáticos de IPs ou DNS hardcoded.

4. **Controle Centralizado de CORS:**
   - Configuração de Cross-Origin Resource Sharing (CORS) centralizada no Gateway, autorizando métodos HTTP padrão (`GET`, `POST`, `PUT`, `PATCH`, `DELETE`, `OPTIONS`) e headers (`Authorization`, `Content-Type`).

---

## Consequências

### Positivas
- **Ponto Único de Acesso Unificado:** Clientes externos consomem um único hostname base HTTPS para todas as operações da oficina (login, consultas e transações), com total transparência quanto à implementação interna.
- **Isolamento de Segurança e Ocultação de Topologia:** A topologia de rede privada, subnets e portas internas dos nós do EKS permanecem totalmente ocultas atrás do Gateway.
- **Excelente Desempenho e Eficiência Financeira:** O protocolo HTTP API v2 garante latência ultrabaixa com o menor custo de tarifação na AWS.
- **Roteamento Flexível:** Facilidade para adicionar novos microsserviços futuros (ex: microsserviço de pagamentos ou relatórios) apenas adicionando novas rotas no Terraform do Gateway sem alterar a aplicação existente.

### Negativas / Trade-offs
- **Dependência Central do Gateway:** O API Gateway torna-se o caminho crítico de entrada para todo o tráfego externo.

### Riscos e Mitigações
- **Risco:** Falha de roteamento caso o Load Balancer do Kubernetes no EKS seja recriado com outro DNS.
- **Mitigação:** Automação via Terraform com `lifecycle { create_before_destroy = true }` e pipelines de CI/CD ordenadas conforme documentado no manual de subida de infraestrutura.
