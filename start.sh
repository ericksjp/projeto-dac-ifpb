#!/bin/bash

set -euxo pipefail

# entrar no diretório do Vagrant e copiar o arquivo JSON
cd vagrant-env
cp -f vms.ex.json vms.json

# inicializar VMs
vagrant up

# buildar e mandar as imagens para o repositorio local no manager1

cd ../charger-manager
./mvnw jib:build -Djib.to.image=192.168.56.32:5000/charger-manager:latest

cd ../charger-proxy
./mvnw jib:build -Djib.to.image=192.168.56.32:5000/charger-proxy:latest

# copiar o arquivo de stack para o diretório compartilhado
cd ..
cp stack.ex.yml vagrant-env/shared/stacks/stack.yml

# provisionar o manager1 com a stack que vai inicializar os serviços
cd vagrant-env
vagrant provision manager1 --provision-with start-stack

sleep 10

# aplica migrations ao bd que esta rodando no manager
cd ../charger-proxy
PORT=8080 DB_URL=jdbc:postgresql://192.168.56.32:5432/chargerdb DB_USER=postgrau DB_PASSWORD=postgrau ./mvnw flyway:migrate
