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

# Check or create .env file (without hardcoded secrets)
$envFilePath = ".env"
if (-not (Test-Path $envFilePath)) {
    Write-Host "No .env file found."
    Write-Host "Creating an empty template .env file..."
    @"
# Set your admin password here
# IMPORTANT:
# - This initial password is ONLY for local testing.
# - For any production system, change it immediately.
OPENSEARCH_INITIAL_ADMIN_PASSWORD=
"@ | Set-Content -Path $envFilePath

    Write-Host ".env template created at: $envFilePath"
    Write-Host "Please edit the file and set a password before running this script again."
    exit 1
} else {
    Write-Host ".env file detected."
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
