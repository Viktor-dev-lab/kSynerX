package org.example.ksynerx.Infrastrucutre.Job;

import org.example.ksynerx.Application.Dto.Command.EnqueueInventoryProductCommand;
import org.example.ksynerx.Application.Usecase.EnqueueInventoryProductUseCase;
import org.example.ksynerx.Domain.Model.ChannelType;
import org.example.ksynerx.Infrastrucutre.InventoryFeignClient;
import org.example.ksynerx.Infrastrucutre.InventoryProductMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.task.TaskRejectedException;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicBoolean;

@Component
@ConditionalOnProperty(prefix = "integrations.inventory.polling", name = "enabled", havingValue = "true", matchIfMissing = true)
public class InventoryProductPollingJob {
    private static final Logger LOGGER = LoggerFactory.getLogger(InventoryProductPollingJob.class);

    private final InventoryFeignClient inventory;
    private final EnqueueInventoryProductUseCase inbox;
    private final ThreadPoolTaskExecutor executor;
    private final String sourceCode;
    private final AtomicBoolean running = new AtomicBoolean();

    public InventoryProductPollingJob(InventoryFeignClient inventory, EnqueueInventoryProductUseCase inbox,
                                      @Qualifier("inventoryPollingExecutor") ThreadPoolTaskExecutor executor,
                                      @Value("${integrations.inventory.source-code:VIETFUL_DEMO}") String sourceCode) {
        this.inventory = inventory;
        this.inbox = inbox;
        this.executor = executor;
        this.sourceCode = sourceCode;
    }

    @Scheduled(fixedDelayString = "${integrations.inventory.polling.fixed-delay-ms:30000}",
               initialDelayString = "${integrations.inventory.polling.initial-delay-ms:5000}")
    public void poll() {
        if (!running.compareAndSet(false, true)) {
            LOGGER.info("Inventory polling skipped: previous run is still active source_code={}", sourceCode);
            return;
        }
        try {
            executor.execute(() -> {
                try {
                    enqueueProducts();
                } catch (Exception exception) {
                    LOGGER.error("Inventory polling failed source_code={}", sourceCode, exception);
                } finally {
                    running.set(false);
                }
            });
        } catch (TaskRejectedException exception) {
            running.set(false);
            LOGGER.warn("Inventory polling submission rejected source_code={}", sourceCode, exception);
        }
    }

    private void enqueueProducts() {
        LOGGER.info("Inventory polling started source_code={} revision_origin=SOURCE", sourceCode);
        var products = inventory.getProductList();
        int enqueued = 0;
        for (var response : products) {
            try {
                var product = InventoryProductMapper.fromResponse(response);
                var event = inbox.execute(new EnqueueInventoryProductCommand(sourceCode, ChannelType.POLLING, product));
                LOGGER.info("Inventory product enqueued source_code={} product_id={} event_id={} revision={} content_hash={} status=PENDING revision_origin=SOURCE",
                        sourceCode, product.productId(), event.eventId(), event.revision(), event.contentHash());
                enqueued++;
            } catch (Exception exception) {
                LOGGER.error("Inventory product could not be processed source_code={} product_id={}",
                        sourceCode, response == null ? null : response.productId(), exception);
            }
        }
        LOGGER.info("Inventory polling completed source_code={} fetched={} enqueued={}", sourceCode, products.size(), enqueued);
    }
}
