# Dicionário de Linguagem Ubíqua
## Sistema de Gestão — Oficina Mecânica

> Este dicionário define os termos do domínio utilizados no Event Storming e no modelo de domínio do sistema. Cada termo possui um único significado dentro do seu Contexto Delimitado. Quando o mesmo termo aparece em contextos diferentes, o significado é explicitado separadamente.

---

## Legenda de Tipos

| Tipo | Descrição |
|---|---|
| `Agregado` | Unidade de consistência com Aggregate Root |
| `Entidade` | Objeto com identidade própria |
| `Value Object` | Objeto imutável definido pelos seus atributos |
| `Ator` | Pessoa ou sistema que interage com o domínio |
| `Sistema Externo` | Sistema fora do boundary do domínio |
| `Evento` | Algo que aconteceu no domínio (passado) |
| `Comando` | Intenção de realizar uma ação |
| `Política` | Regra que conecta um Evento a um Comando |
| `Modelo de Leitura` | Dado consultado para apoiar uma decisão |

---

## Contexto: Cadastro

> Responsável pelo registro de clientes e veículos. É o ponto de entrada do sistema — nenhuma OS pode ser criada sem cliente e veículo cadastrados.

---

### Cliente
**Tipo:** Ator

Pessoa que solicita o serviço à oficina. Inicia o fluxo ao requisitar o atendimento. Também interage na avaliação da OS (aprovação ou não aprovação).

> ⚠️ **Atenção:** Aparece como Ator em dois momentos: no Cadastro ao requisitar o serviço, e na Ordem de Serviço ao avaliar o orçamento.

---

### Atendente
**Tipo:** Ator

Funcionário responsável por cadastrar o cliente e o veículo no sistema, criar a OS e enviar o orçamento ao cliente.

---

### Requisita o serviço
**Tipo:** Comando

Ação do Cliente que inicia o fluxo de cadastro. Dispara a necessidade de verificar se o cliente já está cadastrado no sistema.

---

### Serviço requisitado
**Tipo:** Evento

Evento gerado após o cliente solicitar o serviço. Marca o início do processo de cadastro e verificação.

---

### Informações do cliente
**Tipo:** Modelo de Leitura

Dados do cliente consultados ou coletados antes do cadastro: nome, CPF/CNPJ, contato e endereço. Usado pelo Atendente para executar o comando de cadastro.

---

### Cadastrar cliente
**Tipo:** Comando

Ação do Atendente para registrar os dados do cliente no sistema. Executado apenas se o cliente não estiver cadastrado.

> 📌 **Observação:** Se o cliente já estiver cadastrado, o fluxo avança diretamente para o cadastro do veículo.

---

### Cliente cadastrado
**Tipo:** Evento

Evento que confirma o registro do cliente no sistema. Habilita o próximo passo: cadastro do veículo.

---

### Informações do veículo
**Tipo:** Modelo de Leitura

Dados do veículo necessários para o cadastro: placa, marca, modelo, ano e cor. Fornecidos pelo Cliente e inseridos pelo Atendente.

---

### Cadastrar veículo
**Tipo:** Comando

Ação do Atendente para registrar o veículo associado ao cliente no sistema. Executado apenas se o veículo não estiver cadastrado.

> 📌 **Observação:** Se o veículo já estiver cadastrado, o fluxo avança sem novo cadastro.

---

### Veículo cadastrado
**Tipo:** Evento

Evento que confirma o registro do veículo no sistema, associado ao cliente. Habilita a criação da OS.

---

### Cadastro
**Tipo:** Agregado

Agregado responsável por gerenciar o registro de clientes e veículos. Protege as invariantes de unicidade e validade dos dados cadastrais.

---

## Contexto: Ordem de Serviço

> Contexto central do domínio. Gerencia todo o ciclo de vida do atendimento: da abertura ao encerramento. Concentra as decisões de negócio mais críticas, incluindo a aprovação ou não aprovação do orçamento pelo cliente.

---

### Mecânico
**Tipo:** Ator

