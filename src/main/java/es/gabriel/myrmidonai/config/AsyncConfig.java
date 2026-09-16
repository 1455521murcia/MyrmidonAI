package es.gabriel.myrmidonai.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

@Configuration
@EnableAsync
public class AsyncConfig {

    @Bean(name = "taskExecutor")
    public Executor taskExecutor(){
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(2);       // Hilos que siempre estarán vivos esperando trabajo
        executor.setMaxPoolSize(5);        // Hilos máximos simultáneos (p. ej. si suben 5 PDFs a la vez)
        executor.setQueueCapacity(50);     // Si hay más de 5 peticiones, se encolan hasta 50
        executor.setThreadNamePrefix("MyrmidonAI-Async-"); // Prefijo para los logs (muy útil para debug)
        executor.initialize();
        return executor;
    }
}
