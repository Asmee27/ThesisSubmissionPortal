$ErrorActionPreference = "Stop"

$java = "C:\DevTools\jdk-21\bin\java.exe"
$jar = Join-Path $PSScriptRoot "..\target\thesis-submission-portal-0.0.1-SNAPSHOT.jar"

if (-not (Test-Path $jar)) {
    throw "Deployment failed: JAR not found at $jar"
}

if (-not $env:DB_PASSWORD) {
    throw "Deployment failed: DB_PASSWORD is not available."
}

Write-Host "Deploying ThesisFlow..."

# Start the packaged Spring Boot application
$process = Start-Process `
    -FilePath $java `
    -ArgumentList "-jar", "`"$jar`"" `
    -WorkingDirectory (Split-Path $jar) `
    -PassThru

Write-Host "ThesisFlow started with PID $($process.Id)"
Write-Host "Deployment completed."