# EventSphere Monitoring and Observability Guide

## Overview

This guide provides comprehensive instructions for setting up and using the monitoring and observability features implemented in EventSphere. The system uses Grafana for visualization, Prometheus for metrics collection, and Spring Boot Actuator for application monitoring.

## Architecture

The monitoring stack consists of:

- **Spring Boot Actuator**: Provides application metrics and health endpoints
- **Micrometer with Prometheus**: Exports metrics in Prometheus format
- **Prometheus**: Scrapes and stores metrics data
- **Grafana**: Visualizes metrics through dashboards
- **AlertManager**: Handles alert notifications
- **Custom Monitoring Components**: Application-specific profiling and monitoring

## Quick Start

### 1. Start the Monitoring Stack

```powershell
# Start Prometheus, Grafana, and AlertManager
docker-compose -f docker-compose.monitoring.yml up -d
```

### 2. Start EventSphere Application

```powershell
# Build and run the application
./gradlew bootRun
```

### 3. Access Monitoring Interfaces

- **Grafana Dashboard**: http://localhost:3000 (admin/admin)
- **Prometheus**: http://localhost:9090
- **AlertManager**: http://localhost:9093
- **Application Metrics**: http://localhost:8080/actuator/prometheus
- **Application Health**: http://localhost:8080/actuator/health
- **Custom Metrics**: http://localhost:8080/actuator/eventsphere-metrics

## Monitoring Features

### 1. Performance Profiling

The application automatically profiles all event management operations using the `@ProfileExecution` annotation:

```java
@ProfileExecution(operation = "create_event", slowThresholdMs = 3000)
public String createEvent(...) {
    // Method implementation
}
```

**Tracked Metrics:**
- Method execution time
- Operation success/failure rates
- Slow operation detection
- Error counting

### 2. Event Management Metrics

**Available Metrics:**
- `event_operations_total`: Total count of operations by type (create, read, update, delete)
- `event_execution_seconds`: Timer for execution duration
- `event_active_events`: Current number of active events
- `event_errors_total`: Total error count
- `event_slow_operations_total`: Count of slow operations
- `event_database_operations_seconds`: Database operation timing

### 3. System Metrics

**JVM Metrics:**
- Heap memory usage
- Non-heap memory usage
- Garbage collection statistics
- Thread pool status

**System Metrics:**
- CPU usage (system and process)
- System load average
- Disk usage

**HTTP Metrics:**
- Request duration percentiles
- Request rate
- Response status codes

## Dashboard Configuration

### Event Management Dashboard

The main dashboard (`monitoring/grafana/dashboards/event-management-dashboard.json`) includes:

1. **Event Operations Response Time**: Shows average, 95th, and 99th percentile response times
2. **Event Operations Rate**: Operations per second by type
3. **Active Events**: Current count of active events
4. **Error Rate**: Percentage of failed operations
5. **Database Operations Performance**: Database query performance
6. **Slow Operations Log**: Table of slow operations
7. **JVM Memory Usage**: Heap memory utilization
8. **HTTP Request Duration**: Web request performance
9. **System CPU Usage**: CPU utilization metrics
10. **Thread Pool Status**: Application thread pool monitoring

### Custom Metrics Endpoint

Access custom application metrics at `/actuator/eventsphere-metrics`:

```json
{
  "operationCounters": {
    "createOperations": 45,
    "readOperations": 120,
    "updateOperations": 23,
    "deleteOperations": 8
  },
  "timerMetrics": {
    "averageExecutionTime": 245.5,
    "totalExecutions": 196
  },
  "activeEvents": 15,
  "errorMetrics": {
    "totalErrors": 2,
    "slowOperations": 5
  },
  "performanceSummary": {
    "status": "HEALTHY",
    "timestamp": 1716652800000
  }
}
```

## Alert Configuration

### Configured Alerts

1. **HighResponseTime**: Triggers when 95th percentile response time > 2s
2. **HighErrorRate**: Triggers when error rate > 0.1 errors/sec
3. **DatabaseSlowQueries**: Triggers when average DB query time > 1s
4. **HighMemoryUsage**: Triggers when heap usage > 80%
5. **HighCPUUsage**: Triggers when CPU usage > 80%
6. **ServiceDown**: Triggers when application is unreachable
7. **TooManySlowOperations**: Triggers when >50 slow operations in 10 minutes

