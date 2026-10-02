$ErrorActionPreference = "Stop"

$imageName = "thesisflow:v1"
$containerName = "thesisflow-app"

if (-not $env:DB_PASSWORD) {
    throw "Deployment failed: DB_PASSWORD is not available."
}

Write-Host "Building ThesisFlow Docker image..."
docker build -t $imageName .

if ($LASTEXITCODE -ne 0) {
    throw "Docker image build failed."
}

$existingContainer = docker ps -aq -f "name=^${containerName}$"

if ($existingContainer) {
    Write-Host "Stopping existing ThesisFlow container..."
    docker stop $containerName

    Write-Host "Removing existing ThesisFlow container..."
    docker rm $containerName
}

Write-Host "Starting new ThesisFlow container..."

docker run -d `
    --name $containerName `
    -p 8083:8083 `
    -e DB_PASSWORD="$env:DB_PASSWORD" `
    -e SPRING_DATASOURCE_URL="jdbc:mysql://host.docker.internal:3306/thesis_portal" `
    $imageName

if ($LASTEXITCODE -ne 0) {
    throw "Failed to start ThesisFlow container."
}

Write-Host "Waiting for ThesisFlow to start..."
Start-Sleep -Seconds 15

$running = docker inspect `
    -f "{{.State.Running}}" `
    $containerName

if ($running -ne "true") {
    docker logs $containerName
    throw "Deployment failed: container is not running."
}

try {
    $response = Invoke-WebRequest `
        -Uri "http://localhost:8083/" `
        -UseBasicParsing `
        -TimeoutSec 10

    Write-Host "Health check returned HTTP $($response.StatusCode)"
}
catch {
    docker logs $containerName
    throw "Deployment failed: ThesisFlow health check failed."
}

Write-Host "Docker deployment completed successfully."