# ADR-004: Provisionamento de Infraestrutura como Código com Terraform e Segregação de Ambientes

**Data:** 2026-09-01  
**Status:** Aceito  
**Autor:** Grupo CAO (POSTECH 15SOAT)  

---

## Contexto

Para garantir a confiabilidade, auditabilidade e reprodutibilidade de toda a infraestrutura em nuvem (AWS), era fundamental adotar uma ferramenta padrão de **Infraestrutura como Código (IaC)**. A criação manual de recursos no console da AWS (*ClickOps*) é propensa a erros humanos, não é versionável, impede a automação via CI/CD e não permite isolamento rigoroso entre múltiplos estágios de entrega.

Além disso, o projeto possui o requisito inegociável de manter **3 ambientes estritamente isolados**:
- **`dev` (Desenvolvimento):** Ambiente para testes ativos e deploys frequentes de desenvolvimento.
- **`hml` (Homologação):** Ambiente réplica para validação de qualidade, testes integrados de carga e aceitação.
- **`prd` (Produção):** Ambiente final com configurações de alta disponibilidade e restrições máximas de acesso.

---

## Decisão

Adotar o **HashiCorp Terraform (v1.8.5+)** como a única ferramenta oficial de IaC para todo o ecossistema AWS, utilizando **Remote State em Bucket S3 Centralizado** com chaves isoladas e arquivos de variáveis (`.tfvars`) segregados por ambiente.

### Detalhes de Arquitetura e Implementação:

1. **Remote State Centralizado e Chaves Únicas:**
   - O arquivo de estado do Terraform (`.tfstate`) é armazenado remotamente em um Bucket S3 centralizado na AWS (`fiap-repairshop2`).
   - Para evitar concorrência ou sobrescrita acidental entre serviços e ambientes, cada repositório e cada ambiente utiliza uma chave/caminho exclusivo dentro do bucket:
     - `network/dev.tfstate`, `network/hml.tfstate`, `network/prd.tfstate`
     - `eks/dev.tfstate`, `eks/hml.tfstate`, `eks/prd.tfstate`
     - `rds/dev.tfstate`, `rds/hml.tfstate`, `rds/prd.tfstate`
     - `lambda-auth/dev.tfstate`, `lambda-auth/hml.tfstate`, `lambda-auth/prd.tfstate`
     - `apigateway/dev.tfstate`, `apigateway/hml.tfstate`, `apigateway/prd.tfstate`

2. **Isolamento de Variáveis por Ambiente:**
   - Cada repositório de infraestrutura possui uma pasta `environments/` contendo arquivos declarativos específicos:
     - `dev.tfvars`: Instâncias menores, CIDR blocks `10.0.0.0/16`, tags com ambiente `dev`.
     - `hml.tfvars`: Dimensionamento intermediário, CIDR blocks `10.1.0.0/16`, tags `hml`.
     - `prd.tfvars`: Dimensionamento para produção com alta disponibilidade, CIDR blocks `10.2.0.0/16`, tags `prd`.
   - As pipelines do GitHub Actions utilizam parâmetros de entrada dinâmicos (`environment`) para carregar o arquivo `.tfvars` e a chave de estado correspondente:
     ```bash
     terraform init -backend-config="key=<servico>/${ENV}.tfstate"
     terraform apply -var-file="environments/${ENV}.tfvars" -auto-approve
     ```

3. **Injeção Dinâmica de Credenciais e Secrets:**
   - Nenhuma credencial sensível (senhas de banco, tokens de API, chaves secretas AWS) é versionada em arquivos de texto plano nos repositórios.
   - Variáveis sensíveis são declaradas como `sensitive = true` no Terraform e injetadas em tempo de execução através de GitHub Actions Secrets (`TF_VAR_db_password`, etc.).

---

## Consequências

### Positivas
- **Determinismo e Idempotência:** A execução do Terraform garante que o estado real na nuvem corresponda exatamente ao código declarado, eliminando inconsistências (*configuration drift*).
- **Isolamento Completo entre Ambientes:** Destruir ou recriar o ambiente de `dev` não possui nenhum impacto sobre os arquivos de estado ou recursos de `hml` ou `prd`.
- **Rastreabilidade e Governança:** Toda modificação de infraestrutura é revisada por meio de planos declarativos (`terraform plan`) antes de sua aplicação em produção.
- **Automação Total:** Possibilidade de provisionar ou destruir um ambiente inteiro de ponta a ponta através de pipelines do GitHub Actions ou scripts unificados.

### Negativas / Trade-offs
- **Gerenciamento de Estado Remoto:** Necessidade de gerenciar a inicialização do bucket S3 antes do primeiro provisionamento de qualquer outro recurso.
- **Interdependência de Outputs:** Módulos subsequentes (ex: API Gateway precisando do endpoint do EKS) precisam utilizar `terraform_remote_state` data sources ou tags dinâmicas da AWS para descoberta de recursos.

### Riscos e Mitigações
- **Risco:** Corrupção ou perda acidental do arquivo de estado no Bucket S3.
- **Mitigação:** Ativação de versionamento de objetos no Bucket S3 (`versioning { enabled = true }`), permitindo restauração instantânea de versões anteriores do `.tfstate` em caso de necessidade.
