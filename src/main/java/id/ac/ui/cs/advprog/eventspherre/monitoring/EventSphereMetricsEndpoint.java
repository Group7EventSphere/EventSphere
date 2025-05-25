package id.ac.ui.cs.advprog.eventspherre.monitoring;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuator.endpoint.annotation.Endpoint;
import org.springframework.boot.actuator.endpoint.annotation.ReadOperation;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * Custom actuator endpoint for EventSphere monitoring metrics
 */
@Component
@Endpoint(id = "eventsphere-metrics")
public class EventSphereMetricsEndpoint {

    private final MeterRegistry meterRegistry;
    private final EventManagementPerformanceMonitor performanceMonitor;

    @Autowired
    public EventSphereMetricsEndpoint(MeterRegistry meterRegistry, 
                                    EventManagementPerformanceMonitor performanceMonitor) {
        this.meterRegistry = meterRegistry;
        this.performanceMonitor = performanceMonitor;
    }

    @ReadOperation
    public Map<String, Object> getMetrics() {
        Map<String, Object> metrics = new HashMap<>();
        
        // Operation counters
        Map<String, Object> operationCounters = new HashMap<>();
        operationCounters.put("createOperations", getCounterValue("event.operations", "operation", "create"));
        operationCounters.put("readOperations", getCounterValue("event.operations", "operation", "read"));
        operationCounters.put("updateOperations", getCounterValue("event.operations", "operation", "update"));
        operationCounters.put("deleteOperations", getCounterValue("event.operations", "operation", "delete"));
        
        metrics.put("operationCounters", operationCounters);
        
        // Timer metrics
        Map<String, Object> timerMetrics = new HashMap<>();
        timerMetrics.put("averageExecutionTime", getAverageExecutionTime());
        timerMetrics.put("totalExecutions", getTotalExecutions());
        
        metrics.put("timerMetrics", timerMetrics);
        
        // Active events gauge
        metrics.put("activeEvents", getGaugeValue("event.active.events"));
        
        // Error metrics
        Map<String, Object> errorMetrics = new HashMap<>();
        errorMetrics.put("totalErrors", getCounterValue("event.errors"));
        errorMetrics.put("slowOperations", getCounterValue("event.slow.operations"));
        
        metrics.put("errorMetrics", errorMetrics);
        
        // Performance summary
        Map<String, Object> performanceSummary = new HashMap<>();
        performanceSummary.put("status", determineSystemStatus());
        performanceSummary.put("timestamp", System.currentTimeMillis());
        
        metrics.put("performanceSummary", performanceSummary);
        
        return metrics;
    }

    private double getCounterValue(String meterName, String... tags) {
        return meterRegistry.find(meterName)
                .tags(tags)
                .counter()
                .map(counter -> counter.count())
                .orElse(0.0);
    }

    private double getCounterValue(String meterName) {
        return meterRegistry.find(meterName)
                .counter()
                .map(counter -> counter.count())
                .orElse(0.0);
    }

    private double getGaugeValue(String meterName) {
        return meterRegistry.find(meterName)
                .gauge()
                .map(gauge -> gauge.value())
                .orElse(0.0);
    }

    private double getAverageExecutionTime() {
        Timer timer = meterRegistry.find("event.execution")
                .timer()
                .orElse(null);
        
        if (timer != null && timer.count() > 0) {
            return timer.totalTime(java.util.concurrent.TimeUnit.MILLISECONDS) / timer.count();
        }
        return 0.0;
    }

    private long getTotalExecutions() {
        Timer timer = meterRegistry.find("event.execution")
                .timer()
                .orElse(null);
        
        return timer != null ? timer.count() : 0;
    }

    private String determineSystemStatus() {
        double errorRate = getCounterValue("event.errors");
        double totalOperations = getCounterValue("event.operations");
        double avgExecutionTime = getAverageExecutionTime();
        
        if (totalOperations > 0) {
            double errorPercentage = (errorRate / totalOperations) * 100;
            
            if (errorPercentage > 5 || avgExecutionTime > 2000) {
                return "DEGRADED";
            } else if (errorPercentage > 1 || avgExecutionTime > 1000) {
                return "WARNING";
            }
        }
        
        return "HEALTHY";
    }
}
