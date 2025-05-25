# EventSphere Monitoring Validation Script
# This script validates that all monitoring components are working correctly

Write-Host "🔍 EventSphere Monitoring Validation" -ForegroundColor Green
Write-Host "====================================" -ForegroundColor Green

$failedChecks = 0
$totalChecks = 0

function Test-Endpoint {
    param(
        [string]$Name,
        [string]$Url,
        [string]$ExpectedContent = $null
    )
    
    $script:totalChecks++
    Write-Host "Testing $Name..." -ForegroundColor Yellow
    
    try {
        $response = Invoke-WebRequest -Uri $Url -TimeoutSec 10 -ErrorAction Stop
        
        if ($response.StatusCode -eq 200) {
            if ($ExpectedContent -and $response.Content -notlike "*$ExpectedContent*") {
                Write-Host "❌ $Name - Unexpected content" -ForegroundColor Red
                $script:failedChecks++
            } else {
                Write-Host "✅ $Name - OK" -ForegroundColor Green
            }
        } else {
            Write-Host "❌ $Name - Status: $($response.StatusCode)" -ForegroundColor Red
            $script:failedChecks++
        }
    } catch {
        Write-Host "❌ $Name - Error: $($_.Exception.Message)" -ForegroundColor Red
        $script:failedChecks++
    }
}

Write-Host ""
Write-Host "📊 Testing Monitoring Infrastructure..." -ForegroundColor Cyan

# Test Docker services
Write-Host "Checking Docker containers..." -ForegroundColor Yellow
$containers = docker ps --filter "name=eventsphere" --format "table {{.Names}}\t{{.Status}}"
if ($containers) {
    Write-Host $containers -ForegroundColor White
} else {
    Write-Host "⚠️  No EventSphere monitoring containers found" -ForegroundColor Yellow
}

Write-Host ""
Write-Host "🌐 Testing Service Endpoints..." -ForegroundColor Cyan

# Test Prometheus
Test-Endpoint "Prometheus" "http://localhost:9090/api/v1/status/config"

# Test Grafana
Test-Endpoint "Grafana" "http://localhost:3000/api/health"

# Test AlertManager (if running)
Test-Endpoint "AlertManager" "http://localhost:9093/api/v1/status"

Write-Host ""
Write-Host "🚀 Testing Application Endpoints..." -ForegroundColor Cyan

# Test application health
Test-Endpoint "Application Health" "http://localhost:8080/actuator/health" "UP"

# Test metrics endpoint
Test-Endpoint "Metrics Endpoint" "http://localhost:8080/actuator/metrics" "names"

# Test Prometheus metrics
Test-Endpoint "Prometheus Metrics" "http://localhost:8080/actuator/prometheus" "jvm_memory"

# Test custom metrics
Test-Endpoint "Custom EventSphere Metrics" "http://localhost:8080/actuator/eventsphere-metrics" "operationCounters"

Write-Host ""
Write-Host "🔧 Testing Metric Collection..." -ForegroundColor Cyan

# Check if Prometheus is collecting metrics from the application
try {
    $prometheusTargets = Invoke-RestMethod -Uri "http://localhost:9090/api/v1/targets" -ErrorAction Stop
    $eventSphereTarget = $prometheusTargets.data.activeTargets | Where-Object { $_.job -eq "eventsphere" }
    
    if ($eventSphereTarget) {
        if ($eventSphereTarget.health -eq "up") {
            Write-Host "✅ Prometheus is successfully scraping EventSphere metrics" -ForegroundColor Green
        } else {
            Write-Host "❌ EventSphere target is down in Prometheus" -ForegroundColor Red
            $failedChecks++
        }
    } else {
        Write-Host "⚠️  EventSphere target not found in Prometheus" -ForegroundColor Yellow
    }
    $totalChecks++
} catch {
    Write-Host "❌ Could not check Prometheus targets: $($_.Exception.Message)" -ForegroundColor Red
    $failedChecks++
    $totalChecks++
}

Write-Host ""
Write-Host "📈 Testing Sample Metrics..." -ForegroundColor Cyan

# Test some specific metrics
$metricsToCheck = @(
    "jvm_memory_used_bytes",
    "system_cpu_usage",
    "http_server_requests_seconds_count"
)

foreach ($metric in $metricsToCheck) {
    try {
        $query = "http://localhost:9090/api/v1/query?query=$metric"
        $result = Invoke-RestMethod -Uri $query -ErrorAction Stop
        
        if ($result.data.result.Count -gt 0) {
            Write-Host "✅ Metric '$metric' is available" -ForegroundColor Green
        } else {
            Write-Host "⚠️  Metric '$metric' has no data" -ForegroundColor Yellow
        }
        $totalChecks++
    } catch {
        Write-Host "❌ Could not query metric '$metric': $($_.Exception.Message)" -ForegroundColor Red
        $failedChecks++
        $totalChecks++
    }
}

Write-Host ""
Write-Host "📊 Validation Summary:" -ForegroundColor Magenta
Write-Host "=====================" -ForegroundColor Magenta

$successRate = [math]::Round((($totalChecks - $failedChecks) / $totalChecks) * 100, 1)

Write-Host "Total Checks: $totalChecks" -ForegroundColor White
Write-Host "Passed: $($totalChecks - $failedChecks)" -ForegroundColor Green
Write-Host "Failed: $failedChecks" -ForegroundColor Red
Write-Host "Success Rate: $successRate%" -ForegroundColor $(if ($successRate -ge 80) { "Green" } elseif ($successRate -ge 60) { "Yellow" } else { "Red" })

Write-Host ""
if ($failedChecks -eq 0) {
    Write-Host "🎉 All monitoring components are working perfectly!" -ForegroundColor Green
    Write-Host "🚀 Your EventSphere monitoring stack is ready for production!" -ForegroundColor Cyan
} elseif ($failedChecks -le 2) {
    Write-Host "⚠️  Most components are working, but some need attention." -ForegroundColor Yellow
    Write-Host "📖 Check the MONITORING.md file for troubleshooting guidance." -ForegroundColor White
} else {
    Write-Host "❌ Multiple components need attention." -ForegroundColor Red
    Write-Host "🔧 Please review the setup and check the logs." -ForegroundColor White
}

Write-Host ""
Write-Host "📚 Useful Commands:" -ForegroundColor Cyan
Write-Host "  View container logs: docker-compose -f docker-compose.monitoring.yml logs" -ForegroundColor White
Write-Host "  Restart monitoring:  docker-compose -f docker-compose.monitoring.yml restart" -ForegroundColor White
Write-Host "  Stop monitoring:     docker-compose -f docker-compose.monitoring.yml down" -ForegroundColor White
