# projeto-dac-ifpb

## Pré-requisitos

- **JDK 21**
- **Git**
- **Vagrant** e um provider compatível (ex.: VirtualBox ou libvirt)
- Conta **sandbox** no **Asaas**
- Um **provedor de e-mail SMTP válido** (ex.: Mailtrap para testes, SendGrid/SES/Gmail para produção)

---

## Estrutura do Repositório

- `charger-manager/` — módulo Spring Boot responsável pela API REST e orquestração.
- `charger-proxy/` — módulo Spring Boot responsável pela integração SOAP e comunicação com o Asaas.
- `infra/` — arquivos de infraestrutura.
  - `infra/stacks/` — arquivos de stack usados para subir serviços no Docker Swarm.
  - `infra/scripts/` — scripts bash de suporte para automação.
  - `infra/envs/` — variáveis de ambiente dos serviços Swarm.
  - `infra/migrations/` — migrations do banco de dados.

- `vagrant-env/` — configuração do Vagrant para criação das VMs e provisionamento do cluster Docker Swarm.
- `start.sh` — script que prepara o ambiente, sobe as VMs, gera imagens via Jib e provisiona a stack (usa **VirtualBox** como provider padrão).

---

## Start de forma automática

### Observações

- Defina os arquivos de variaveis de ambiente `infra/envs/db.env`, `infra/envs/proxy.env`, `infra/envs/manager.env`.
- Cadastre um **webhook** no Asaas utilizando a URL pública gerada pelo serviço
  `charger-stack_cloudflare-tunnel` + `/api/webhook/asaas`.
- Define o `Token de autenticação` do webhook com o definido na variavel de
  ambiente `ASAAS_AUTH_TOKEN` no arquivo `infra/envs/proxy.env`
- Utilize uma **chave de API válida** na variável de ambiente `ASAAS_API_KEY`, definida em `infra/envs/proxy.env`.
- Configure corretamente as variáveis de e-mail no arquivo `/infra/envs/manager.env`:
  - `MAIL_HOST`
  - `MAIL_USERNAME`
  - `MAIL_PASSWORD`
  - `MAIL_PORT`

### Descrição

O script `start.sh` executa todo o fluxo de provisionamento automaticamente:

- copia `vms.ex.json` para `vms.json` dentro de `vagrant-env`;
- copia os arquivos de `infra/stacks/`, `infra/migrations/` e `infra/envs/` para `vagrant-env/shared/`;
- executa `vagrant up` para criar as VMs configuradas;
- cria a rede overlay `charger-network`;
- provisiona o serviço de banco de dados (`postgres`);
- aplica as migrations do banco de dados;
- provisiona um registro Docker local para armazenamento de imagens;
- builda o módulo `charger-proxy` utilizando o plugin **Jib** e publica a imagem no registro local;
- provisiona o serviço `charger-proxy`;
- provisiona o serviço **Cloudflare Tunnel** para exposição externa.

```bash
./start.sh
```

---

## Start de forma manual

### 1. Subir as VMs

```bash
cd vagrant-env
cp vms.ex.json vms.json   # ajuste conforme necessário
cp -r ../infra/stacks shared/
cp -r ../infra/migrations shared/
cp -r ../infra/envs shared/
vagrant up --provider=virtualbox # altere o provider se necessário
cd ..
```

### 2. Criar a rede overlay

```bash
export VAGRANT_DIR="$(pwd)/vagrant-env"
vagrant ssh manager1 -c 'docker network create --driver overlay --attachable charger-network'
```

### 3. Subir o banco de dados

```bash
cd $VAGRANT_DIR
vagrant ssh manager1 -c "docker stack deploy -c /shared/stacks/db.yml charger-stack"
cd ..

./infra/scripts/wait_for_service.sh $VAGRANT_DIR manager1 charger-stack_postgres 10 5
export DB_IP=$(./infra/scripts/service_ip.sh $VAGRANT_DIR manager1 charger-stack_postgres)
```

### 4. Aplicar as migrations

