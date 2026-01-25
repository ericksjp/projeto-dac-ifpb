# projeto-dac-ifpb

## Pré-requisitos

- `JDK 21`
- `git`
- `Vagrant` e um provider compatível (ex.: VirtualBox ou libvirt) para o ambiente de VMs.

## Estrutura do Repositório

- `charger-manager/` — módulo Spring Boot.
- `charger-proxy/` — módulo Spring Boot.
- `infra/` - pasta com arquivos de infraestrutura.
  - `infra/stacks/` - arquivos de stack usados para subir serviços no Docker Swarm.
  - `infra/scripts/` - scripts bash de suporte que ajudam na automação.
  - `infra/migrations/` - migrations de banco de dados.
- `vagrant-env/` — configuração do Vagrant para criar VMs e provisionar um
  cluster (inclui `Vagrantfile`, scripts e arquivos de configuração).
- `start.sh` — script que prepara `vms.json`, sobe as VMs, gera imagens via
  `jib` e provisiona a stack. Usa `virtualbox` como provider. 

## Start de forma automatica

O `start.sh` fornece um fluxo automático que:

- copia `vms.ex.json` para `vms.json` dentro de `vagrant-env`;
- copia arquivos de `infra/stacks/` e `infra/migrations/` para `vagrant-env/shared/`;
- executa `vagrant up` para criar as VMs configuradas;
- cria uma rede overlay `charger-network`;
- provisiona o serviço de banco de dados via `vagrant-env/shared/stacks/db.yml`;
- aplica as migrations do banco via `vagrant-env/shared/stacks/db-migrate.yml`;
- provisiona um registro Docker local para armazenar imagens;
- builda o módulo `charger-proxy` para uma imagem Docker usando o plugin Jib e publica no registro local;
- provisiona o serviço `charger-proxy` usando `vagrant-env/shared/stacks/charger-proxy.yml`;
- provisiona o serviço Cloudflare Tunnel para exposição externa via `vagrant-env/shared/stacks/cloudflare-tunnel.yml`;

```bash
./start.sh
```

## Start de forma manual

1. Suba as VMs:

```bash
cd vagrant-env
cp vms.ex.json vms.json   # ajuste se preferir
cp -r ../infra/stacks shared/
cp -r ../infra/migrations shared/
vagrant up --provider=virtualbox # mude o provider conforme necessario
cd ..
```

2. Cria a rede overlay e define variáveis de ambiente necessárias:

```bash
export VAGRANT_DIR="$(pwd)/vagrant-env"
vagrant ssh manager1 -c 'docker network create --driver overlay --attachable charger-network'
```

3. Subir o serviço de banco de dados no cluster Docker Swarm, utilizando
    a stack `charger-stack` e definir variavel de ambiente `DB_IP` que indica
    a VM em que o bd está rodando:

```bash
cd $VAGRANT_DIR
vagrant ssh -c "docker stack deploy -c /shared/stacks/db.yml charger-stack" manager1;
cd ..

./infra/scripts/wait_for_service.sh $VAGRANT_DIR manager1 charger-stack_postgres 10 5;
export DB_IP=$(./infra/scripts/service_ip.sh $VAGRANT_DIR manager1 charger-stack_postgres);
```

4. Aplicar as migrations do banco de dados:

```bash
cd $VAGRANT_DIR
vagrant ssh -c "docker stack deploy -c /shared/stacks/db-migrate.yml charger-stack" manager1;
cd ..
```

5. Provisionar o registro Docker local:

```bash
cd $VAGRANT_DIR
vagrant ssh -c "docker service create --name localregistry --publish published=5000,target=5000 registry:2" manager1
cd ..

./infra/scripts/wait_for_service.sh $VAGRANT_DIR manager1 localregistry 10 5;
export REGISTRY_IP=$(./infra/scripts/service_ip.sh $VAGRANT_DIR manager1 localregistry)
```

6. Compilar o módulo `charger-proxy` em uma imagem Docker, enviar a imagem para o registro do cluster
    Swarm e subir o serviço utilizando o arquivo de stack
    `/vagrant-env/shared/stacks/charger-proxy.yml`. Defina `ASAAS_API_KEY` se necessário.

```bash
cd charger-proxy
./mvnw clean compile jib:build -Djib.to.image="$REGISTRY_IP:5000/charger-proxy:latest"

cd $VAGRANT_DIR
vagrant ssh -c "env ASAAS_API_KEY=${ASAAS_API_KEY} docker stack deploy -c /shared/stacks/charger-proxy.yml charger-stack" manager1;
cd ..

./infra/scripts/wait_for_service.sh $VAGRANT_DIR manager1 charger-stack_charger-proxy 10 5;
export PROXY_IP=$(./infra/scripts/service_ip.sh $VAGRANT_DIR manager1 charger-stack_charger-proxy)
```

7. Provisionar o serviço Cloudflare Tunnel para exposição externa:

```bash
cd $VAGRANT_DIR
vagrant ssh -c "docker stack deploy -c /shared/stacks/cloudflare-tunnel.yml charger-stack" manager1;
cd ..
```

8. (Opcional) Compilar módulo `charger-manager` em uma imagem Docker, enviar a imagem para
    o registro do cluster Swarm e subir o serviço utilizando o arquivo de stack
    `/vagrant-env/shared/stacks/charger-manager.yml`.

```bash
cd charger-manager
./mvnw clean compile jib:build -Djib.to.image="$REGISTRY_IP:5000/charger-manager:latest"

cd $VAGRANT_DIR
vagrant ssh -c "docker stack deploy -c /shared/stacks/charger-manager.yml charger-stack" manager1;
cd ..

./infra/scripts/wait_for_service.sh $VAGRANT_DIR manager1 charger-stack_charger-manager 10 5;
```

#### Observações

- O arquivo `vms.json` tem que ter no mínimo 1 vm como manager;
- O endereço do registry (`192.168.56.32:5000`) é usado como exemplo e deve ser
  ajustado conforme sua rede.
- Você pode ajustar os arquivos `vagrant-env/vms.json`
  e `vagrant-env/shared/stacks/*.yml` de acordo com a sua preferência.
- O plugin Jib usado nos módulos permite empurrar imagens sem Docker local.
- Para acessar as maquinas via `SSH` faça: `cd vagrant-env`, `vagrant ssh <maquina>`
- Para aplicar as migrações é necessario esperar ate que o serviço de banco de
  dados esteja inicializado, oque pode demorar um pouco.

## Uso

- O **charger-proxy** disponibiliza um serviço SOAP, consome um banco de dados
  via JDBC e integra com a API do Asaas para webhooks de pagamento. Requer a variável
  de ambiente `ASAAS_API_KEY` para autenticação.
- O **charger-manager** consome esse serviço SOAP e expõe dois endpoints REST
  simples para testes:
    - `POST /api/v1/messages`
    - `GET /api/v1/messages/{id}`
- O serviço Cloudflare Tunnel expõe os serviços externamente através de um endereço
  HTTPS público, facilitando o acesso remoto e testes.

```bash
curl --json '{ "message": "hello" }' http://192.168.56.32:8080/api/v1/messages
curl http://192.168.56.32:8080/api/v1/messages/1
```

#### Observações

- Substitua `192.168.56.32:8080` pelo host/porta onde o `charger-manager` está
  rodando.
