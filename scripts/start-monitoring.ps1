# EventSphere Monitoring Setup Script
# This script sets up and starts the complete monitoring stack

Write-Host "🚀 EventSphere Monitoring Setup" -ForegroundColor Green
Write-Host "================================" -ForegroundColor Green

# Check if Docker is running
Write-Host "Checking Docker status..." -ForegroundColor Yellow
try {
    docker version | Out-Null
    Write-Host "✅ Docker is running" -ForegroundColor Green
} catch {
    Write-Host "❌ Docker is not running. Please start Docker Desktop." -ForegroundColor Red
    exit 1
}

# Check if docker-compose is available
Write-Host "Checking Docker Compose..." -ForegroundColor Yellow
try {
    docker-compose version | Out-Null
    Write-Host "✅ Docker Compose is available" -ForegroundColor Green
} catch {
    Write-Host "❌ Docker Compose is not available. Please install Docker Compose." -ForegroundColor Red
    exit 1
}

# Create required directories
Write-Host "Creating monitoring directories..." -ForegroundColor Yellow
$directories = @(
    "monitoring\grafana\dashboards",
    "monitoring\grafana\datasources", 
    "monitoring\prometheus",
    "monitoring\alertmanager"
)

foreach ($dir in $directories) {
    if (!(Test-Path $dir)) {
        New-Item -ItemType Directory -Path $dir -Force | Out-Null
        Write-Host "📁 Created directory: $dir" -ForegroundColor Cyan
    }
}

# Stop existing monitoring services
Write-Host "Stopping existing monitoring services..." -ForegroundColor Yellow
docker-compose -f docker-compose.monitoring.yml down 2>$null

# Start monitoring stack
Write-Host "Starting monitoring stack..." -ForegroundColor Yellow
docker-compose -f docker-compose.monitoring.yml up -d

if ($LASTEXITCODE -eq 0) {
    Write-Host "✅ Monitoring stack started successfully!" -ForegroundColor Green
    
    # Wait for services to be ready
    Write-Host "Waiting for services to be ready..." -ForegroundColor Yellow
    Start-Sleep -Seconds 30
    
    Write-Host ""
    Write-Host "🎯 Access Points:" -ForegroundColor Cyan
    Write-Host "  Grafana Dashboard: http://localhost:3000 (admin/admin)" -ForegroundColor White
    Write-Host "  Prometheus:        http://localhost:9090" -ForegroundColor White
    Write-Host "  AlertManager:      http://localhost:9093" -ForegroundColor White
    Write-Host ""
    Write-Host "📊 Application Endpoints (start your app first):" -ForegroundColor Cyan
    Write-Host "  Health Check:      http://localhost:8080/actuator/health" -ForegroundColor White
    Write-Host "  Metrics:           http://localhost:8080/actuator/prometheus" -ForegroundColor White
    Write-Host "  Custom Metrics:    http://localhost:8080/actuator/eventsphere-metrics" -ForegroundColor White
    Write-Host ""
    Write-Host "🏃 Next Steps:" -ForegroundColor Magenta
    Write-Host "  1. Start your EventSphere application: ./gradlew bootRun" -ForegroundColor White
    Write-Host "  2. Open Grafana and import the dashboard" -ForegroundColor White
    Write-Host "  3. Generate some events to see metrics in action" -ForegroundColor White
    Write-Host ""
    Write-Host "📖 For detailed setup instructions, see MONITORING.md" -ForegroundColor Yellow
    
} else {
    Write-Host "❌ Failed to start monitoring stack" -ForegroundColor Red
    Write-Host "Check the logs: docker-compose -f docker-compose.monitoring.yml logs" -ForegroundColor Yellow
}

# Function to test application readiness
function Test-ApplicationReadiness {
    try {
        $response = Invoke-WebRequest -Uri "http://localhost:8080/actuator/health" -TimeoutSec 5 -ErrorAction Stop
        return $response.StatusCode -eq 200
    } catch {
        return $false
    }
}

# Check if application is running
Write-Host ""
Write-Host "Checking if EventSphere application is running..." -ForegroundColor Yellow
if (Test-ApplicationReadiness) {
    Write-Host "✅ EventSphere application is running and healthy!" -ForegroundColor Green
    Write-Host "🎉 Complete monitoring setup is ready!" -ForegroundColor Green
} else {
    Write-Host "⚠️  EventSphere application is not running." -ForegroundColor Yellow
    Write-Host "   Start it with: ./gradlew bootRun" -ForegroundColor White
}