Profissional técnico responsável por realizar o diagnóstico do veículo, identificando os serviços necessários, peças e insumos.

---

### Cria a OS
**Tipo:** Comando

Ação do Atendente para abrir uma nova Ordem de Serviço no sistema, com base no cliente e veículo cadastrados.

---

### OS criada
**Tipo:** Evento

Evento que confirma a abertura da Ordem de Serviço. Dispara a política de solicitar diagnóstico ao mecânico.

---

### Solicitar diagnóstico para criação de OS
**Tipo:** Política

Quando a OS é criada, o sistema dispara automaticamente a solicitação de diagnóstico ao mecânico.

---

### Peças, Insumos e Serviços
**Tipo:** Modelo de Leitura

Lista de peças, insumos e serviços identificados durante o diagnóstico. Usada como base para o cálculo do orçamento da OS.

---

### Realiza diagnóstico
**Tipo:** Comando

Ação do Mecânico para registrar no sistema os serviços identificados, peças e insumos necessários e os valores estimados.

---

### Diagnóstico realizado
**Tipo:** Evento

Evento que confirma a conclusão do diagnóstico técnico. É um **evento pivotal** — separa a fase de avaliação técnica da fase comercial. Dispara a política de enviar o orçamento ao cliente.

---

### Enviar e-mail com orçamento da OS para cliente
**Tipo:** Política

Quando o diagnóstico é realizado, o sistema gera automaticamente o orçamento e envia por e-mail ao cliente para avaliação.

---

### Orçamento da OS
**Tipo:** Modelo de Leitura

Documento com o detalhamento financeiro dos serviços e peças identificados no diagnóstico. Enviado ao cliente para aprovação.

---

### Envia OS
**Tipo:** Comando

Ação do Atendente para enviar formalmente a OS com o orçamento ao cliente.

---

### E-mail com OS enviado
**Tipo:** Evento

Evento que confirma o envio do e-mail com a OS e orçamento ao cliente.

---

### Avalia OS
**Tipo:** Comando

Ação do Cliente de analisar o orçamento recebido e tomar uma decisão: aprovar ou não aprovar os serviços.

---

### OS aprovada
**Tipo:** Evento

Evento gerado quando o Cliente aceita o orçamento. É um **evento pivotal** — separa a fase comercial da fase de execução. Dispara a política de notificar a necessidade de peças e insumos.

---

### Notificar necessidade de peças e insumos
**Tipo:** Política

Quando a OS é aprovada, o sistema notifica o Atendente sobre a necessidade de verificar e providenciar as peças e insumos para execução.

---

### OS não aprovada
**Tipo:** Evento

Evento gerado quando o Cliente recusa o orçamento. Dispara a política de finalização da OS sem execução dos serviços.

> 📌 **Observação:** Fluxo alternativo — representado com seta tracejada no diagrama.

---

### Finaliza OS (por não aprovação)
**Tipo:** Política

Quando a OS não é aprovada, o sistema encerra automaticamente a OS sem execução dos serviços.

> ⚠️ **Atenção:** Este termo aparece também no contexto de Pagamento com significado diferente — lá representa o encerramento após pagamento bem-sucedido.

---

### Sistema encerrou a OS
**Tipo:** Evento

Evento que confirma o encerramento da OS por não aprovação do cliente. Marca o fim do ciclo de vida dessa OS.

---

### Ordem de Serviço
**Tipo:** Agregado

Agregado central do domínio. Gerencia todo o ciclo de vida do atendimento: da criação ao encerramento. Protege as transições de status e as invariantes do processo.

**Estados possíveis:** Recebida → Em diagnóstico → Aguardando aprovação → Em execução → Finalizada → Entregue / Cancelada

---

## Contexto: Estoque

> Responsável pelo controle de disponibilidade de peças e insumos. Garante que os materiais necessários estejam disponíveis antes do início da execução dos serviços.

---

### Notifica peças e insumos necessários
**Tipo:** Comando

Ação do Atendente para comunicar ao sistema quais peças e insumos são necessários para a execução dos serviços aprovados.

