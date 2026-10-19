package org.example.ksynerx.Infrastrucutre.Configuration;

import org.example.ksynerx.Application.Service.InventoryProductContentHasher;
import org.example.ksynerx.Application.Service.ProcessNextInboxEventService;
import org.example.ksynerx.Application.Usecase.InsertInventoryProductUseCase;
import org.example.ksynerx.Application.Validators.InsertInventoryProductValidator;
import org.example.ksynerx.Domain.Interface.InboxEventRepository;
import org.example.ksynerx.Domain.Interface.InboxWorkerRepository;
import org.example.ksynerx.Domain.Interface.InventoryProductSnapshotRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration(proxyBeanMethods = false)
@EnableScheduling
public class InboxWorkerConfiguration {
    @Bean
    @ConditionalOnProperty(prefix = "inbox.worker", name = "enabled", havingValue = "true", matchIfMissing = true)
    public ThreadPoolTaskExecutor inboxWorkerExecutor(
            @Value("${inbox.worker.concurrency:2}") int concurrency,
            @Value("${tasks.executor.shutdown-seconds:20}") int shutdownSeconds) {
        if (concurrency <= 0) throw new IllegalArgumentException("Worker concurrency must be positive");
        var executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(concurrency);
        executor.setMaxPoolSize(concurrency);
        executor.setQueueCapacity(0);
        executor.setThreadNamePrefix("inbox-worker-");
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(shutdownSeconds);
        return executor;
    }

    @Bean
    public ProcessNextInboxEventService processNextInboxEventService(
            InboxWorkerRepository worker, InboxEventRepository inbox, InventoryProductSnapshotRepository products,
            InsertInventoryProductUseCase insertProduct, InventoryProductContentHasher hasher,
            InsertInventoryProductValidator validator, PlatformTransactionManager transactionManager,
            @Value("${inbox.worker.max-attempts:5}") int maxAttempts,
            @Value("${inbox.worker.retry-delay-seconds:10}") int retryDelaySeconds) {
        return new ProcessNextInboxEventService(worker, inbox, products, insertProduct, hasher, validator,
                transactionManager, maxAttempts, retryDelaySeconds);
    }
}
