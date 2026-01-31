#!/bin/bash

set -euo pipefail

VAGRANT_DIR="$(pwd)/vagrant-env"
WAIT_FOR_SERVICE_SH="$(pwd)/infra/scripts/wait_for_service.sh"
SERVICE_IP_SH="$(pwd)/infra/scripts/service_ip.sh"

API_KEY="$$aact_hmlg_000MzkwODA2MWY2OGM3MWRlMDU2NWM3MzJlNzZmNGZhZGY6OjA2ZmFkM2YxLWU5NmMtNDEwMi04ODIyLTk1MmRjMDA4NDEzMjo6JGFhY2hfZmE3MjM1YzYtYzcyOS00N2YyLWFhYWYtODg4ZWQxYzRkMTdk"

#=== INICIALIZAR VMs ===#he

# entrar no diretório do Vagrant e copiar o arquivo JSON
cd vagrant-env
cp -f vms.ex.json vms.json

cp -r ../infra/stacks shared/
cp -r ../infra/migrations shared/

# inicializar VMs
vagrant up --provider=virtualbox

# criar network
vagrant ssh manager1 -c 'docker network inspect charger-network >/dev/null 2>&1 || docker network create --driver overlay --attachable charger-network'

#=== PROVISIONAR BANCO DE DADOS ===#

# provisionar o manager com a stack do banco de dados
vagrant ssh -c "docker stack deploy -c /shared/stacks/db.yml charger-stack" manager1

# esperar o banco de dados ficar disponível
$WAIT_FOR_SERVICE_SH "$VAGRANT_DIR" manager1 charger-stack_postgres 10 5 3 \
'docker service ps --filter "desired-state=running" --format "{{.CurrentState}}" charger-stack_postgres 2>/dev/null | grep -q "Running"'
# pegar o ip do banco de dados
DB_IP=$($SERVICE_IP_SH $VAGRANT_DIR manager1 charger-stack_postgres)

# aplicar migrations no banco de dados
vagrant ssh -c "docker stack deploy -c /shared/stacks/db-migrate.yml charger-stack" manager1
# esperar as migrations terminarem
$WAIT_FOR_SERVICE_SH $VAGRANT_DIR manager1 localregistry 10 3 1 \
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
vagrant ssh -c "env ASAAS_API_KEY='$API_KEY' docker stack deploy -c /shared/stacks/charger-proxy.yml charger-stack" manager1

# espera o charger-proxy ficar disponível
$WAIT_FOR_SERVICE_SH $VAGRANT_DIR manager1 charger-stack_charger-proxy 10 5 3 "curl -fs http://127.0.0.1:8082/api/webhook/health"
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
cd ..

echo "its all good"
echo "===== Cloudflare Tunnel Public Address: $PUBLIC_ADDRESS"
