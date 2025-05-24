package id.ac.ui.cs.advprog.eventspherre.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

@Configuration
@EnableAsync
public class AsyncConfig {

    @Bean(name = "taskExecutor")
    public Executor taskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(4); // Number of threads to keep alive (even if they are idle)
        executor.setMaxPoolSize(10); // Maximum number of threads to allow in the pool
        executor.setQueueCapacity(100); // Maximum number of queued tasks before blocking
        executor.setThreadNamePrefix("EventAsync-"); // Custom prefix for thread names
        executor.initialize();
        return executor;
    }
}
