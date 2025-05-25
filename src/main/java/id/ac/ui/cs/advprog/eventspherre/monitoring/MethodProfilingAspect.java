package id.ac.ui.cs.advprog.eventspherre.monitoring;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Aspect for monitoring method execution times and counting operations
 */
@Aspect
@Component
public class MethodProfilingAspect {

    private static final Logger logger = LoggerFactory.getLogger(MethodProfilingAspect.class);

    private final MeterRegistry meterRegistry;

    @Value("${eventmanagement.profiling.enabled:true}")
    private boolean profilingEnabled;

    @Value("${eventmanagement.profiling.slow-threshold-ms:1000}")
    private long defaultSlowThresholdMs;

    @Value("${eventmanagement.profiling.log-all-methods:false}")
    private boolean logAllMethods;

    @Autowired
    public MethodProfilingAspect(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
    }

    /**
     * Profile all service layer methods
     */
    @Around("execution(* id.ac.ui.cs.advprog.eventspherre.service..*(..))")
    public Object profileServiceMethods(ProceedingJoinPoint joinPoint) throws Throwable {
        if (!profilingEnabled) {
            return joinPoint.proceed();
        }

        String className = joinPoint.getTarget().getClass().getSimpleName();
        String methodName = joinPoint.getSignature().getName();
        String fullMethodName = className + "." + methodName;

        return executeWithProfiling(joinPoint, fullMethodName, "service", defaultSlowThresholdMs);
    }

    /**
     * Profile all controller layer methods
     */
    @Around("execution(* id.ac.ui.cs.advprog.eventspherre.controller..*(..))")
    public Object profileControllerMethods(ProceedingJoinPoint joinPoint) throws Throwable {
        if (!profilingEnabled) {
            return joinPoint.proceed();
        }

        String className = joinPoint.getTarget().getClass().getSimpleName();
        String methodName = joinPoint.getSignature().getName();
        String fullMethodName = className + "." + methodName;

        return executeWithProfiling(joinPoint, fullMethodName, "controller", defaultSlowThresholdMs);
    }

    /**
     * Profile methods annotated with @ProfileExecution
     */
    @Around("@annotation(profileExecution)")
    public Object profileAnnotatedMethods(ProceedingJoinPoint joinPoint, ProfileExecution profileExecution) throws Throwable {
        if (!profilingEnabled) {
            return joinPoint.proceed();
        }

        String className = joinPoint.getTarget().getClass().getSimpleName();
        String methodName = joinPoint.getSignature().getName();
        String fullMethodName = className + "." + methodName;

        String operation = profileExecution.operation().isEmpty() ? "unknown" : profileExecution.operation();
        long threshold = profileExecution.slowThresholdMs();

        Timer.Sample sample = Timer.start(meterRegistry);
        long startTime = System.currentTimeMillis();

        try {
            Object result = joinPoint.proceed();
            
            long executionTime = System.currentTimeMillis() - startTime;
            
            // Record metrics
            if (profileExecution.countExecutions()) {
                recordMetrics(profileExecution.metricPrefix(), operation, executionTime, sample);
            }

            // Log slow executions
            if (profileExecution.logSlowExecutions() && executionTime > threshold) {
                logger.warn("SLOW EXECUTION: {} took {}ms (threshold: {}ms)", 
                    fullMethodName, executionTime, threshold);
            }

            if (logAllMethods) {
                logger.debug("Method {} executed in {}ms", fullMethodName, executionTime);
            }

            return result;
        } catch (Exception e) {
            // Record error metrics
            Counter.builder(profileExecution.metricPrefix() + ".error.total")
                    .tag("operation", operation)
                    .tag("method", methodName)
                    .tag("exception", e.getClass().getSimpleName())
                    .register(meterRegistry)
                    .increment();

            logger.error("Error in method {}: {}", fullMethodName, e.getMessage());
            throw e;
        } finally {
            sample.stop(Timer.builder(profileExecution.metricPrefix() + "." + operation + ".duration")
                    .tag("method", methodName)
                    .register(meterRegistry));
        }
    }

    private Object executeWithProfiling(ProceedingJoinPoint joinPoint, String methodName, String layer, long threshold) throws Throwable {
        Timer.Sample sample = Timer.start(meterRegistry);
        long startTime = System.currentTimeMillis();

        try {
            Object result = joinPoint.proceed();
            
            long executionTime = System.currentTimeMillis() - startTime;

            // Log slow executions
            if (executionTime > threshold) {
                logger.warn("SLOW {} METHOD: {} took {}ms (threshold: {}ms)", 
                    layer.toUpperCase(), methodName, executionTime, threshold);
            }

            if (logAllMethods) {
                logger.debug("{} method {} executed in {}ms", layer, methodName, executionTime);
            }

            return result;
        } catch (Exception e) {
            // Record error metrics
            Counter.builder("method.error.total")
                    .tag("layer", layer)
                    .tag("method", methodName)
                    .tag("exception", e.getClass().getSimpleName())
                    .register(meterRegistry)
                    .increment();

            logger.error("Error in {} method {}: {}", layer, methodName, e.getMessage());
            throw e;
        } finally {
            sample.stop(Timer.builder("method.execution.duration")
                    .tag("layer", layer)
                    .tag("method", methodName)
                    .register(meterRegistry));
        }
    }

    private void recordMetrics(String metricPrefix, String operation, long executionTime, Timer.Sample sample) {
        // Increment operation counter
        Counter.builder(metricPrefix + "." + operation + ".total")
                .register(meterRegistry)
                .increment();

        // Record execution time distribution
        Timer.builder(metricPrefix + "." + operation + ".duration")
                .register(meterRegistry)
                .record(executionTime, java.util.concurrent.TimeUnit.MILLISECONDS);
    }
}
