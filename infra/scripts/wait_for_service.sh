#!/bin/bash
set -euo pipefail

vagrant_dir="$1"
vm_name="$2"
service_name="$3"
max_retries="$4"
wait_time="$5"
health_hits="$6"
health_command="$7"

cd "$vagrant_dir" || {
  echo "Directory $vagrant_dir does not exist."
  exit 1
}

[[ -n "$health_command" ]] || {
  echo "health_command não pode ser vazio"
  exit 1
}

if ! bash -n <<<"$health_command"; then
  echo "health_command com erro de sintaxe"
  exit 1
fi

vagrant ssh "$vm_name" -c "
attempt=0
health_success=0

while [ \$attempt -lt $max_retries ]; do
    attempt=\$((attempt + 1))
    sleep $wait_time

    if bash -c '$health_command >/dev/null 2>&1'; then
        health_success=\$((health_success + 1))
        echo \"Health OK (\$health_success/$health_hits)\"
    else
        health_success=0
        echo \"Health FAIL (resetando contador)\"
    fi

    if [ \$health_success -ge $health_hits ]; then
        echo \"Serviço '$service_name' está saudável após \$attempt tentativas.\"
        exit 0
    fi

    echo \"Aguardando serviço '$service_name' ficar saudável... [\$attempt/$max_retries]\"
done

echo \"Timeout: O serviço '$service_name' não estabilizou após $max_retries tentativas.\"
echo \"Últimos logs do serviço '$service_name':\"
docker service logs --tail 35 $service_name | tail -n 35

exit 1
"
