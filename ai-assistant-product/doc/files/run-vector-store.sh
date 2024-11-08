#!/bin/bash

# Set vm.max_map_count using WSL if on Windows with WSL
if [[ "$(uname -r)" == *"Microsoft"* ]]; then
    if wsl -l -q | grep -q "rancher-desktop"; then
        echo "Setting vm.max_map_count to 262144 using WSL for Rancher Desktop..."
        wsl -d rancher-desktop sysctl -w vm.max_map_count=262144
    elif wsl -l -q | grep -q "docker-desktop"; then
        echo "Setting vm.max_map_count to 262144 using WSL for Docker Desktop..."
        wsl -d docker-desktop sysctl -w vm.max_map_count=262144
    fi
else
    # For native Linux and macOS, set vm.max_map_count directly
    echo "Setting vm.max_map_count to 262144..."
    sudo sysctl -w vm.max_map_count=262144
fi

# Create directories if they do not exist
directories=("opensearch-data" "opensearch-logs")
for dir in "${directories[@]}"; do
    if [ ! -d "$dir" ]; then
        mkdir -p "$dir"
        echo "Created directory: $dir"
    fi
done

# Set permissions for OpenSearch directories
echo "Setting permissions for data and logs directories..."
chmod -R 777 ./opensearch-data
chmod -R 777 ./opensearch-logs

# Create an .env file to store environment variables securely
env_file=".env"
cat << EOF > "$env_file"
OPENSEARCH_INITIAL_ADMIN_PASSWORD=1Ae0ce926bb6a0a1d1cf10c9c9e147a50457f9c27e49780c20e103a78036380d
EOF
echo "Created .env file with environment variables."

# Create updated docker-compose.yml file
compose_file="docker-compose.yml"
cat << EOF > "$compose_file"
services:
  opensearch:
    image: opensearchproject/opensearch:2.17.1
    container_name: axon-ivy-open-search-vector-store
    environment:
      - cluster.name=axon-ivy-open-search-vector-store-cluster
      - cluster.routing.allocation.disk.watermark.low=900mb
      - cluster.routing.allocation.disk.watermark.high=600mb
      - cluster.routing.allocation.disk.watermark.flood_stage=400mb
      - discovery.type=single-node
      - plugins.security.disabled=true
      - node.name=axon-ivy-open-search-vector-store-node
      - bootstrap.memory_lock=true
      - OPENSEARCH_JAVA_OPTS=-Xms1g -Xmx1g
    deploy:
      resources:
        limits:
          memory: 2g
        reservations:
          memory: 1g
    ulimits:
      memlock:
        soft: -1
        hard: -1
      nofile:
        soft: 65536
        hard: 65536
    ports:
      - "19201:9200"
    volumes:
      - type: bind
        source: ./opensearch-data
        target: /usr/share/opensearch/data
      - type: bind
        source: ./opensearch-logs
        target: /usr/share/opensearch/logs
    restart: unless-stopped
    env_file:
      - .env
EOF
echo "docker-compose.yml file created."

# Start Docker Compose
echo "Starting Docker Compose..."
docker compose up

# Keep Bash shell open
echo "Press Enter to exit..."
read -r
