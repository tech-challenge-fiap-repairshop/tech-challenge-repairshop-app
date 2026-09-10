# ADR-013: Substituição do MailHog pelo Mailpit como Servidor SMTP de Testes e Interceptação de E-mails

**Data:** 2026-09-08  
**Status:** Aceito  
**Autor:** Grupo CAO (POSTECH 15SOAT)  

---

## Contexto

No ciclo de vida de uma Ordem de Serviço (OS) na oficina mecânica (*RepairShop*), diversas etapas exigem a emissão de comunicações transacionais para os clientes — como orçamento para aprovação, autorização de início dos serviços, aviso de conclusão dos reparos e envio de link para faturamento/pagamento.

Nas fases iniciais do projeto, adotou-se o **MailHog** como ferramenta de simulação de servidor SMTP e interceptação de e-mails em contêiner Docker para ambientes de desenvolvimento local e testes.

Contudo, com o avanço do desenvolvimento, expansão da suíte de testes integrados e aumento da volumetria de mensagens simuladas, foram identificadas limitações críticas no MailHog:
1. **Descontinuação e Falta de Manutenção:** O projeto original do MailHog foi descontinuado e não recebe atualizações ativas de segurança, correções de bugs ou melhorias de compatibilidade há anos.
2. **Degradação de Desempenho e Vazamento de Memória:** O MailHog armazena mensagens em memória sem indexação avançada, apresentando alto consumo de memória RAM, lentidão na interface web e travamentos ao acumular centenas de mensagens durante baterias contínuas de testes.
3. **Interface e Recursos Limitados:** Falta de suporte a busca full-text performática, ausência de visualização responsiva de templates HTML para dispositivos móveis, e ausência de WebSockets eficientes para testes automatizados ponta a ponta.

Houve, portanto, a necessidade de selecionar e adotar um substituto moderno, performático, de alta capacidade de armazenamento e compatível com a infraestrutura existente.

---

## Decisão

Decidimos substituir formalmente o **MailHog** pelo **Mailpit** (`axllent/mailpit:latest`) como a ferramenta padrão de interceptação de e-mails e simulação SMTP em todos os ambientes não produtivos (desenvolvimento local e cluster Kubernetes EKS em `dev`).

### Detalhes Técnicos e Arquiteturais:

1. **Compatibilidade Transparente (*Drop-in Replacement*):**
   - O Mailpit opera nas mesmas portas canônicas utilizadas anteriormente pelo MailHog: porta `1025` para recepção de tráfego SMTP e porta `8025` para a interface gráfica web e API REST.
   - Nenhuma alteração foi necessária no código-fonte da aplicação Spring Boot (`JavaMailSender`), mantendo-se as propriedades padronizadas `SPRING_MAIL_HOST: mailpit` e `SPRING_MAIL_PORT: 1025`.

2. **Alta Capacidade de Armazenamento e Performance:**
   - O Mailpit é implementado em Go moderno, consumindo menos de **15MB de memória** em repouso e utilizando um banco de dados temporário SQLite altamente indexado e rápido para persistência de mensagens.
   - Capacidade configurável de retenção de mensagens via variável de ambiente `MP_MAX_MESSAGES` (definida como `500` por padrão no Docker Compose e Kubernetes), evitando consumo descontrolado de disco e memória.

3. **Recursos Avançados para Desenvolvimento e QA:**
   - **Interface Web Moderna e Responsiva:** Suporte a visualização em desktop, tablet e mobile, testes de renderização de HTML, headers brutos (*raw headers*), inspeção de anexos e cálculo de pontuação de spam (*SpamAssassin check*).
   - **API REST e WebSockets:** Disponibilização de endpoints REST completos (`/api/v1/messages`) e WebSocket para que testes automatizados de integração possam consultar e validar o recebimento de e-mails de forma assíncrona e determinística.

4. **Padronização em Todos os Ambientes:**
   - **Local (`docker-compose.yml`):** Provisionamento do serviço `mailpit` na rede interna da aplicação.
   - **Cluster EKS (`k8s/mailpit.yaml` e `k8s/configmap/configmap-dev.yaml`):** Pod seguro executando como usuário não-root (UID `10001`), com volumes efêmeros (`emptyDir`) e exposto internamente via Service `ClusterIP` no namespace `repairshop`.
   - **Produção (`k8s/configmap/configmap-prd.yaml`):** O Mailpit não é implantado; o `ConfigMap` de produção aponta diretamente para o serviço de mensageria corporativo (Amazon SES / SMTP TLS na porta `587`).

---

## Consequências

### Positivas
- **Garantia de Estabilidade e Manutenibilidade:** Adoção de uma ferramenta mantida ativamente pela comunidade open-source com constantes atualizações de segurança.
- **Excelente Eficiência de Recursos:** Redução de mais de 70% no consumo de memória em relação ao MailHog, com velocidade de processamento de e-mails ordens de grandeza superior.
- **Zero Impacto no Código da Aplicação:** A substituição ocorreu exclusivamente na camada de infraestrutura e manifestos de contêiner, preservando 100% da lógica de negócio e serviços de notificação da aplicação.
- **Melhor Experiência de Depuração:** A interface moderna e a API REST facilitam a inspeção visual por desenvolvedores e a automação de testes de regressão de e-mails.

### Negativas / Trade-offs
- **Dependência de Imagem Externa:** Necessidade de gerenciar e atualizar a imagem Docker `axllent/mailpit` no pipeline de segurança e verificação de vulnerabilidades (Trivy).

### Riscos e Mitigações
- **Risco:** Acúmulo excessivo de mensagens em execuções longas de testes sobrecarregar o volume temporário do Pod no Kubernetes.
- **Mitigação:** Configuração explícita do parâmetro de retenção máxima de mensagens (`MP_MAX_MESSAGES: 500`) e definição de limites estritos de `resources.limits` e `ephemeral-storage` no manifesto `k8s/mailpit.yaml`.
