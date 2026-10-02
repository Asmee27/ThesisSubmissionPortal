$ErrorActionPreference = "Stop"

$imageName = "thesisflow:v1"
$containerName = "thesisflow-app"

# --------------------------------------------------
# 1. Validate required environment variables
# --------------------------------------------------

if (-not $env:DB_PASSWORD) {
    throw "Deployment failed: DB_PASSWORD is not available."
}

# --------------------------------------------------
# 2. Build Docker image
# --------------------------------------------------

Write-Host "Building ThesisFlow Docker image..."

docker build -t $imageName .

if ($LASTEXITCODE -ne 0) {
    throw "Deployment failed: Docker image build failed."
}

Write-Host "Docker image built successfully."

# --------------------------------------------------
# 3. Stop and remove existing container
# --------------------------------------------------

$existingContainer = docker ps -aq -f "name=^${containerName}$"

if ($existingContainer) {

    Write-Host "Stopping existing ThesisFlow container..."

    docker stop $containerName

    if ($LASTEXITCODE -ne 0) {
        throw "Deployment failed: could not stop existing container."
    }

    Write-Host "Removing existing ThesisFlow container..."

    docker rm $containerName

    if ($LASTEXITCODE -ne 0) {
        throw "Deployment failed: could not remove existing container."
    }
}

# --------------------------------------------------
# 4. Start new container
# --------------------------------------------------

Write-Host "Starting new ThesisFlow container..."

docker run -d `
    --name $containerName `
    -p 8083:8083 `
    -e DB_PASSWORD="$env:DB_PASSWORD" `
    -e SPRING_DATASOURCE_URL="jdbc:mysql://host.docker.internal:3306/thesis_portal" `
    $imageName

if ($LASTEXITCODE -ne 0) {
    throw "Deployment failed: could not start ThesisFlow container."
}

# --------------------------------------------------
# 5. Verify container is running
# --------------------------------------------------

Start-Sleep -Seconds 3

$running = docker inspect `
    -f "{{.State.Running}}" `
    $containerName

if ($running -ne "true") {

    Write-Host "Container failed to remain running."
    Write-Host "Container logs:"

    docker logs $containerName

    throw "Deployment failed: ThesisFlow container is not running."
}

Write-Host "Container is running."

# --------------------------------------------------
# 6. Retry-based application health check
# --------------------------------------------------

Write-Host "Waiting for ThesisFlow to become healthy..."

$healthy = $false

for ($attempt = 1; $attempt -le 6; $attempt++) {

    Start-Sleep -Seconds 5

    try {

        $response = Invoke-WebRequest `
            -Uri "http://localhost:8083/" `
            -UseBasicParsing `
            -TimeoutSec 10

        if ($response.StatusCode -eq 200) {

            Write-Host "Health check returned HTTP 200"
            $healthy = $true

            break
        }
    }
    catch {

        Write-Host "Health check attempt $attempt failed. Retrying..."
    }
}

# --------------------------------------------------
# 7. Fail deployment if application never becomes healthy
# --------------------------------------------------

if (-not $healthy) {

    Write-Host "ThesisFlow failed health verification."
    Write-Host "Container logs:"

    docker logs $containerName

    throw "Deployment failed: ThesisFlow health check failed."
}

# --------------------------------------------------
# 8. Deployment success
# --------------------------------------------------

Write-Host "Docker deployment completed successfully."