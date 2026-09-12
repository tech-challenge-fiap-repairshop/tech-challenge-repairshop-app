# RFC – Ponto Único de Entrada com AWS API Gateway (HTTP API v2) e Roteamento Desacoplado

## DATA
23/08/2026

## STATUS
Encerrada – Aprovada

> **Nota de Encerramento:** Proposta aprovada por todo o time de infraestrutura e desenvolvimento, formalizada no [ADR-008](../ADRs/ADR-008-ponto-unico-de-entrada-com-aws-api-gateway.md).

## RESUMO
Proposta de implementação do AWS API Gateway (HTTP API v2) como Ponto Único de Entrada (*Single Point of Entry*) para todo o ecossistema da oficina mecânica, gerenciado via Terraform no repositório `tech-challenge-repairshop-infra-apigateway`, unificando as chamadas para a função AWS Lambda Auth e para os contêineres no cluster AWS EKS.

## PROBLEMA
Com a divisão da arquitetura em múltiplos serviços heterogêneos na AWS (Função Serverless AWS Lambda e Pods de aplicação no Kubernetes EKS), o consumo de APIs por clientes externos apresenta desafios significativos:
1. **Exposição de Múltiplos Hostnames Públicos:** Sem um gateway unificado, o cliente (Swagger UI, Postman, aplicativo frontend) precisaria conhecer e alternar entre URLs distintas para autenticar (`https://lambda-url...`) e para manipular ordens de serviço (`http://k8s-nlb-dns...`).
2. **Exposição Insegura da Topologia Interna:** Expor diretamente o Load Balancer do Kubernetes à internet pública viola boas práticas de segurança, revelando nomes de infraestrutura interna e abrindo portas diretamente nos nós.
3. **Complexidade de CORS e SSL:** Gerenciar certificados SSL/TLS, políticas de Cross-Origin Resource Sharing (CORS) e cabeçalhos de segurança em múltiplos pontos distintos gera inconsistências e brechas de segurança.

## PROPOSTA TÉCNICA
Propõe-se a criação do **AWS API Gateway (HTTP API v2)** como a única porta de entrada pública do sistema:

```
                            [ CONSUMIDORES EXTERNOS ]
                       (Postman, Frontend, Swagger UI)
                                     |
                                     v HTTPS
               +-------------------------------------------+
               |         AWS API GATEWAY (HTTP API v2)     |
               |         (Single Point of Entry)           |
               |         - CORS Centralizado               |
               |         - Ocultação de Topologia          |
               +-------------------------------------------+
                                /          \
             Rota: /auth/*     /            \   Rota: /{proxy+} (Default)
                              /              \
                             v                v
             +--------------------+    +--------------------+
             |   AWS Lambda Auth  |    |     AWS EKS NLB    |
             |   (POST /auth/...) |    | (Spring Boot App)  |
             +--------------------+    +--------------------+
```

### Detalhes Técnicos e Mapeamento de Rotas:

1. **Seleção de Tecnologia (HTTP API v2 vs REST API v1):**
   - Adoção da **HTTP API v2** devido ao seu menor custo (cerca de 70% mais barata por milhão de requisições) e latência reduzida (elimina camadas legadas de transformação do REST API v1).
2. **Mapeamento Declarativo de Rotas no Terraform (`infra-apigateway`):**
   - **Rota `/auth/{proxy+}` e `POST /auth/login`:**
     - Integração: `AWS_PROXY` direcionada para a função Lambda (`tech-challenge-repairshop-lambda-auth`).
   - **Rota `/{proxy+}` (Default Catch-All):**
     - Integração: `HTTP_PROXY` apontando para o DNS do Load Balancer interno do Kubernetes (`tech-challenge-repairshop-app`).
   - **Rota `/mailpit/{proxy+}` (Habilitada em `dev`):**
     - Integração proxy para a interface web de inspeção de e-mails em ambiente de testes.
3. **Descoberta Dinâmica de Recursos no Terraform:**
   - O Terraform do `infra-apigateway` utiliza data sources (`aws_lb`) baseados em tags dinâmicas para localizar o Load Balancer do EKS sem a necessidade de fixar endereços estáticos ou IPs manuais no código.
4. **Governança de CORS:**
   - Habilitação centralizada de CORS autorizando os métodos HTTP (`GET`, `POST`, `PUT`, `PATCH`, `DELETE`, `OPTIONS`) e cabeçalhos padrão (`Authorization`, `Content-Type`).

## IMPACTO ESPERADO
- **Benefícios:**
  - Ponto único de contato para todos os clientes com URL amigável e segura.
  - Blindagem total da infraestrutura interna da VPC e dos nós do EKS.
  - Alta performance e economia financeira com a tecnologia HTTP API v2.
  - Facilidade de expansão: novos microsserviços futuros podem ser acoplados com novas rotas no Gateway sem impactar os serviços existentes.
- **Riscos e Mitigações:**
  - *Risco:* Dependência central no API Gateway como ponto único de falha (*SPOF*).
  - *Mitigação:* O AWS API Gateway é um serviço nativamente serverless, multi-AZ e de altíssima disponibilidade garantida por SLA da AWS (99.95%+).
- **Impacto Operacional:**
  - Redução drástica da complexidade de rede e certificados no lado do cliente.

## ALTERNATIVAS CONSIDERADAS
1. **AWS API Gateway REST API Clássico (v1):**
   - *Prós:* Mais recursos legados de transformação VTL.
   - *Contras:* Custo mais elevado e maior latência de processamento por requisição desnecessária para nossa arquitetura de proxies diretos.
2. **AWS Application Load Balancer (ALB) com Ingress Controller no Kubernetes:**
   - *Prós:* Integração direta com manifestos Ingress do K8s.
   - *Contras:* Custo fixo mensal de instâncias de ALB mesmo em períodos ociosos em múltiplos ambientes, e integração menos direta com Lambdas isoladas.
3. **Exposição Direta via IPs Públicos dos Nós do EKS:**
   - *Prós:* Zero custo adicional.
   - *Contras:* Gravíssima vulnerabilidade de segurança e ausência de roteamento inteligente.

## PONTOS EM ABERTO
- [x] Protocolo de payload para a Lambda (Alinhado: Payload Format 2.0).
- [x] Tratamento de rotas de documentação Swagger (Alinhado: Roteado via proxy default para a aplicação no EKS).
