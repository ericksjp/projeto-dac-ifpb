#!/bin/bash

set -euo pipefail

vagrant_dir="$1"
vm_name="$2"
service_name="$3"

cd "$vagrant_dir" || { echo "Directory $vagrant_dir does not exist."; exit 1; }

echo $(vagrant ssh -c "
    output=\$(docker service ps $service_name --format '{{ .Node }} {{ .CurrentState }}' 2>&1)

    if echo \"\$output\" | grep -q \"no such service\"; then
        echo \"$service_name service not found\"
        exit 1
    fi

    node=\$(echo \"\$output\" | grep Running | head -1 | awk '{print \$1}')
    docker node inspect \$node --format '{{.Status.Addr}}'
" manager1 | tr -d '[:space:]\r')
