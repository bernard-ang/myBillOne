set -e
docker network ls | grep traefik-network > /dev/null || docker network create traefik-network;
