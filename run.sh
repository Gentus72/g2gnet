#!/bin/bash

mvn package

docker compose up -d --build --force-recreate
sleep 2
docker attach g2gnet-client-1
