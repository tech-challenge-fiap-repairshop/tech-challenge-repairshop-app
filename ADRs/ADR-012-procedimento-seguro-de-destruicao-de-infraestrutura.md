# ADR-012: Procedimento Controlado de Destruição de Infraestrutura em Nuvem com Safety Gate

**Data:** 2026-09-01  
**Status:** Aceito  
**Autor:** Grupo CAO (POSTECH 15SOAT)  

---

## Contexto

Em contextos acadêmicos, de laboratório (AWS Academy) e ambientes de teste e validação contínua de infraestrutura, os recursos provisionados na nuvem pública (Clusters EKS, instâncias RDS PostgreSQL, NAT Gateways e Load Balancers) geram cobranças por hora de utilização e possuem limites rigorosos de cotas (*quotas*) e créditos financeiros.

No entanto, a remoção da infraestrutura em nuvem apresenta dois grandes riscos e desafios técnicos:
1. **Risco de Destruição Acidental:** A execução não intencional de comandos de exclusão (`terraform destroy`) em ambientes compartilhados ou produtivos resultaria em perda catastrófica de dados e indisponibilidade de serviços.
2. **Dependências de Rede e Recursos Presos na VPC (*Dependency Lock*):** A execução direta de `terraform destroy` na VPC frequentemente falha com erros de exclusão de subnets e gateways. Isso ocorre porque os serviços do Kubernetes (`LoadBalancer` ou Pods com interfaces ENI anexadas) mantêm conexões e IPs ativos na AWS que não são gerenciados diretamente pelo Terraform da VPC.
3. **Ordem Inversa Obrigatória de Destruição:** Um banco RDS ou Cluster EKS não pode ser destruído após a VPC ter sido removida, e a VPC não pode ser destruída enquanto recursos estiverem vinculados a suas sub-redes.

---

## Decisão

Implementar um **Workflow Manual Automatizado e Parametrizado no GitHub Actions (`destroy.yml`)** em todos os repositórios de infraestrutura, complementado por **Scripts de Orquestração Unificada (`destroy_all_infra.ps1` e `destroy_all_infra.sh`)**, incorporando **Safety Gate obrigatório**, **Desativação Limpa do Kubernetes** e **Pausa Controlada para Desalocação de ENIs/ELBs**.

### Detalhes Técnicos e Fluxo de Execução:

1. **Safety Gate Mandatório (Confirmação Literal):**
   - O workflow de destruição é acionado exclusivamente via **gatilho manual (`workflow_dispatch`)**.
   - O pipeline exige como parâmetro obrigatório a digitação literal da palavra **`DESTRUIR`** (em letras maiúsculas).
   - Qualquer outro valor inserido (inclusive o valor padrão `NAO`) interrompe imediatamente a execução do job com mensagem de segurança, sem interagir com nenhum recurso na nuvem.

2. **Desativação Limpa do Kubernetes (`Kubectl Clean`):**
   - Antes de qualquer chamada ao Terraform, o workflow se conecta ao cluster EKS e executa a exclusão de todos os manifestos da aplicação e serviços:
     ```bash
     kubectl delete -f k8s/ --ignore-not-found=true
     ```
   - Isso aciona a solicitação de desprovisionamento dos Load Balancers da AWS criados dinamicamente pelos serviços do Kubernetes.

3. **Pausa Programada de 60 Segundos (`Sleep 60s`):**
   - O pipeline executa uma pausa obrigatória de 60 segundos para conceder tempo hábil à AWS para desmontar e desalocar as Interfaces de Rede Elásticas (**ENIs**) e os **Elastic Load Balancers (ELBs)** associados às sub-redes da VPC.

4. **Ordem Inversa de Destruição (IaC Terraform):**
   - A destruição de infraestrutura via Terraform deve seguir estritamente a ordem inversa da criação de dependências:
     1. `tech-challenge-repairshop-infra-apigateway` (remove rotas e integrações do API Gateway).
     2. `tech-challenge-repairshop-lambda-auth` (remove a função serverless e suas permissões).
     3. `tech-challenge-repairshop-app` / Manifestos do EKS (desinstala workloads e serviços).
     4. `tech-challenge-repairshop-infra-eks` (destrói Node Groups e plano de controle do EKS).
     5. `tech-challenge-repairshop-infra-db-rds` (destrói a instância de banco de dados PostgreSQL RDS).
     6. `tech-challenge-repairshop-infra-network` (destrói NAT Gateways, EIPs, Subnets, Route Tables e VPC).
   - Cada etapa roda com o comando `terraform destroy -var-file="environments/${ENV}.tfvars" -auto-approve`.

5. **Avisos e Transparência no README:**
   - Inclusão explícita de avisos no README principal esclarecendo que o workflow `destroy.yml` foi disponibilizado estritamente para fins acadêmicos e de laboratório, não sendo recomendado para repositórios de produção corporativa real.

---

## Consequências

### Positivas
- **Prevenção Total de Gastos Ociosos:** Possibilidade de desativar 100% da infraestrutura em nuvem ao final de testes ou apresentações em poucos minutos.
- **Eliminação de Falhas de Destruição (*Zero Orphaned Resources*):** O *cleanup* prévio do Kubernetes e a pausa de desalocação garantem que a VPC seja destruída sem erros de dependências de rede pendentes.
- **Proteção Contra Erros Humanos:** O Safety Gate impede acionamentos acidentais por clique indevido.
- **Reprodutibilidade:** Scripts em PowerShell e Bash garantem que qualquer membro do time execute o tear-down com um único comando.

### Negativas / Trade-offs
- **Tempo de Destruição:** A exclusão completa e ordenada de um ambiente com EKS e RDS leva aproximadamente 15 a 20 minutos devido ao tempo de desalocação dos nós e instâncias na AWS.
- **Perda de Dados Efêmeros:** A destruição do RDS elimina o banco de dados caso backups finais não sejam solicitados (comportamento desejado para laboratórios acadêmicos).

### Riscos e Mitigações
- **Risco:** Execução acidental da esteira de destruição apontando para o ambiente de produção (`prd`).
- **Mitigação:** Exigência de permissões elevadas no GitHub, parametrização explícita de ambiente e digitação da palavra-chave de confirmação.
