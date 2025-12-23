#!/usr/bin/env pwsh
# Start all local development services for myBillOne

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  Starting myBillOne Local Environment  " -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan

# Set HOME variable for Docker Compose path expansion
$env:HOME = $env:USERPROFILE

# Stop any existing instances to prevent port conflicts
Write-Host "`n[0/5] Stopping existing instances..." -ForegroundColor Yellow
$javaProcs = Get-Process -Name java -ErrorAction SilentlyContinue
if ($javaProcs) {
    Write-Host "  Stopping $($javaProcs.Count) Java process(es)..." -NoNewline
    $javaProcs | Stop-Process -Force -ErrorAction SilentlyContinue
    Write-Host " Done." -ForegroundColor Green
}
$nodeProcs = Get-Process -Name node -ErrorAction SilentlyContinue
if ($nodeProcs) {
    Write-Host "  Stopping $($nodeProcs.Count) Node process(es)..." -NoNewline
    $nodeProcs | Stop-Process -Force -ErrorAction SilentlyContinue
    Write-Host " Done." -ForegroundColor Green
}
if (-not $javaProcs -and -not $nodeProcs) {
    Write-Host "  No existing processes found." -ForegroundColor Gray
}

# Ensure Traefik external network exists
$networkName = "traefik-network"
$networkCheck = docker network ls --filter "name=$networkName" --format "{{.Name}}"
if (-not $networkCheck) {
    Write-Host "`n[0/4] Creating external network: $networkName..." -ForegroundColor Yellow
    docker network create $networkName
}

# Start Docker infrastructure
Write-Host "`n[1/4] Starting Docker infrastructure..." -ForegroundColor Yellow
$infraPath = Join-Path $PSScriptRoot "environments\dev\grabbill-infra"
if (Test-Path $infraPath) {
    Push-Location $infraPath
    docker compose up -d
    Pop-Location
    Write-Host "  Docker services started (traefik, mariadb, rabbitmq, minio, mailhog)." -ForegroundColor Green
}
else {
    Write-Host "  Infrastructure path not found: $infraPath" -ForegroundColor Red
    exit 1
}

# Check for MariaDB in Docker
Write-Host "`n[2/4] Checking MariaDB (Docker)..." -ForegroundColor Yellow
$maxRetries = 30
$retryCount = 0
$dbReady = $false

while (-not $dbReady -and $retryCount -lt $maxRetries) {
    Write-Host "  Waiting for MariaDB to be ready ($($retryCount + 1)/$maxRetries)..." -NoNewline
    $mysqlCheck = docker exec grabbill-infra-mariadb-1 mysqladmin ping -u grabbill.root -ptest12345 2>&1
    if ($LASTEXITCODE -eq 0) {
        $dbReady = $true
        Write-Host " Ready!" -ForegroundColor Green
    }
    else {
        Write-Host " Not ready yet."
        Start-Sleep -Seconds 2
        $retryCount++
    }
}

if (-not $dbReady) {
    Write-Host "  Warning: MariaDB in Docker failed to start within the timeout." -ForegroundColor Red
    Write-Host "  Last Error was: $mysqlCheck" -ForegroundColor Gray
}

# Verify host port 3307 is accessible
Write-Host "  Verifying MariaDB port 3307 is accessible from host..." -NoNewline
$portReady = $false
$portRetries = 0
while (-not $portReady -and $portRetries -lt 15) {
    try {
        $tcpClient = New-Object System.Net.Sockets.TcpClient
        $tcpClient.Connect("localhost", 3307)
        $tcpClient.Close()
        $portReady = $true
        Write-Host " OK!" -ForegroundColor Green
    }
    catch {
        Start-Sleep -Seconds 1
        $portRetries++
        Write-Host "." -NoNewline
    }
}
if (-not $portReady) {
    Write-Host " Failed to connect to port 3307!" -ForegroundColor Red
}

# Start Backend
Write-Host "`n[3/4] Starting Backend (Spring Boot)..." -ForegroundColor Yellow
$backendPath = Join-Path $PSScriptRoot "grabbill-backend"
Start-Process -FilePath "powershell.exe" -ArgumentList "-NoExit", "-Command", "cd `"$backendPath`"; .\mvnw.cmd spring-boot:run -pl grabbill-server" -WindowStyle Normal
Write-Host "  Backend starting in new window (port 8080)..." -ForegroundColor Green

# Wait for backend to start
Write-Host "  Waiting for backend to initialize (20 seconds)..." -ForegroundColor Gray
Start-Sleep -Seconds 20

# Start Frontend
Write-Host "`n[4/4] Starting Frontend (Angular)..." -ForegroundColor Yellow
$uiPath = Join-Path $PSScriptRoot "grabbill-ui"

# Start Client App
Start-Process -FilePath "powershell.exe" -ArgumentList "-NoExit", "-Command", "cd `"$uiPath`"; npm run start:client" -WindowStyle Normal
Write-Host "  Client app starting in new window (port 4801)..." -ForegroundColor Green

# Start Admin App  
Start-Process -FilePath "powershell.exe" -ArgumentList "-NoExit", "-Command", "cd `"$uiPath`"; npm run start:admin" -WindowStyle Normal
Write-Host "  Admin app starting in new window (port 4802)..." -ForegroundColor Green

Write-Host "`n========================================" -ForegroundColor Cyan
Write-Host "  All services starting!               " -ForegroundColor Cyan  
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "`nAccess URLs:" -ForegroundColor White
Write-Host "  Client App:   http://localhost:4801" -ForegroundColor Green
Write-Host "  Admin App:    http://localhost:4802" -ForegroundColor Green
Write-Host "  Backend API:  http://localhost:8080" -ForegroundColor Green
Write-Host "`nInfrastructure:" -ForegroundColor White
Write-Host "  MariaDB:      localhost:3307 (user: grabbill.root / pass: test12345)" -ForegroundColor Gray
Write-Host "  RabbitMQ:     localhost:5672 (user: grabbill.root / pass: test12345)" -ForegroundColor Gray
Write-Host "  MinIO:        http://localhost:9000 (user: grabbill.root / pass: test12345)" -ForegroundColor Gray
Write-Host "  MailHog:      http://localhost:8025" -ForegroundColor Gray
Write-Host "  Traefik URLS: *.grabbill.localhost (requires hosts file entries)" -ForegroundColor Gray

