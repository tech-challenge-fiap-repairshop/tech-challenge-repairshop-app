# Dicionário de Linguagem Ubíqua
## Sistema de Gestão — Oficina Mecânica

> Este documento define os principais termos e conceitos utilizados no projeto da Oficina Mecânica, sendo auto-explicativo e relacionando a linguagem do negócio (como os usuários chamam no dia a dia) com a implementação técnica (como as tabelas e campos foram definidos no banco de dados e no código).

---

### Ordem de Serviço (OS / Service Order)
Documento central que registra o atendimento de um veículo na oficina. Ela consolida os serviços e insumos/peças utilizados e passa por diversos status durante o seu ciclo de vida (Recebida, Em Diagnóstico, Aguardando Aprovação, Aprovada, Em Execução, Finalizada, Paga, Cancelada). É conhecida por OS, ou do inglês *Service Order* (e é assim que chamamos na tabela).
- **Tabela:** `tb_service_order`
- **Principais campos:** 
  - `id_tb_service_order`: Identificador único da OS.
  - `customer_id`: Cliente vinculado à OS.
  - `vehicle_id`: Veículo vinculado à OS.
  - `status`: Situação atual da OS.
  - `total_price`: Valor total do atendimento (serviços + peças).
- **Histórico (Service Order History):** Todas as transições de status da OS são auditadas e registradas na tabela `tb_service_order_history`.

### Cliente (Customer)
Pessoa (física) que solicita os serviços da oficina. Nenhuma OS pode ser criada sem que o cliente esteja previamente cadastrado no sistema.
- **Tabela:** `tb_customer`
- **Principais campos:** 
  - `id_tb_customer`: Identificador único do cliente.
  - `name`: Nome do cliente.
  - `document`: CPF.
  - `email` e `phone`: Dados de contato.

### Veículo (Vehicle)
Automóvel pertencente a um cliente, que será alvo do diagnóstico e dos serviços de manutenção.
- **Tabela:** `tb_vehicle`
- **Principais campos:** 
  - `id_tb_vehicle`: Identificador único do veículo.
  - `customer_id`: Referência ao proprietário (cliente).
  - `plate`: Placa do veículo.
  - `brand` e `model`: Marca e modelo.

### Usuário / Funcionário (User)
Representa os funcionários da oficina que operam o sistema. Pode assumir diferentes funções, como "Atendente" (que cria a OS e gerencia cadastros) ou "Mecânico" (que realiza diagnósticos e a execução técnica dos serviços).
- **Tabela:** `tb_user`
- **Principais campos:** 
  - `id_tb_user`: Identificador único.
  - `name`: Nome do funcionário.
  - `function`: Cargo ou função na oficina.
  - `email` e `password`: Credenciais de acesso.

### Insumo / Peça (Insume)
Materiais, peças ou insumos (como óleo, filtro, parafusos, peças de reposição) utilizados durante a execução dos serviços. O sistema mantém um registro para fins de precificação e controle de estoque.
- **Tabela:** `tb_insume`
- **Principais campos:** 
  - `id_tb_insume`: Identificador único do insumo.
  - `name`: Nome da peça ou material.
  - `quantity`: Quantidade disponível em estoque.
  - `price`: Preço de venda para o cliente.

### Execução de Serviço (Execution)
Representa cada serviço individual (mão de obra e diagnósticos) que precisa ser realizado dentro de uma Ordem de Serviço (OS). Uma mesma OS pode ter várias "Execuções" vinculadas a ela.
- **Tabela:** `tb_execution`
- **Principais campos:** 
  - `id_tb_execution`: Identificador único do serviço executado.
  - `service_order`: Referência à OS pai.
  - `basic_description`: Descrição do serviço (ex: Troca de óleo).
  - `price`: Valor cobrado pela mão de obra.
  - `status`: Situação do serviço (Iniciado, Pendente, Finalizado).
- **Histórico (Execution History):** Mudanças de status de uma execução específica são registradas na tabela `tb_execution_history`.

### Insumos da Execução (Execution Insume)
Tabela associativa que relaciona quais peças/insumos foram utilizados em uma Execução de Serviço específica, bem como a quantidade consumida para compor o valor final.
- **Tabela:** `tb_execution_insume`
- **Principais campos:** 
  - `id_tb_execution`: Referência ao serviço executado.
  - `id_tb_insume`: Referência à peça consumida.
  - `quantity_used`: Quantidade utilizada na execução.

### Nota Fiscal (Invoice)
Documento financeiro e fiscal gerado após a conclusão e o pagamento total da Ordem de Serviço.
- **Tabela:** `tb_invoice`
- **Principais campos:** 
  - `id_tb_invoice`: Identificador único da fatura.
  - `service_order_id`: OS associada à nota.
  - `price`: Valor total cobrado.
  - `invoice_number`: Número de registro da nota fiscal.