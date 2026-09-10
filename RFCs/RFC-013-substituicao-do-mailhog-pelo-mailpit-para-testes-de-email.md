# RFC – Substituição do MailHog pelo Mailpit como Servidor SMTP de Testes e Interceptação de E-mails

## DATA
05/09/2026

## STATUS
Encerrada – Aprovada

> **Nota de Encerramento:** Proposta aprovada por unanimidade pelo time técnico, originando formalmente o [ADR-013](../ADRs/ADR-013-substituicao-do-mailhog-pelo-mailpit-para-testes-de-email.md).

## RESUMO
Proposta de substituição do MailHog pelo Mailpit (`axllent/mailpit`) como ferramenta padrão de servidor SMTP simulado e interceptador visual de e-mails em todos os ambientes de desenvolvimento local e no cluster Kubernetes EKS em ambiente de desenvolvimento (`dev`).

## PROBLEMA
Nas fases iniciais do projeto, o **MailHog** foi adotado como ferramenta de emulação de servidor SMTP em contêiner Docker para interceptar e-mails disparados pelo sistema.

Com o amadurecimento do projeto, execução contínua de testes de integração e maior volume de mensagens geradas no fluxo de ordens de serviço, surgiram limitações severas no MailHog:
1. **Descontinuação e Falta de Manutenção:** O repositório oficial do MailHog não recebe atualizações, correções de segurança ou melhorias há anos, apresentando vulnerabilidades conhecidas em dependências legadas do Go.
2. **Consumo Excessivo de Memória e Instabilidade:** O MailHog mantém todas as mensagens na memória volátil sem indexação eficiente, sofrendo degradação de desempenho, lentidão na interface web e travamentos constantes ao acumular centenas de e-mails de teste.
3. **Interface e Recursos Obsoletos:** Falta de suporte a busca avançada por texto, ausência de visualização responsiva para simulação em telas de smartphones/tablets e carência de APIs REST modernas e WebSockets para validação em testes automatizados ponta a ponta.

## PROPOSTA TÉCNICA
Propõe-se a substituição direta (*drop-in replacement*) do **MailHog** pelo **Mailpit** (`axllent/mailpit:latest`), mantendo total compatibilidade com a aplicação:

```
[ Aplicação Spring Boot (JavaMailSender) ]
                      |
                      | Porta SMTP 1025 (Transparente / Sem alteração de código)
                      v
+-------------------------------------------------------------------------+
| Pod Mailpit (axllent/mailpit) no Namespace repairshop                   |
| - Consumo de Memória < 15MB (Go Otimizado)                              |
| - Armazenamento Efêmero com SQLite Integrado                            |
| - Limite de Retenção Configurável (MP_MAX_MESSAGES: 500)                |
| - Suporte a Usuário Não-Root (UID 10001 - Padrão de Segurança K8s)      |
+-------------------------------------------------------------------------+
                      |
                      +-------------------+-------------------+
                      |                                       |
                      v Porta 8025 (Web UI)                   v Porta 8025 (REST API)
+---------------------------------------+   +---------------------------------------+
| Interface Web Moderna e Responsiva    |   | API REST & WebSockets                 |
| - Visualização Desktop, Tablet, Mobile|   | - GET /api/v1/messages                |
| - Inspeção de Cabeçalhos e Anexos     |   | - Validação Automatizada em Testes    |
| - Análise de Spam (SpamAssassin Score)|   |   de Integração Assíncronos           |
+---------------------------------------+   +---------------------------------------+
```

### Detalhes da Substituição e Configuração:

1. **Compatibilidade Transparente (*Drop-in Replacement*):**
   - Utiliza exatamente as mesmas portas padrão: `1025` para tráfego SMTP e `8025` para a interface web/API REST.
   - Nenhuma linha de código da aplicação Spring Boot precisou ser alterada (`SPRING_MAIL_HOST: mailpit`, `SPRING_MAIL_PORT: 1025`).
2. **Performance e Baixo Consumo de Recursos:**
   - Implementado em Go moderno, consumindo menos de 15MB de memória RAM (redução de mais de 70% em relação ao MailHog).
   - Utilização de banco temporário SQLite embutido com indexação rápida para buscas instantâneas.
   - Configuração do limite de retenção de mensagens (`MP_MAX_MESSAGES: 500`) para evitar esgotamento de disco ou memória.
3. **Manifesto Kubernetes Seguro (`k8s/mailpit.yaml`):**
   - Execução como usuário não-root (UID `10001`), volume efêmero `emptyDir` e Service `ClusterIP` no namespace `repairshop`.

## IMPACTO ESPERADO
- **Benefícios:**
  - Estabilidade e segurança garantidas com uma ferramenta mantida ativamente pela comunidade open-source.
  - Redução drástica de consumo de memória e fim dos travamentos na visualização de e-mails em testes.
  - Recursos avançados: visualização responsiva em mobile, teste de pontuação de spam e inspeção de cabeçalhos brutos.
  - Integração perfeita com testes automatizados via API REST `/api/v1/messages`.
- **Riscos e Mitigações:**
  - *Risco:* Compatibilidade de comandos e variáveis de ambiente.
  - *Mitigação:* O Mailpit foi desenvolvido especificamente como um substituto 100% compatível para o MailHog.
- **Impacto Operacional:**
  - Melhoria significativa na produtividade e experiência de depuração do time de desenvolvimento e QA.

## ALTERNATIVAS CONSIDERADAS
1. **Manutenção do MailHog com Limpeza Periódica de Memória:**
   - *Prós:* Não exige troca de imagem de contêiner.
   - *Contras:* Mantém uma ferramenta abandonada com riscos de segurança conhecidos e sem suporte a novos recursos.
2. **Mailcatcher (Ruby) ou Maildev (Node.js):**
   - *Prós:* Simulam servidores SMTP.
   - *Contras:* Consumo de memória e dependências de runtime (Ruby/Node) substancialmente maiores do que o binário estático compilado em Go do Mailpit.

## PONTOS EM ABERTO
- [x] Limite de mensagens retidas no Pod (Alinhado: `MP_MAX_MESSAGES=500`).
- [x] Rota de acesso via API Gateway (Alinhado: Mapeada em `/mailpit/*` em ambiente de dev).
