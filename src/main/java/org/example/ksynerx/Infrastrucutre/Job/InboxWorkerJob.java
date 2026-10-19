package org.example.ksynerx.Infrastrucutre.Job;

import org.example.ksynerx.Application.Usecase.ProcessNextInboxEventUseCase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.task.TaskRejectedException;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(prefix = "inbox.worker", name = "enabled", havingValue = "true", matchIfMissing = true)
public class InboxWorkerJob {
    private static final Logger LOGGER = LoggerFactory.getLogger(InboxWorkerJob.class);
    private final ProcessNextInboxEventUseCase worker;
    private final ThreadPoolTaskExecutor executor;
    private final int concurrency;
    private final int batchSize;

    public InboxWorkerJob(ProcessNextInboxEventUseCase worker,
                           @Qualifier("inboxWorkerExecutor") ThreadPoolTaskExecutor executor,
                           @Value("${inbox.worker.concurrency:2}") int concurrency,
                           @Value("${inbox.worker.batch-size:50}") int batchSize) {
        if (concurrency <= 0 || batchSize <= 0) throw new IllegalArgumentException("Worker concurrency and batchSize must be positive");
        this.worker = worker;
        this.executor = executor;
        this.concurrency = concurrency;
        this.batchSize = batchSize;
    }

    @Scheduled(initialDelayString = "${inbox.worker.initial-delay-ms:1000}",
               fixedDelayString = "${inbox.worker.fixed-delay-ms:1000}")
    public void run() {
        for (int index = 0; index < concurrency; index++) {
            try {
                executor.execute(this::drain);
            } catch (TaskRejectedException exception) {
                LOGGER.debug("Inbox worker executor is busy or stopping");
                break;
            }
        }
    }

    private void drain() {
        try {
            for (int index = 0; index < batchSize; index++) {
                var result = worker.execute();
                if (result.isEmpty()) break;
                LOGGER.info("Inbox event processed event_id={} status={}", result.get().eventId(), result.get().status());
            }
        } catch (Exception exception) {
            LOGGER.error("Inbox worker failed; uncommitted work will be retried on the next schedule", exception);
        }
    }
}
