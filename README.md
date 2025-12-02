# projeto-dac-ifpb

## Pré-requisitos

- `JDK 21`
- `git`
- `Vagrant` e um provider compatível (ex.: VirtualBox ou libvirt) para o ambiente de VMs.

## Estrutura do Repositório

- `charger-manager/` — módulo Spring Boot.
- `charger-proxy/` — módulo Spring Boot.
- `vagrant-env/` — configuração do Vagrant para criar VMs e provisionar um
  cluster (inclui `Vagrantfile`, scripts e arquivos de configuração).
- `start.sh` — script que prepara `vms.json`, sobe as VMs, gera imagens via
  `jib` e provisiona a stack.
- `stack.ex.yml` — arquivo de stack que pode ser copiado para
  `vagrant-env/shared/stacks/stack.yml` para deploy.

## Start de forma automatica

O `start.sh` fornece um fluxo automático que:

- copia `vms.ex.json` para `vms.json` dentro de `vagrant-env`;
- executa `vagrant up` para criar as VMs configuradas;
- builda as imagens Docker usando o plugin Jib e publica para o registry local
  configurado nas VMs;
- copia a `stack.ex.yml` para a pasta compartilhada usada pelo Vagrant;
- provisiona o manager com `docker stack deploy` para iniciar a stack.
- aplica as migrations ao banco de dados.

```bash
./start.sh
```

## Start de forma manual

1. Suba as VMs:

```bash
cd vagrant-env
cp vms.ex.json vms.json   # ajuste se preferir
vagrant up
```

2. Build das imagens localmente

```bash
cd ../charger-manager
./mvnw jib:build -Djib.to.image=192.168.56.32:5000/charger-manager:latest

cd ../charger-proxy
./mvnw jib:build -Djib.to.image=192.168.56.32:5000/charger-proxy:latest
```

3. Copie o `stack.yml` para `vagrant-env/shared/stacks/stack.yml` e execute no
   manager:

```bash
cd ../
cp stack.ex.yml vagrant-env/shared/stacks/stack.yml

cd vagrant-env
vagrant provision manager1 --provision-with start-stack
```

4. Aplique as migrações ao banco de dados

```bash
cd ../charger-proxy
PORT=8080 DB_URL=jdbc:postgresql://192.168.56.32:5432/chargerdb DB_USER=postgrau DB_PASSWORD=postgrau ./mvnw flyway:migrate
```

#### Observações

- O arquivo `vms.json` tem que ter no mínimo 1 vm como manager;
- O endereço do registry (`192.168.56.32:5000`) é usado como exemplo e deve ser
  ajustado conforme sua rede.
- Você pode ajustar os arquivos `vms.json` e `stack.yml` de acordo com a sua
  preferência.
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
