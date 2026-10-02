$ErrorActionPreference = "Stop"

$java = "C:\DevTools\jdk-21\bin\java.exe"
$jar = Join-Path $PSScriptRoot "..\target\thesis-submission-portal-0.0.1-SNAPSHOT.jar"

if (-not (Test-Path $jar)) {
    throw "Deployment failed: JAR not found at $jar"
}

if (-not $env:DB_PASSWORD) {
    throw "Deployment failed: DB_PASSWORD is not available."
}

$logDir = "C:\ThesisFlow\logs"
New-Item -ItemType Directory -Force -Path $logDir | Out-Null

$stdout = Join-Path $logDir "thesisflow-out.log"
$stderr = Join-Path $logDir "thesisflow-error.log"

Write-Host "Starting ThesisFlow deployment..."

# Prevent Jenkins ProcessTreeKiller from terminating the deployed application
$env:BUILD_ID = "dontKillMe"
$env:JENKINS_NODE_COOKIE = "dontKillMe"

$process = Start-Process `
    -FilePath $java `
    -ArgumentList "-jar", "`"$jar`"" `
    -WorkingDirectory (Split-Path $jar) `
    -RedirectStandardOutput $stdout `
    -RedirectStandardError $stderr `
    -PassThru

Write-Host "Started ThesisFlow with PID $($process.Id)"

Start-Sleep -Seconds 12

if ($process.HasExited) {
    Write-Host "ThesisFlow failed to remain running."

    if (Test-Path $stderr) {
        Get-Content $stderr -Tail 30
    }

    throw "Deployment failed: ThesisFlow process exited."
}

try {
    $response = Invoke-WebRequest `
        -Uri "http://localhost:8083/" `
        -UseBasicParsing `
        -TimeoutSec 10

    Write-Host "Health check returned HTTP $($response.StatusCode)"
}
catch {
    throw "Deployment failed: ThesisFlow did not respond on port 8083."
}

Write-Host "ThesisFlow deployment verified successfully."