# projeto-dac-ifpb

## Pré-requisitos

- `JDK 21`
- `git`
- `Vagrant` e um provider compatível (ex.: VirtualBox ou libvirt) para o ambiente de VMs.

## Estrutura do Repositório

- `charger-manager/` — módulo Spring Boot.
- `charger-proxy/` — módulo Spring Boot.
- `bash/` - scripts bash de suporte que ajudam na automação.
- `stack_files/` - arquivos de stack usados para subir serviços no docker swarm
- `vagrant-env/` — configuração do Vagrant para criar VMs e provisionar um
  cluster (inclui `Vagrantfile`, scripts e arquivos de configuração).
- `start.sh` — script que prepara `vms.json`, sobe as VMs, gera imagens via
  `jib` e provisiona a stack. Usa `virtualbox` como provider. 

## Start de forma automatica

O `start.sh` fornece um fluxo automático que:

- copia `vms.ex.json` para `vms.json` dentro de `vagrant-env`;
- executa `vagrant up` para criar as VMs configuradas;
- copia os arquivos de stack em `stack_files/` para `vagrant-env/shared/stacks/`;
- sobe o serviço de banco de dados definido em
  `vagrant-env/shared/stacks/db.yml`;
- aplica as migrations do `charger-proxy/` ao banco de dados;
- builda os modulos spring para imagens Docker usando o plugin Jib e publica
  para o registry local configurado no swarm;
- sobe os serviços `charger-proxy` e `charger-manager` usando as imagens do
  registro local e os arquivos de stack em `vagrant-env/shared/stacks/`;

```bash
./start.sh
```

## Start de forma manual

1. Suba as VMs:

```bash
cd vagrant-env
cp vms.ex.json vms.json   # ajuste se preferir
vagrant up --provider=virtualbox # mude o provider conforme necessario
cd ..
```

2. Define variáveis de ambiente necessárias e aguarda o serviço `registry`
   iniciar:

```bash
export VAGRANT_DIR="$(pwd)/vagrant-env"
./bash/wait_for_service.sh $VAGRANT_DIR manager1 registry 10 5;
export REGISTRY_IP=$(./bash/service_ip.sh $VAGRANT_DIR manager1 registry)
```

2. Copiar arquivos de stack para pasta compartilhada entre host e VM que roda
   um swarm-manager;

```bash
mkdir -p vagrant-env/shared/stacks
cp stack_files/* vagrant-env/shared/stacks
```

3. Subir o serviço de banco de dados no cluster Docker Swarm, utilizando
   a stack `charger-stack` e definir variavel de ambiente `DB_IP` que indica
   a VM em que o bd está rodando:

```bash
cd $VAGRANT_DIR
vagrant ssh -c "docker stack deploy -c /shared/stacks/db.yml charger-stack" manager1;
cd ..

./bash/wait_for_service.sh $VAGRANT_DIR manager1 charger-stack_postgres 10 5;
export DB_IP=$(./bash/service_ip.sh $VAGRANT_DIR manager1 charger-stack_postgres);
```

4. Aplicar as migrations do módulo `charger-proxy` no banco de dados, compilar
   o módulo em uma imagem Docker, enviar a imagem para o registro do cluster
   Swarm e subir o serviço utilizando o arquivo de stack
   `/vagrant-env/shared/stacks/charger-proxy.yml`.

```bash
cd charger-proxy
PORT=8080 DB_URL=jdbc:postgresql://$DB_IP:5432/chargerdb DB_USER=postgrau DB_PASSWORD=postgrau ./mvnw flyway:migrate
./mvnw clean compile jib:build -Djib.to.image="$REGISTRY_IP:5000/charger-proxy:latest"

cd $VAGRANT_DIR
vagrant ssh -c "docker stack deploy -c /shared/stacks/charger-proxy.yml charger-stack" manager1;
cd ..

./bash/wait_for_service.sh $VAGRANT_DIR manager1 charger-stack_charger-proxy 10 5;
export PROXY_IP=$(./bash/service_ip.sh $VAGRANT_DIR manager1 charger-stack_charger-proxy)
```

5. Compilor módulo `charger-manager` em uma imagem Docker, enviar a imagem para
   o registro do cluster Swarm e subir o serviço utilizando o arquivo de stack
   `/vagrant-env/shared/stacks/charger-manager.yml`.

```bash
cd charger-manager
./mvnw clean compile jib:build -Djib.to.image="$REGISTRY_IP:5000/charger-manager:latest" -DwsdlUrl="http://$PROXY_IP:8082/ws/hello.wsdl"

cd $VAGRANT_DIR
vagrant ssh -c "docker stack deploy -c /shared/stacks/charger-manager.yml charger-stack" manager1;
cd ..

./bash/wait_for_service.sh $VAGRANT_DIR manager1 charger-stack_charger-manager 10 5;
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

- O **charger-proxy** disponibiliza um serviço SOAP e consome um banco de dados
  via jdbc.
- O **charger-manager** consome esse serviço SOAP e expõe dois endpoints REST
  simples para testes:
    - `POST /api/v1/messages`
    - `GET /api/v1/messages/{id}`

```bash
curl --json '{ "message": "hello" }' http://192.168.56.32:8080/api/v1/messages
curl http://192.168.56.32:8080/api/v1/messages/1
```

#### Observações

- Substitua `192.168.56.32:8080` pelo host/porta onde o `charger-manager` está
  rodando.
