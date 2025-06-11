#!/bin/bash

docker compose down

mvn package

docker compose up -d --build --force-recreate
sleep 1
docker compose logs -f &
# kitty docker exec -it g2gnet-client-1 sh &
kitty docker exec -it g2gnet-server2-1 sh &
kitty docker attach g2gnet-client-1

