#!/usr/bin/env pwsh
# Stop all local development services for myBillOne

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  Stopping myBillOne Local Environment  " -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan

# Stop Frontend processes (Node.js)
Write-Host "`n[1/3] Stopping Frontend..." -ForegroundColor Yellow
Get-Process -Name "node" -ErrorAction SilentlyContinue | Stop-Process -Force -ErrorAction SilentlyContinue
Write-Host "  Frontend stopped." -ForegroundColor Green

# Stop Backend (Java)
Write-Host "`n[2/3] Stopping Backend..." -ForegroundColor Yellow
# Find and kill Java processes running grabbill
$javaProcesses = Get-Process -Name "java" -ErrorAction SilentlyContinue
if ($javaProcesses) {
    $javaProcesses | Stop-Process -Force -ErrorAction SilentlyContinue
    Write-Host "  Backend stopped." -ForegroundColor Green
}
else {
    Write-Host "  No backend process found." -ForegroundColor Gray
}

# Stop Docker containers
Write-Host "`n[3/3] Stopping Docker containers..." -ForegroundColor Yellow
$infraPath = Join-Path $PSScriptRoot "environments\dev\grabbill-infra"
if (Test-Path $infraPath) {
    Push-Location $infraPath
    docker compose stop
    Pop-Location
    Write-Host "  Docker containers stopped." -ForegroundColor Green
}
else {
    Write-Host "  Infrastructure path not found." -ForegroundColor Gray
}

Write-Host "`n========================================" -ForegroundColor Cyan
Write-Host "  All services stopped!                " -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan

Write-Host "`nNote: To restart, run: .\start-local.ps1" -ForegroundColor Gray
