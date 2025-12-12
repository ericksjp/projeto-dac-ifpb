#!/bin/bash

set -euo pipefail

VAGRANT_FILE="$1"
vm_name="$2"
service_name="$3"
max_retries="$4"
wait_time="$5"

vagrant ssh -c "
    counter=0
    while [ \$counter -lt $max_retries ]; do
        replicas=\$(docker service ls | grep $service_name | awk '{print \$4}' | cut -d'/' -f1)
        if [ \"\$replicas\" -ge 1 ]; then
            echo \"$service_name has at least one replica running\"
            exit 0
        fi
        echo \"Waiting for $service_name to start... (\$counter/$max_retries)\"
        counter=\$((counter+1))
        sleep $wait_time
    done
    echo \"Timeout: $service_name did not start after $max_retries tries.\"
    exit 1
" $vm_name
