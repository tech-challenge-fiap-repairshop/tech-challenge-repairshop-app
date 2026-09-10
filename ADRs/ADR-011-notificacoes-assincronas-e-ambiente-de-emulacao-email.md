# ADR-011: Notificações de Status de Ordem de Serviço e Interceptação em Desenvolvimento com Mailpit

**Data:** 2026-09-01  
**Status:** Aceito  
**Autor:** Grupo CAO (POSTECH 15SOAT)  

---

## Contexto

A experiência do cliente no fluxo de oficina mecânica exige transparência e comunicação em tempo real. A cada alteração de estado no ciclo de vida de uma **Ordem de Serviço (OS)** — como emissão de diagnóstico, solicitação de aprovação de orçamento, início de execução na oficina, conclusão dos reparos e emissão da fatura para pagamento —, o sistema deve enviar notificações por e-mail com os detalhes do veículo, serviços e valores.

Entretanto, essa funcionalidade traz desafios operacionais para os ambientes inferiores:
1. **Risco de Disparo de E-mails Fictícios para Clientes Reais:** Em ambientes de desenvolvimento (`dev`), homologação (`hml`) ou testes locais, o disparo de e-mails para endereços reais cadastrados em bases de teste geraria ruído, riscos de privacidade e potenciais incidentes de suporte.
2. **Custo e Complexidade de SMTP em Dev:** Manter credenciais de servidores SMTP comerciais ativos (ex: SendGrid, Amazon SES) em ambientes efêmeros ou de desenvolvimento local introduz complexidade de gestão de credenciais e custos adicionais.
3. **Dificuldade de Validação Visual:** Desenvolvedores e testadores precisam inspecionar visualmente o layout, formatação HTML e cabeçalhos dos e-mails disparados sem precisar acessar caixas de entrada reais.

---

## Decisão

Implementar o serviço de envio de notificações por e-mail na aplicação utilizando o **Spring Boot Starter Mail** associado a uma estratégia de **Segregação por Ambiente via ConfigMaps do Kubernetes**, utilizando o **Mailpit** como servidor SMTP simulado e visualizador web em ambientes locais e de desenvolvimento.

### Detalhes Técnicos e Arquiteturais:

1. **Camada de Aplicação e Domínio:**
   - A lógica de envio de notificações é disparada através de eventos e casos de uso da camada de aplicação (`NotifyCustomerUseCase`), garantindo que o disparo seja um efeito colateral desacoplado das regras puras de transição de status da OS.
   - O serviço utiliza templates estruturados em HTML para compor o corpo do e-mail de acordo com o status atual da ordem de serviço.

2. **Isolamento por Ambiente via ConfigMaps (`k8s/configmap/`):**
   - **Ambiente Local (`configmap-local.yaml`) e DEV (`configmap-dev.yaml`):**
     - `SPRING_MAIL_HOST`: `mailpit-service` (resolução interna no cluster Kubernetes) ou `localhost`.
     - `SPRING_MAIL_PORT`: `1025` (porta SMTP padrão do Mailpit).
     - As mensagens são capturadas pelo Mailpit e armazenadas temporariamente em memória.
     - A interface gráfica do Mailpit é exposta na porta `8025` (e via rota `/mailpit/*` no API Gateway em dev) para que desenvolvedores visualizem instantaneamente os e-mails enviados.
   - **Ambiente de Homologação (`configmap-hml.yaml`) e Produção (`configmap-prd.yaml`):**
     - `SPRING_MAIL_HOST`: Aponta para o servidor SMTP corporativo ou Amazon SES.
     - `SPRING_MAIL_PORT`: `587` / `465` (com autenticação TLS segura e credenciais injetadas via Kubernetes Secrets).

3. **Provisionamento do Mailpit no Kubernetes (`k8s/mailpit.yaml`):**
   - Contêiner leve baseado na imagem oficial do Mailpit alocado no namespace `repairshop` com portas `1025` (SMTP) e `8025` (Web UI).

---

## Consequências

### Positivas
- **Total Segurança contra Envios Acidentais:** Elimina qualquer possibilidade de e-mails de teste vazarem para caixas postais de clientes reais durante o desenvolvimento e homologação.
- **Validação Imediata de Layout e Conteúdo:** Desenvolvedores e analistas de QA podem validar o HTML, anexos e links de aprovação de orçamento diretamente na interface visual do Mailpit em segundos.
- **Zero Custo de SMTP em Ambientes de Desenvolvimento:** Não há necessidade de contratar provedores pagos de disparo de e-mails para desenvolvimento e testes.
- **Transição Transparente para Produção:** A aplicação utiliza exatamente a mesma abstração de envio (`JavaMailSender`), alterando apenas as variáveis de host e porta do ConfigMap ao ser promovida para produção.

### Negativas / Trade-offs
- **Recurso Adicional em Dev:** Exige a execução de um Pod adicional no namespace `repairshop` para o Mailpit nos ambientes de desenvolvimento.

### Riscos e Mitigações
- **Risco:** Falha silenciosa no envio de e-mails bloquear a transição de status da OS caso a chamada seja síncrona.
- **Mitigação:** Tratamento de exceções com logs estruturados para garantir que a falha de envio de e-mail não reverta a transação principal de alteração de status da Ordem de Serviço no banco de dados.
