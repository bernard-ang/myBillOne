#!/usr/bin/env pwsh
# Start all local development services for myBillOne

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  Starting myBillOne Local Environment  " -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan

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

# Check for Local MySQL
Write-Host "`n[2/4] Checking Local MySQL..." -ForegroundColor Yellow
$mysqlCheck = cmd /c "mysqladmin ping -u root -proot 2>&1"
if ($LASTEXITCODE -eq 0) {
    Write-Host "  Local MySQL is ready." -ForegroundColor Green
}
else {
    Write-Host "  Warning: Local MySQL might not be ready or credentials invalid." -ForegroundColor Red
}

# Start Backend
Write-Host "`n[3/4] Starting Backend (Spring Boot)..." -ForegroundColor Yellow
$backendPath = Join-Path $PSScriptRoot "grabbill-backend"
Start-Process -FilePath "cmd.exe" -ArgumentList "/c cd /d `"$backendPath`" && .\mvnw.cmd spring-boot:run -pl grabbill-server" -WindowStyle Normal
Write-Host "  Backend starting in new window (port 8080)..." -ForegroundColor Green

# Wait for backend to start
Write-Host "  Waiting for backend to initialize (20 seconds)..." -ForegroundColor Gray
Start-Sleep -Seconds 20

# Start Frontend
Write-Host "`n[4/4] Starting Frontend (Angular)..." -ForegroundColor Yellow
$uiPath = Join-Path $PSScriptRoot "grabbill-ui"

# Start Client App
Start-Process -FilePath "cmd.exe" -ArgumentList "/c cd /d `"$uiPath`" && npm run start:client" -WindowStyle Normal
Write-Host "  Client app starting in new window (port 4801)..." -ForegroundColor Green

# Start Admin App  
Start-Process -FilePath "cmd.exe" -ArgumentList "/c cd /d `"$uiPath`" && npm run start:admin" -WindowStyle Normal
Write-Host "  Admin app starting in new window (port 4802)..." -ForegroundColor Green

Write-Host "`n========================================" -ForegroundColor Cyan
Write-Host "  All services starting!               " -ForegroundColor Cyan  
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "`nAccess URLs:" -ForegroundColor White
Write-Host "  Client App:   http://localhost:4801" -ForegroundColor Green
Write-Host "  Admin App:    http://localhost:4802" -ForegroundColor Green
Write-Host "  Backend API:  http://localhost:8080" -ForegroundColor Green
Write-Host "`nInfrastructure:" -ForegroundColor White
Write-Host "  Local MySQL:  localhost:3306 (user: root / pass: root)" -ForegroundColor Gray
Write-Host "  RabbitMQ:     https://queue-console.grabbill.localhost" -ForegroundColor Gray
Write-Host "  MinIO:        https://storage-console.grabbill.localhost" -ForegroundColor Gray
Write-Host "  MailHog:      https://mail-console.grabbill.localhost" -ForegroundColor Gray
Write-Host "  Traefik:      https://traefik.grabbill.localhost" -ForegroundColor Gray
