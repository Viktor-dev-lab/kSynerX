package org.example.ksynerx.Infrastrucutre.Configuration;

import org.example.ksynerx.Application.Service.InventoryProductContentHasher;
import org.example.ksynerx.Application.Service.EnqueueInventoryProductService;
import org.example.ksynerx.Application.Validators.InsertInventoryProductValidator;
import org.example.ksynerx.Domain.Interface.InboxEventRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@Configuration(proxyBeanMethods = false)
@EnableScheduling
public class InventoryPollingConfiguration {
    @Bean
    @ConditionalOnProperty(prefix = "integrations.inventory.polling", name = "enabled", havingValue = "true", matchIfMissing = true)
    public ThreadPoolTaskExecutor inventoryPollingExecutor(
            @Value("${integrations.inventory.polling.core-pool-size:2}") int corePoolSize,
            @Value("${integrations.inventory.polling.max-pool-size:4}") int maxPoolSize,
            @Value("${integrations.inventory.polling.queue-capacity:10}") int queueCapacity,
            @Value("${tasks.executor.shutdown-seconds:20}") int shutdownSeconds) {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(corePoolSize);
        executor.setMaxPoolSize(maxPoolSize);
        executor.setQueueCapacity(queueCapacity);
        executor.setThreadNamePrefix("inventory-polling-");
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(shutdownSeconds);
        return executor;
    }

    @Bean
    public InventoryProductContentHasher inventoryProductContentHasher() {
        return new InventoryProductContentHasher();
    }

    @Bean
    public EnqueueInventoryProductService enqueueInventoryProductService(
            InboxEventRepository repository, InventoryProductContentHasher hasher,
            InsertInventoryProductValidator validator) {
        return new EnqueueInventoryProductService(repository, hasher, validator);
    }
}
