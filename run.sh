#!/bin/bash

docker compose down

mvn package

docker compose up -d --build --force-recreate
sleep 1
docker compose logs -f # &
# kitty docker attach g2gnet-client-1