```bash
cd $VAGRANT_DIR
vagrant ssh manager1 -c "docker stack deploy -c /shared/stacks/db-migrate.yml charger-stack"
cd ..
```

### 5. Provisionar o registro Docker local

```bash
cd $VAGRANT_DIR
vagrant ssh manager1 -c "docker service create --name localregistry --publish published=5000,target=5000 registry:2"
cd ..

./infra/scripts/wait_for_service.sh $VAGRANT_DIR manager1 localregistry 10 5
export REGISTRY_IP=$(./infra/scripts/service_ip.sh $VAGRANT_DIR manager1 localregistry)
```

### 6. Build e deploy do `charger-proxy`

```bash
cd charger-proxy
./mvnw clean compile jib:build -Djib.to.image="$REGISTRY_IP:5000/charger-proxy:latest"

cd $VAGRANT_DIR
vagrant ssh manager1 -c "env ASAAS_API_KEY=${ASAAS_API_KEY} docker stack deploy -c /shared/stacks/charger-proxy.yml charger-stack"
cd ..

./infra/scripts/wait_for_service.sh $VAGRANT_DIR manager1 charger-stack_charger-proxy 10 5
export PROXY_IP=$(./infra/scripts/service_ip.sh $VAGRANT_DIR manager1 charger-stack_charger-proxy)
```

### 7. Provisionar o Cloudflare Tunnel

```bash
cd $VAGRANT_DIR
vagrant ssh manager1 -c "docker stack deploy -c /shared/stacks/cloudflare-tunnel.yml charger-stack"
cd ..
```

### 8. Build e deploy do `charger-manager`

```bash
cd charger-manager
./mvnw clean compile jib:build -Djib.to.image="$REGISTRY_IP:5000/charger-manager:latest"

cd $VAGRANT_DIR
vagrant ssh manager1 -c "docker stack deploy -c /shared/stacks/charger-manager.yml charger-stack"
cd ..

./infra/scripts/wait_for_service.sh $VAGRANT_DIR manager1 charger-stack_charger-manager 10 5
```

### Observações

- O arquivo `vms.json` deve conter **ao menos uma VM como manager**.
- O endereço do registry (`192.168.56.32:5000`) é apenas um exemplo.
- Aguarde o banco de dados inicializar completamente antes de aplicar migrations.
- Para acessar uma VM via SSH:

```bash
cd vagrant-env
vagrant ssh <nome-da-vm>
```

---

## Uso

- O **charger-proxy** disponibiliza um serviço **SOAP**, consome um banco de
  dados via JDBC e integra com a API do **Asaas** para webhooks de pagamento.

- O **charger-manager** consome esse serviço SOAP e expõe endpoints REST:

### Health Check

- `GET /api/health` - Verifica o status do serviço

### Clientes

- `POST /api/v1/customers` - Criar novo cliente
  ```json
  {
    "name": "Nome do Cliente",
    "cpfCnpj": "123.456.789-09",
    "email": "cliente@email.com"
  }
  ```
- `GET /api/v1/customers` - Listar todos os clientes
- `GET /api/v1/customers/{id}` - Buscar cliente por ID

### Cobranças

- `POST /api/v1/charges` - Criar nova cobrança
  ```json
  {
    "customerId": "uuid-do-cliente",
    "billingType": "PIX",
    "value": 100.5,
    "dueDate": "2025-12-31",
    "description": "Descrição da cobrança",
    "installmentCount": 1
  }
  ```
- `GET /api/v1/charges` - Listar todas as cobranças
- `GET /api/v1/charges/{id}` - Buscar cobrança por ID
- `GET /api/v1/charges/customer/{customerId}` - Listar cobranças de um cliente
- `DELETE /api/v1/charges/{id}` - Cancelar cobrança

**Tipos de cobrança:** `PIX`, `BOLETO`, `CREDIT_CARD`, `UNDEFINED`

> Substitua `192.168.56.32:8080` pelo host e porta onde o `charger-manager` estiver em execução.