---

### Peças e insumos necessários notificados
**Tipo:** Evento

Evento que confirma o registro da necessidade de peças e insumos. Dispara a verificação de estoque disponível.

---

### Lista de peças e insumos necessários
**Tipo:** Modelo de Leitura

Relação das peças e insumos identificados no diagnóstico que precisam ser providenciados para a execução dos serviços.

---

### Solicita peças e insumos necessários
**Tipo:** Comando

Comando para acionar o fornecedor externo quando não há estoque suficiente das peças e insumos necessários.

> 📌 **Observação:** Se houver estoque suficiente de todas as peças e insumos necessários, o fluxo avança sem acionar o fornecedor.

---

### Fornecedor (compra de peças)
**Tipo:** Sistema Externo

Fornecedor externo que realiza a compra e entrega das peças e insumos necessários para a execução dos serviços. Está fora do boundary do domínio — o sistema apenas registra o pedido e a entrada dos materiais.

---

### Realiza a compra de peças e insumos necessários
**Tipo:** Evento (Externo)

Ação executada pelo sistema externo (fornecedor) de adquirir e entregar as peças e insumos solicitados.

---

### Serviço ficou disponível para ser executado
**Tipo:** Evento

Evento que confirma que todas as peças e insumos necessários estão disponíveis. Habilita o início da execução dos serviços pelo Mecânico. É um **evento pivotal** — separa a fase de abastecimento da fase de execução técnica.

---

### Estoque
**Tipo:** Agregado

Agregado responsável pelo controle de disponibilidade de peças e insumos. Verifica disponibilidade, registra entradas por fornecedor e saídas por uso nos serviços. Protege a invariante de que a quantidade em estoque nunca pode ser negativa.

---

## Contexto: Serviço

> Responsável pelo controle da execução técnica dos serviços pelo mecânico. Garante que todos os serviços aprovados sejam realizados antes da conclusão da OS.

---

### Lista de serviços a fazer
**Tipo:** Modelo de Leitura

Relação dos serviços aprovados pelo cliente que o Mecânico deve executar no veículo. Base de trabalho do Mecânico durante a execução.

---

### Realiza serviço
**Tipo:** Comando

Ação do Mecânico de executar fisicamente os serviços aprovados no veículo.

> 📌 **Observação:** O mecânico deverá realizar todos os serviços aprovados até que a OS seja concluída.

---

### Mecânico realizou o serviço no veículo
**Tipo:** Evento

Evento que confirma a execução de um serviço pelo Mecânico. Pode haver múltiplas ocorrências até todos os serviços serem concluídos.

---

### Atualiza status da OS
**Tipo:** Comando

Ação do Atendente para registrar no sistema o progresso da execução dos serviços na OS.

---

### OS foi concluída
**Tipo:** Evento

Evento que confirma a conclusão de todos os serviços aprovados. É um **evento pivotal** — separa a fase de execução da fase de pagamento. Dispara a notificação ao cliente e o processo de cobrança.

---

### OS não concluída
**Tipo:** Evento

Evento gerado quando nem todos os serviços foram concluídos. O ciclo de execução se repete até que todos os serviços sejam finalizados.

> 📌 **Observação:** Fluxo alternativo — representado com seta tracejada. O Mecânico retorna ao comando "Realiza serviço".

---

### Notificar serviços finalizados ao cliente
**Tipo:** Política

Quando a OS é concluída, o sistema notifica automaticamente o cliente de que os serviços foram finalizados e o veículo está pronto.

---

### Serviço
**Tipo:** Agregado

Agregado responsável pelo controle da execução técnica dos serviços. Registra o progresso do Mecânico e protege a invariante de que todos os serviços aprovados devem ser realizados antes da conclusão da OS.

---

## Contexto: Pagamento

> Responsável pelo encerramento financeiro da OS. Gerencia a cobrança, o pagamento, a emissão de nota fiscal e o encerramento formal do atendimento.

---

### Notifica serviços finalizados
**Tipo:** Comando

