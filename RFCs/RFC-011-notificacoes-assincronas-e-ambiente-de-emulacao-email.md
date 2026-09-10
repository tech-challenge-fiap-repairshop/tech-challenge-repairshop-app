# RFC – Notificações de Status de Ordem de Serviço e Interceptação em Desenvolvimento com Mailpit

## DATA
26/08/2026

## STATUS
Encerrada – Aprovada

> **Nota de Encerramento:** Proposta aprovada e integrada ao fluxo de notificações da aplicação, formalizada no [ADR-011](../ADRs/ADR-011-notificacoes-assincronas-e-ambiente-de-emulacao-email.md).

## RESUMO
Proposta de implementação de um mecanismo de disparo de notificações por e-mail no ciclo de vida das Ordens de Serviço (OS), com segregação de ambientes através de ConfigMaps do Kubernetes e interceptação segura de mensagens em desenvolvimento utilizando o servidor SMTP emulado Mailpit.

## PROBLEMA
No fluxo de atendimento da oficina mecânica (*RepairShop*), os clientes precisam ser informados em tempo real sobre o status de seus veículos:
- Emissão de diagnóstico técnico inicial;
- Solicitação de aprovação de orçamento detalhado (com link e valores de insumos/serviços);
- Início da execução dos reparos na oficina;
- Conclusão dos trabalhos e disponibilização do veículo para retirada;
- Emissão de fatura e confirmação de pagamento.

No entanto, a implementação desse serviço gera riscos nos ambientes inferiores:
1. **Risco de Disparo de E-mails Fictícios para Caixas Postais Reais:** Testes executados em ambientes de desenvolvimento (`dev`), homologação (`hml`) ou baterias automáticas com e-mails reais de clientes poderiam causar constrangimentos, mensagens indevidas e potenciais incidentes legais/LGPD.
2. **Custos e Complexidade de SMTP em Dev:** Manter contas ativas e credenciais em provedores pagos de envio transacional (ex: Amazon SES, SendGrid) em ambientes efêmeros é caro e complexo de gerenciar.
3. **Dificuldade de Validação Visual de Templates HTML:** Desenvolvedores e testadores necessitam validar formatações visuais, tabelas de orçamento e links de e-mails em tempo real sem precisar abrir caixas de correio reais.

## PROPOSTA TÉCNICA
Propõe-se a implementação do envio de e-mails na aplicação via **Spring Boot Starter Mail**, desacoplado das regras de domínio através de casos de uso da camada de aplicação, com configuração desacoplada por **ConfigMaps do Kubernetes** e uso do **Mailpit** como servidor SMTP emulado:

```
[ Transição de Status da OS ]
             |
             v
+-----------------------------------------------------------+
| NotifyCustomerUseCase (Camada Application)                |
| - Montagem do Template HTML (Orçamento / Status)          |
| - Disparo via JavaMailSender                              |
+-----------------------------------------------------------+
             |
             +-----------------------+-----------------------+
             |                                               |
             v (Ambientes DEV / Local)                       v (Ambiente de Produção)
+---------------------------------------+       +---------------------------------------+
| ConfigMap: configmap-dev.yaml         |       | ConfigMap: configmap-prd.yaml         |
| Host: mailpit-service (Porta: 1025)   |       | Host: email-smtp.us-east-1.amazonaws  |
|                                       |       | Port: 587 (TLS com Secrets)           |
| -> Captura Mensagem no Pod Mailpit    |       |                                       |
| -> Interface Web na Porta 8025 / API  |       | -> Entrega Real para Caixa do Cliente |
+---------------------------------------+       +---------------------------------------+
```

### Detalhes Estruturais da Proposta:

1. **Desacoplamento e Tratamento Não-Bloqueante:**
   - O disparo de e-mails é tratado na camada de aplicação (`NotifyCustomerUseCase`) como um efeito colateral da transição de status.
   - Tratamento defensivo de exceções: caso o envio de e-mail falhe, a transição de status da OS no banco de dados não é abortada, registrando-se o erro nos logs com *Trace ID*.
2. **Segregação por ConfigMap no Kubernetes (`k8s/configmap/`):**
   - **`configmap-dev.yaml`:** Aponta para o serviço `mailpit-service:1025` no namespace `repairshop`.
   - **`configmap-prd.yaml`:** Aponta para o servidor SMTP corporativo / Amazon SES com credenciais injetadas por Kubernetes Secrets.
3. **Provisionamento do Mailpit (`k8s/mailpit.yaml`):**
   - Execução de contêiner leve no namespace `repairshop` escutando nas portas `1025` (SMTP) e `8025` (Interface Web/API REST).

## IMPACTO ESPERADO
- **Benefícios:**
  - 100% de proteção contra vazamento acidental de e-mails de teste para clientes reais.
  - Validação imediata de layouts HTML, tabelas de orçamento e links na interface web do Mailpit.
  - Zero custo financeiro de envio de e-mails em desenvolvimento e homologação.
  - Troca transparente de ambiente sem alterar uma única linha de código Java/Kotlin.
- **Riscos e Mitigações:**
  - *Risco:* Manutenção de um Pod adicional no Kubernetes nos ambientes inferiores.
  - *Mitigação:* O Mailpit possui baixíssimo consumo de memória (<15MB) e volume efêmero.
- **Impacto Operacional:**
  - Experiência fluida de desenvolvimento e validação de QA.

## ALTERNATIVAS CONSIDERADAS
1. **Mock Manual de `JavaMailSender` no Código:**
   - *Prós:* Não exige nenhum servidor SMTP.
   - *Contras:* Não valida o empacotamento MIME real, a codificação UTF-8 ou a renderização visual dos templates HTML.
2. **Utilização de Provedor Comercial Real (Amazon SES / SendGrid) em Dev:**
   - *Prós:* Testa o canal SMTP comercial.
   - *Contras:* Risco iminente de envio indevido para clientes reais e consumo desnecessário de limites de envio e custos de nuvem.

## PONTOS EM ABERTO
- [x] Configuração da porta SMTP (Alinhado: 1025 para Mailpit e 587 para produção com STARTTLS).
- [x] Rota de acesso à interface do Mailpit em `dev` (Alinhado: Exposta via API Gateway em `/mailpit/*`).
