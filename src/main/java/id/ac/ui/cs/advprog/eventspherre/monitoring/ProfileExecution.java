package id.ac.ui.cs.advprog.eventspherre.monitoring;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Custom annotation for profiling method execution time and performance metrics
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface ProfileExecution {
    
    /**
     * The operation type for this method (e.g., "create", "read", "update", "delete")
     */
    String operation() default "";
    
    /**
     * Whether to log slow executions
     */
    boolean logSlowExecutions() default true;
    
    /**
     * Custom threshold in milliseconds for considering an execution slow
     */
    long slowThresholdMs() default 1000;
    
    /**
     * Whether to increment counters for this operation
     */
    boolean countExecutions() default true;
    
    /**
     * Custom metric name prefix
     */
    String metricPrefix() default "event";
}
