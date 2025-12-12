#!/bin/bash

set -euo pipefail

VAGRANT_DIR="$(pwd)/vagrant-env"

# entrar no diretório do Vagrant e copiar o arquivo JSON
cd vagrant-env
cp -f vms.ex.json vms.json

# inicializar VMs
vagrant up --provider=virtualbox

# provisionar o manager com a stack do banco de dados
cp ../stack_files/* shared/stacks
vagrant ssh -c "docker stack deploy -c /shared/stacks/db.yml charger-stack" manager1
cd ..

# ip do bd
./bash/wait_for_service.sh $VAGRANT_DIR manager1 charger-stack_postgres 10 5
DB_IP=$(./bash/service_ip.sh $VAGRANT_DIR manager1 charger-stack_postgres)

# ip do register 
./bash/wait_for_service.sh $VAGRANT_DIR manager1 registry 10 5
REGISTRY=$(./bash/service_ip.sh $VAGRANT_DIR manager1 registry)

# charger-proxy
cd charger-proxy
# aplica migrations ao bd que esta rodando no manager
PORT=8080 DB_URL=jdbc:postgresql://$DB_IP:5432/chargerdb DB_USER=postgrau DB_PASSWORD=postgrau ./mvnw flyway:migrate
# manda build da imagem ao registry privado
./mvnw clean compile jib:build -Djib.to.image="$REGISTRY:5000/charger-proxy:latest"
# provisiona o manager com a stack do charger-proxy
cd ../vagrant-env
vagrant ssh -c "docker stack deploy -c /shared/stacks/charger-proxy.yml charger-stack" manager1

../bash/wait_for_service.sh $VAGRANT_DIR manager1 charger-stack_charger-proxy 10 5

PROXY_IP=$(../bash/service_ip.sh $VAGRANT_DIR manager1 charger-stack_charger-proxy)

echo "Charger-proxy is running on IP address: $PROXY_IP"

# charger-manager
cd ../charger-manager
# manda build da imagem ao registry privado
./mvnw clean compile jib:build -Djib.to.image="$REGISTRY:5000/charger-manager:latest" -DwsdlUrl="http://$PROXY_IP:8082/ws/hello.wsdl"
# provisiona o manager com a stack do charger-manager
cd ../vagrant-env
vagrant ssh -c "docker stack deploy -c /shared/stacks/charger-manager.yml charger-stack" manager1
cd ..

echo "its all good"
