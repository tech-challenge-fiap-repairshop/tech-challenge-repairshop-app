# RFC – Provisionamento de Infraestrutura como Código com Terraform e Segregação de Ambientes

## DATA
19/08/2026

## STATUS
Encerrada – Aprovada

> **Nota de Encerramento:** Proposta aprovada pelo time de DevOps e Cloud, formalizada através do [ADR-004](../ADRs/ADR-004-infraestrutura-como-codigo-terraform-e-ambientes.md).

## RESUMO
Proposta de adoção do HashiCorp Terraform como ferramenta única de Infraestrutura como Código (IaC) para o provisionamento e ciclo de vida dos recursos na AWS, utilizando armazenamento remoto de estado em Bucket S3 centralizado e isolamento estrito entre 3 ambientes (`dev`, `hml` e `prd`).

## PROBLEMA
No gerenciamento de ambientes em nuvem pública (AWS), a criação manual de recursos pela interface web (*ClickOps*) apresenta sérios problemas:
1. **Falta de Reprodutibilidade e Erro Humano:** A configuração manual de dezenas de recursos interdependentes (VPC, Subnets, Gateways, Clusters Kubernetes, instâncias RDS e rotas de API Gateway) é propensa a esquecimentos, falhas de digitação e divergências silenciosas entre ambientes (*configuration drift*).
2. **Inexistência de Histórico Versionado:** É impossível auditar quem criou determinado recurso, quais parâmetros foram utilizados ou reverter uma alteração defeituosa sem código versionado.
3. **Necessidade de 3 Ambientes Isolados:** O projeto possui o requisito inegociável de manter 3 ambientes (`dev`, `hml` e `prd`), onde o ambiente de desenvolvimento não pode compartilhar variáveis, bancos de dados, redes ou arquivos de estado com homologação ou produção.

## PROPOSTA TÉCNICA
Propõe-se a padronização do **HashiCorp Terraform (v1.8.5+)** em todos os repositórios de infraestrutura, com backend de **Remote State em Bucket S3 Centralizado (`fiap-repairshop2`)** e segregação declarativa de variáveis através de arquivos `.tfvars`:

```
              Bucket S3: fiap-repairshop2 (Versioning Ativado)
              +----------------------------------------------+
              | /network/{dev,hml,prd}.tfstate               |
              | /eks/{dev,hml,prd}.tfstate                   |
              | /rds/{dev,hml,prd}.tfstate                   |
              | /lambda-auth/{dev,hml,prd}.tfstate           |
              | /apigateway/{dev,hml,prd}.tfstate            |
              +----------------------------------------------+
                                     ^
                                     | Backend Config Dinâmico
            +------------------------+------------------------+
            |                        |                        |
     [ Ambiente dev ]         [ Ambiente hml ]         [ Ambiente prd ]
     - dev.tfvars             - hml.tfvars             - prd.tfvars
     - CIDR 10.0.0.0/16       - CIDR 10.1.0.0/16       - CIDR 10.2.0.0/16
     - Instâncias t3.micro    - Instâncias t3.small    - Instâncias t3.medium
```

### Detalhes Estruturais da Proposta:

1. **Separação por Chave Única no Remote State:**
   - Cada repositório e cada ambiente possui um caminho exclusivo dentro do bucket central:
     - `network/${ENV}.tfstate`, `eks/${ENV}.tfstate`, `rds/${ENV}.tfstate`, `lambda-auth/${ENV}.tfstate`, `apigateway/${ENV}.tfstate`.
   - Isso garante que a destruição ou aplicação do ambiente `dev` nunca altere ou bloqueie o estado de `hml` ou `prd`.
2. **Estrutura Declarativa de Variáveis (`environments/`):**
   - Todos os repositórios de infraestrutura mantêm arquivos `.tfvars` estritamente isolados:
     - `dev.tfvars`: Parâmetros enxutos e econômicos para testes rápidos e desenvolvimento ativo.
     - `hml.tfvars`: Dimensionamento intermediário para testes integrados de carga e homologação.
     - `prd.tfvars`: Alta disponibilidade, maior retenção de backups e políticas restritas de segurança.
3. **Execução Automatizada via CI/CD:**
   - Comandos padronizados acionados via GitHub Actions:
     ```bash
     terraform init -backend-config="bucket=fiap-repairshop2" -backend-config="key=<modulo>/${ENV}.tfstate" -backend-config="region=us-east-1"
     terraform plan -var-file="environments/${ENV}.tfvars"
     terraform apply -var-file="environments/${ENV}.tfvars" -auto-approve
     ```
4. **Proteção de Secrets:**
   - Nenhuma senha ou credencial sensível é gravada nos arquivos `.tfvars`. Valores como `db_password` são passados via variáveis de ambiente seguras (`TF_VAR_db_password`) nas pipelines.

## IMPACTO ESPERADO
- **Benefícios:**
  - Infraestrutura 100% determinística, auditável e reprodutível a qualquer momento com um único comando.
  - Isolamento total entre ambientes `dev`, `hml` e `prd`.
  - Histórico de versões do estado da infraestrutura preservado no S3.
  - Agilidade na criação de novos ambientes idênticos sob demanda.
- **Riscos e Mitigações:**
  - *Risco:* Corrupção ou exclusão acidental do arquivo `.tfstate` no S3.
  - *Mitigação:* Ativação do versionamento de objetos (*Object Versioning*) no bucket S3.
- **Custos e Operação:**
  - Custo irrisório de armazenamento de arquivos de estado no S3 ($0.02/mês).

## ALTERNATIVAS CONSIDERADAS
1. **AWS CloudFormation / AWS CDK:**
   - *Prós:* Ferramenta nativa da AWS.
   - *Contras:* Forte acoplamento ao ecossistema AWS (*vendor lock-in*), sintaxe mais verbosa e menor facilidade para orquestrar múltiplos módulos independentes se comparado à linguagem HCL do Terraform.
2. **Pulumi:**
   - *Prós:* Permite escrever infraestrutura em linguagens de programação gerais (TypeScript/Python).
   - *Contras:* Menor maturidade na comunidade de DevOps para infraestrutura base e necessidade de gerenciar o runtime de código dentro das esteiras de CI/CD.
3. **Armazenamento de Estado Local (`local backend`):**
   - *Prós:* Sem necessidade de bucket S3 inicial.
   - *Contras:* Totalmente inviável para times e pipelines CI/CD (o estado ficaria retido na máquina de quem executou ou seria perdido ao término do runner efêmero do GitHub Actions).

## PONTOS EM ABERTO
- [x] Nome do bucket S3 padronizado para o projeto (Alinhado: `fiap-repairshop2`).
- [x] Região padrão de provisionamento (Alinhado: `us-east-1`).
