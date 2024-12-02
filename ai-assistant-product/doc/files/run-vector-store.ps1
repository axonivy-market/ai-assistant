# Set vm.max_map_count using WSL
if (wsl -l -q | Select-String -Pattern "rancher-desktop") {
    Write-Host "Setting vm.max_map_count to 262144 using WSL for Rancher Desktop..."
    wsl -d rancher-desktop sysctl -w vm.max_map_count=262144
} elseif (wsl -l -q | Select-String -Pattern "docker-desktop") {
    Write-Host "Setting vm.max_map_count to 262144 using WSL for Docker Desktop..."
    wsl -d docker-desktop sysctl -w vm.max_map_count=262144
}

# Create directories if they do not exist
$directories = @("./opensearch-data", "./opensearch-logs")
foreach ($dir in $directories) {
    if (-not (Test-Path $dir)) {
        New-Item -ItemType Directory -Path $dir
        Write-Host "Created directory: $dir"
    }
}

# Set permissions for OpenSearch directories
Write-Host "Setting permissions for data, and logs directories..."
$acl = Get-Acl "./opensearch-data"
$permission = "Everyone","FullControl","Allow"
$accessRule = New-Object System.Security.AccessControl.FileSystemAccessRule $permission
$acl.SetAccessRule($accessRule)
Set-Acl "./opensearch-data" $acl

$acl = Get-Acl "./opensearch-logs"
$acl.SetAccessRule($accessRule)
Set-Acl "./opensearch-logs" $acl

# Create an .env file to store environment variables securely if it doesn't exist
$envFilePath = ".env"
if (-not (Test-Path $envFilePath)) {
    $envContent = @"
OPENSEARCH_INITIAL_ADMIN_PASSWORD=1Ae0ce926bb6a0a1d1cf10c9c9e147a50457f9c27e49780c20e103a78036380d
"@
    Set-Content -Path $envFilePath -Value $envContent
    Write-Host "Created .env file with environment variables."
} else {
    Write-Host ".env file already exists."
}

# Create docker-compose.yml file if it doesn't exist
$composeFilePath = "docker-compose.yml"
if (-not (Test-Path $composeFilePath)) {
    $composeContent = @"
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
"@
    Set-Content -Path $composeFilePath -Value $composeContent
    Write-Host "docker-compose.yml file created."
} else {
    Write-Host "docker-compose.yml file already exists."
}

# Start Docker Compose
Write-Host "Starting Docker Compose..."
docker compose up

# Keep PowerShell window open
Write-Host "Press Enter to exit..."
Read-Host
