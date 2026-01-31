#!/bin/bash

set -euo pipefail

VAGRANT_DIR="$(pwd)/vagrant-env"
WAIT_FOR_SERVICE_SH="$(pwd)/infra/scripts/wait_for_service.sh"
SERVICE_IP_SH="$(pwd)/infra/scripts/service_ip.sh"

# caminhos dos arquivos de ambiente

DB_ENV="./infra/envs/db.env"
MANAGER_ENV="./infra/envs/manager.env"
PROXY_ENV="./infra/envs/proxy.env"

# verifica cada arquivo
if [ ! -f "$DB_ENV" ]; then
    echo "Error: db.env não encontrado em $DB_ENV"
    exit 1
elif [ ! -f "$MANAGER_ENV" ]; then
    echo "Error: manager.env não encontrado em $MANAGER_ENV"
    exit 1
elif [ ! -f "$PROXY_ENV" ]; then
    echo "Error: proxy.env não encontrado em $PROXY_ENV"
    exit 1
else
    echo "todos os arquivos de ambiente encontrados!"
fi    

#=== INICIALIZAR VMs ===#he

# entrar no diretório do Vagrant e copiar o arquivo JSON
cd vagrant-env
cp -f vms.ex.json vms.json

cp -r ../infra/stacks shared/
cp -r ../infra/migrations shared/
cp ../infra/envs/*.env shared/envs/

# inicializar VMs
vagrant up --provider=virtualbox

# criar network
vagrant ssh manager1 -c 'docker network inspect charger-network >/dev/null 2>&1 || docker network create --driver overlay --attachable charger-network'

#=== PROVISIONAR BANCO DE DADOS ===#

# provisionar o manager com a stack do banco de dados
vagrant ssh -c "docker stack deploy -c /shared/stacks/db.yml charger-stack" manager1

# esperar o banco de dados ficar disponível
$WAIT_FOR_SERVICE_SH "$VAGRANT_DIR" manager1 charger-stack_postgres 10 5 3 \
'docker service ps --filter "desired-state=running" --format "{{.CurrentState}}" charger-stack_postgres 2>/dev/null | grep "Running"'
# pegar o ip do banco de dados
DB_IP=$($SERVICE_IP_SH $VAGRANT_DIR manager1 charger-stack_postgres)

# aplicar migrations no banco de dados
vagrant ssh -c "docker stack deploy -c /shared/stacks/db-migrate.yml charger-stack" manager1
# esperar as migrations terminarem
$WAIT_FOR_SERVICE_SH $VAGRANT_DIR manager1 charger-stack_db-migrate 10 3 1 \
"docker service ps --filter 'desired-state=shutdown' --format '{{.CurrentState}}' charger-stack_db-migrate 2>/dev/null | grep -q 'Complete'"

#=== PROVISIONAR REGISTER LOCAL ===#

# criar registry local no manager1, se não existir
vagrant ssh -c "if ! docker service ls --format '{{.Name}}' | grep -q '^localregistry$'; then docker service create --name localregistry --publish published=5000,target=5000 --constraint='node.hostname==manager1' registry:2; fi" manager1
# esperar o registry local ficar disponível
$WAIT_FOR_SERVICE_SH $VAGRANT_DIR manager1 localregistry 10 5 3 "curl -f http://127.0.0.1:5000/v2/"
# pegar o ip do registry local
REGISTRY=$($SERVICE_IP_SH $VAGRANT_DIR manager1 localregistry)

echo "Private Docker Registry is running on IP address: $REGISTRY"

#=== PROVISIONAR CHARGER PROXY ===#

# charger-proxy
cd ../charger-proxy
# manda build da imagem ao registry privado
./mvnw clean compile jib:build -Djib.to.image="$REGISTRY:5000/charger-proxy:latest"
# provisiona o manager com a stack do charger-proxy
cd ../vagrant-env
vagrant ssh -c "docker stack deploy -c /shared/stacks/charger-proxy.yml charger-stack" manager1

# espera o charger-proxy ficar disponível
$WAIT_FOR_SERVICE_SH $VAGRANT_DIR manager1 charger-stack_charger-proxy 10 5 3 \
"curl -fs http://127.0.0.1:8082/api/health | grep status...UP"
# pega o ip do charger-proxy
PROXY_IP=$($SERVICE_IP_SH $VAGRANT_DIR manager1 charger-stack_charger-proxy)

echo "Charger-proxy is running on IP address: $PROXY_IP"

#=== PROVISIONAR CLOUDFLARE TUNNEL ===#

# sobe serviço do cloudfaret tunnel
vagrant ssh -c "docker stack deploy -c /shared/stacks/cloudflare-tunnel.yml charger-stack" manager1
# espera o cloudflare tunnel ficar disponível
$WAIT_FOR_SERVICE_SH $VAGRANT_DIR manager1 charger-stack_cloudflare-tunnel 10 5 3 \
"docker service ps --filter 'desired-state=running' --format '{{.CurrentState}}' charger-stack_cloudflare-tunnel 2>/dev/null | grep -q 'Running'"
# pega o endereço público do tunnel
PUBLIC_ADDRESS=$(vagrant ssh manager1 -c "docker service logs charger-stack_cloudflare-tunnel --raw" | grep -m 1 " |  https://" | awk '{print $4}')
echo "===== Cloudflare Tunnel Public Address: $PUBLIC_ADDRESS"

#=== PROVISIONAR CHARGER MANAGER ===#

# charger-manager
cd ../charger-manager
# manda build da imagem ao registry privado
./mvnw clean compile jib:build -Djib.to.image="$REGISTRY:5000/charger-manager:latest"
# provisiona o manager com a stack do charger-manager
cd ../vagrant-env
vagrant ssh -c "docker stack deploy -c /shared/stacks/charger-manager.yml charger-stack" manager1
$WAIT_FOR_SERVICE_SH $VAGRANT_DIR manager1 charger-stack_charger-manager 10 5 3 \
"curl -fs http://127.0.0.1:8080/api/health | grep status...UP"
cd ..

echo "its all good"
echo "===== Cloudflare Tunnel Public Address: $PUBLIC_ADDRESS"
