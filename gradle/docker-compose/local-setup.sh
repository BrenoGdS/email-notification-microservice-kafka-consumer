#!/bin/bash

if [ $(docker ps -q | wc -l) -gt 0 ]; then
    echo "Stopping and removing existing Docker containers..."
    docker rm -f $(docker ps -aq)
else
    echo "No running Docker containers to remove."
fi

echo "Cleaning up stopped containers, volumes, and networks..."
docker container prune -f
docker volume prune -f
docker network prune -f

echo "Starting containers with docker-compose..."
docker-compose -f gradle/docker-compose/docker-compose.yml --env-file .env up -d