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
create_directories() {
    directories=("opensearch-data" "opensearch-logs")
    for dir in "${directories[@]}"; do
        if [ ! -d "$dir" ]; then
            mkdir -p "$dir"
            echo "Created directory: $dir"
        fi
    done
}

# Set permissions for OpenSearch directories
set_permissions() {
    echo "Setting permissions for data and logs directories..."
    chmod -R 777 ./opensearch-data
    chmod -R 777 ./opensearch-logs
}

# Create an .env file to store environment variables securely if it doesn't exist
# The provided initial password (OPENSEARCH_INITIAL_ADMIN_PASSWORD) is only for local testing purposes.
# For any production or customer-facing system, please change this password immediately to ensure security and compliance.
create_env_file() {
    envFilePath=".env"
    envContent="OPENSEARCH_INITIAL_ADMIN_PASSWORD=admin"
    if [ ! -f "$envFilePath" ]; then
        echo "$envContent" > "$envFilePath"
        echo "Created .env file with environment variables."
    else
        echo ".env file already exists."
    fi
}

# Create docker-compose.yml file if it doesn't exist
create_docker_compose_file() {
    composeFilePath="docker-compose.yml"
    if [ ! -f "$composeFilePath" ]; then
        composeContent=$(cat <<EOF
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
      - "19300:9200"
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
)
        echo "$composeContent" > "$composeFilePath"
        echo "docker-compose.yml file created."
    else
        echo "docker-compose.yml file already exists."
    fi
}

# Start Docker Compose
start_docker_compose() {
    echo "Starting Docker Compose..."
    docker compose up
}

# Main script execution
create_directories
set_permissions
create_env_file
create_docker_compose_file
start_docker_compose

# Keep Bash shell open
echo "Press Enter to exit..."
read -r