Comando que inicia o contexto de pagamento, informando que os serviços foram concluídos e o processo de cobrança pode ser iniciado.

---

### Sistema disparou e-mail de conclusão da OS
**Tipo:** Evento

Evento que confirma o envio do e-mail ao cliente comunicando a conclusão dos serviços e iniciando o processo de cobrança.

---

### Realizar cobrança pelo serviço
**Tipo:** Política

Quando o e-mail de conclusão é disparado, o sistema inicia automaticamente o processo de cobrança pelo valor total dos serviços realizados.

---

### Realiza cobrança
**Tipo:** Comando

Comando para processar a cobrança do valor total da OS ao cliente.

---

### Sistemas Externos — cobrança
**Tipo:** Sistema Externo

Sistema externo responsável pela cobrança ao cliente (gateway de pagamento ou sistema financeiro). Processa o pagamento fora do boundary do domínio.

---

### Cobra o cliente
**Tipo:** Evento (Externo)

Ação executada pelo sistema externo de processar a cobrança junto ao cliente.

---

### Pagamento realizado
**Tipo:** Evento

Evento que confirma a quitação do valor total da OS pelo cliente. É um **evento pivotal** — separa a fase financeira da fase de encerramento. Dispara a emissão da nota fiscal.

---

### Emitir nota fiscal do serviço
**Tipo:** Política

Quando o pagamento é realizado, o sistema emite automaticamente a nota fiscal dos serviços prestados.

---

### Emite nota fiscal do serviço
**Tipo:** Comando

Comando para geração e emissão da nota fiscal referente aos serviços realizados e pagos.

---

### Nota fiscal emitida
**Tipo:** Evento

Evento que confirma a emissão da nota fiscal. Habilita o encerramento formal da OS.

---

### Finaliza OS (após pagamento)
**Tipo:** Comando

Comando final para encerrar a OS após pagamento e emissão da nota fiscal. Registra a entrega do veículo ao cliente.

> ⚠️ **Atenção:** Este termo aparece também no contexto de Ordem de Serviço com significado diferente — lá representa o encerramento por não aprovação do orçamento.

---

### Sistema encerrou a OS
**Tipo:** Evento

Evento que confirma o encerramento completo da OS após pagamento. Marca a entrega do veículo e o fim do ciclo de atendimento.

---

### Pagamento
**Tipo:** Agregado

Agregado responsável pelo controle financeiro do encerramento da OS. Gerencia a cobrança, o pagamento, a emissão de nota fiscal e o encerramento formal da OS.

---

## Eventos Pivotais

Eventos que marcam mudanças de fase significativas no domínio, separando contextos distintos na linha do tempo:

| Evento Pivotal | Separa |
|---|---|
| **Diagnóstico realizado** | Fase de avaliação técnica → Fase comercial |
| **OS aprovada / OS não aprovada** | Fase comercial → Execução ou Cancelamento |
| **Serviço ficou disponível para ser executado** | Fase de abastecimento → Fase de execução técnica |
| **OS foi concluída** | Fase de execução → Fase de pagamento |
| **Pagamento realizado** | Fase financeira → Fase de encerramento |

---

## Termos Ambíguos — Atenção

Termos que aparecem em mais de um contexto com significados distintos:

| Termo | Contexto | Significado |
|---|---|---|
| **Finaliza OS** | Ordem de Serviço | Encerramento por não aprovação do cliente |
| **Finaliza OS** | Pagamento | Encerramento após pagamento e emissão de nota fiscal |
| **Sistema encerrou a OS** | Ordem de Serviço | OS cancelada por não aprovação |
| **Sistema encerrou a OS** | Pagamento | OS entregue após ciclo completo |
| **Cliente** | Cadastro | Ator que fornece dados para registro |
| **Cliente** | Ordem de Serviço | Ator que avalia e aprova/desaprova o orçamento |
| **Cliente** | Pagamento | Ator que realiza o pagamento dos serviços |

---

*Documento gerado com base no Event Storming do sistema de gestão da oficina mecânica — Tech Challenge Fase 1 / SOAT.*
