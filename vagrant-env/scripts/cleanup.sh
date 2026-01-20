#!/usr/bin/env bash
set -euo pipefail

echo "=== cleanup script"

SWARM_ACTIVE=false
if docker info | grep -q "Swarm: active"; then
    SWARM_ACTIVE=true
fi

if [ "$SWARM_ACTIVE" = true ]; then
    ROLE=$(docker info --format '{{.Swarm.ControlAvailable}}')

    if [ "$ROLE" = "true" ]; then
        echo "removing swarm resources"
        docker stack ls --format '{{.Name}}' | xargs -r docker stack rm
        docker service ls --format '{{.Name}}' | xargs -r docker service rm

        sleep 5

        echo "forcing workers to quit"
        docker node ls --format '{{.ID}} {{.Hostname}} {{.Status}}' |
            awk '$3 == "Ready" {print $1}' |
            grep -v "$(docker node inspect self --format '{{.ID}}')" |
            xargs -r docker node rm --force

        echo "node is manager - leaving swarm"
        docker swarm leave --force
    else
        echo "node is worker — leaving swarm"
        docker swarm leave --force
    fi
else
    echo "node is not part of a swarm"
fi

echo "removing docker resources..."
docker container prune -f || true
docker image prune -a -f || true
docker network prune -f || true
docker volume prune -f || true

if [ -d /shared/tokens ]; then
    echo "removing tokens"
    rm -f /shared/tokens/*
fi

echo "=== clueanup done"