### Alert Severity Levels

- **Critical**: Service down, high error rates
- **Warning**: Performance degradation, resource usage

## Configuration Files

### Application Configuration

Key monitoring settings in `application.properties`:

```properties
# Management endpoints
management.endpoints.web.exposure.include=health,metrics,prometheus,info,httptrace,loggers,profile
management.endpoint.health.show-details=always
management.metrics.export.prometheus.enabled=true

# Custom profiling
monitoring.profiling.enabled=true
monitoring.profiling.slow-threshold-ms=1000
monitoring.profiling.detailed-logging=true

# JVM metrics
management.metrics.enable.jvm=true
management.metrics.enable.system=true
```

### Prometheus Configuration

Scrape configuration (`monitoring/prometheus/prometheus.yml`):

```yaml
scrape_configs:
  - job_name: 'eventsphere'
    metrics_path: '/actuator/prometheus'
    scrape_interval: 5s
    static_configs:
      - targets: ['host.docker.internal:8080']
```

## Troubleshooting

### Common Issues

1. **Application metrics not appearing in Prometheus**
   - Check that the application is running on port 8080
   - Verify `/actuator/prometheus` endpoint is accessible
   - Check Docker networking (use `host.docker.internal` on Windows/Mac)

2. **Grafana cannot connect to Prometheus**
   - Verify Prometheus is running on port 9090
   - Check Docker network connectivity
   - Ensure datasource URL is correct (`http://prometheus:9090`)

3. **No data in dashboard panels**
   - Verify metric names match between application and dashboard queries
   - Check time range in Grafana (default: last 1 hour)
   - Ensure application has processed some events to generate metrics

### Debugging Steps

1. **Check Application Metrics**:
   ```powershell
   curl http://localhost:8080/actuator/prometheus | findstr event
   ```

2. **Verify Prometheus Targets**:
   - Go to http://localhost:9090/targets
   - Check that EventSphere target is "UP"

3. **Test Grafana Connection**:
   - Go to Configuration > Data Sources
   - Test the Prometheus connection

4. **Check Application Health**:
   ```powershell
   curl http://localhost:8080/actuator/health
   ```

## Performance Optimization

### Monitoring Best Practices

1. **Metric Collection**: Keep scrape intervals reasonable (5-15s)
2. **Data Retention**: Configure appropriate retention periods
3. **Alert Tuning**: Adjust thresholds based on application behavior
4. **Dashboard Optimization**: Use appropriate time ranges and refresh intervals

### Custom Profiling

To add custom profiling to new methods:

```java
@ProfileExecution(
    operation = "custom_operation",
    slowThresholdMs = 2000,
    metricPrefix = "custom"
)
public void myCustomMethod() {
    // Method implementation
}
```

## Monitoring Maintenance

### Regular Tasks

1. **Review Dashboard Performance**: Weekly review of key metrics
2. **Alert Tuning**: Monthly review and adjustment of alert thresholds
3. **Metric Cleanup**: Quarterly review of unused metrics
4. **Capacity Planning**: Monitor resource usage trends

### Backup and Recovery

1. **Grafana Dashboards**: Export dashboard JSON files regularly
2. **Prometheus Data**: Configure appropriate retention and backup
3. **Configuration Files**: Version control all monitoring configurations

## Security Considerations

1. **Access Control**: Secure Grafana with proper authentication
2. **Network Security**: Use appropriate firewall rules
3. **Sensitive Data**: Avoid exposing sensitive information in metrics
4. **HTTPS**: Use HTTPS in production deployments

## Integration with CI/CD

### Automated Testing

Include monitoring endpoint tests in your test suite:

```java
@Test
void testMetricsEndpoint() {
    // Test that metrics endpoint is accessible
    // Verify expected metrics are present
}
```

### Deployment Validation

Post-deployment checks:
1. Verify all monitoring endpoints are accessible
2. Check that metrics are being collected
3. Validate alert rules are active
4. Test dashboard functionality

---

For additional support, refer to the official documentation:
- [Spring Boot Actuator](https://docs.spring.io/spring-boot/docs/current/reference/html/actuator.html)
- [Micrometer](https://micrometer.io/docs)
- [Prometheus](https://prometheus.io/docs/)
- [Grafana](https://grafana.com/docs/)
