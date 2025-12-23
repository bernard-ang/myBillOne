#!/usr/bin/env pwsh
# Stop all local development services for myBillOne

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  Stopping myBillOne Local Environment  " -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan

# Stop Frontend processes (Node.js)
Write-Host "`n[1/3] Stopping Frontend (Node/Angular)..." -ForegroundColor Yellow
$nodeProcesses = Get-Process -Name "node" -ErrorAction SilentlyContinue
if ($nodeProcesses) {
    $nodeProcesses | Stop-Process -Force -ErrorAction SilentlyContinue
    Write-Host "  Frontend processes stopped." -ForegroundColor Green
}
else {
    Write-Host "  No Frontend processes found." -ForegroundColor Gray
}

# Stop Backend (Java)
Write-Host "`n[2/3] Stopping Backend (Spring Boot)..." -ForegroundColor Yellow
# Find and kill Java processes
$javaProcesses = Get-Process -Name "java" -ErrorAction SilentlyContinue
if ($javaProcesses) {
    $javaProcesses | Stop-Process -Force -ErrorAction SilentlyContinue
    Write-Host "  Backend processes stopped." -ForegroundColor Green
}
else {
    Write-Host "  No Backend processes found." -ForegroundColor Gray
}

# Stop Docker containers
Write-Host "`n[3/3] Stopping Docker infrastructure..." -ForegroundColor Yellow
$infraPath = Join-Path $PSScriptRoot "environments\dev\grabbill-infra"
if (Test-Path $infraPath) {
    Push-Location $infraPath
    docker compose down
    Pop-Location
    Write-Host "  Docker containers stopped and removed." -ForegroundColor Green
}
else {
    Write-Host "  Infrastructure path not found (skipped)." -ForegroundColor Gray
}

Write-Host "`n========================================" -ForegroundColor Cyan
Write-Host "  All services stopped!                " -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan

Write-Host "`nNote: To restart, run: .\start-local.ps1" -ForegroundColor Gray

