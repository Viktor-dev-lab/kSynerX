package org.example.ksynerx.Application.Service;

import org.example.ksynerx.Application.Dto.Command.InsertInventoryProductCommand;
import org.example.ksynerx.Application.Dto.Result.ProcessInboxEventResult;
import org.example.ksynerx.Application.Usecase.InsertInventoryProductUseCase;
import org.example.ksynerx.Application.Usecase.ProcessNextInboxEventUseCase;
import org.example.ksynerx.Application.Validators.InsertInventoryProductValidator;
import org.example.ksynerx.Domain.Interface.InboxEventRepository;
import org.example.ksynerx.Domain.Interface.InboxWorkerRepository;
import org.example.ksynerx.Domain.Interface.InventoryProductSnapshotRepository;
import org.example.ksynerx.Domain.Model.InboxEvent;
import org.example.ksynerx.Domain.Model.InboxStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;
import tools.jackson.core.JacksonException;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

public class ProcessNextInboxEventService implements ProcessNextInboxEventUseCase {
    private static final Logger LOGGER = LoggerFactory.getLogger(ProcessNextInboxEventService.class);
    private final InboxWorkerRepository worker;
    private final InboxEventRepository inbox;
    private final InventoryProductSnapshotRepository products;
    private final InsertInventoryProductUseCase insertProduct;
    private final InventoryProductContentHasher hasher;
    private final InsertInventoryProductValidator validator;
    private final TransactionTemplate transaction;
    private final int maxAttempts;
    private final int retryDelaySeconds;

    public ProcessNextInboxEventService(InboxWorkerRepository worker, InboxEventRepository inbox,
                                        InventoryProductSnapshotRepository products, InsertInventoryProductUseCase insertProduct,
                                        InventoryProductContentHasher hasher, InsertInventoryProductValidator validator,
                                        PlatformTransactionManager transactionManager, int maxAttempts, int retryDelaySeconds) {
        if (maxAttempts <= 0 || retryDelaySeconds <= 0) {
            throw new IllegalArgumentException("Worker maxAttempts and retryDelaySeconds must be positive");
        }
        this.worker = worker;
        this.inbox = inbox;
        this.products = products;
        this.insertProduct = insertProduct;
        this.hasher = hasher;
        this.validator = validator;
        this.maxAttempts = maxAttempts;
        this.retryDelaySeconds = retryDelaySeconds;
        this.transaction = new TransactionTemplate(transactionManager);
        this.transaction.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
        this.transaction.setIsolationLevel(TransactionDefinition.ISOLATION_READ_COMMITTED);
    }

    @Override
    public Optional<ProcessInboxEventResult> execute() {
        var claimed = new AtomicReference<InboxEvent>();
        try {
            return Optional.ofNullable(transaction.execute(status -> {
                var next = worker.claimNext();
                if (next.isEmpty()) return null;
                claimed.set(next.get());
                return process(next.get());
            }));
        } catch (RuntimeException exception) {
            var event = claimed.get();
            if (event == null) throw exception;
            LOGGER.error("Inbox processing rolled back event_id={}", event.id(), exception);
            boolean permanent = exception instanceof IllegalArgumentException || exception instanceof JacksonException;
            String message = exception.getClass().getSimpleName() + ": " + exception.getMessage();
            if (message.length() > 2000) message = message.substring(0, 2000);
            String errorMessage = message;
            // The processing transaction has already rolled back, releasing all its locks.
            return transaction.execute(status -> worker.recordFailure(event, permanent, maxAttempts, retryDelaySeconds, errorMessage)
                    .map(outcome -> new ProcessInboxEventResult(event.id(), outcome)));
        }
    }

    private ProcessInboxEventResult process(InboxEvent event) {
        int productId;
        try {
            productId = Math.toIntExact(event.productId());
        } catch (ArithmeticException exception) {
            throw new IllegalArgumentException("Inbox productId is outside the supported integer range", exception);
        }
        inbox.lockProduct(event.sourceCode(), productId);
        var current = products.findForUpdate(event.sourceCode(), productId);
        var product = hasher.fromJson(event.payload());
        var command = new InsertInventoryProductCommand(event.sourceCode(), event.revision(), event.contentHash(), product);
        validator.validate(command);
        if (product.productId() != productId || !hasher.hash(product).equals(event.contentHash())) {
            throw new IllegalArgumentException("Inbox payload does not match productId or content_hash");
        }

        InboxStatus outcome;
        String message = null;
        if (current.isEmpty()) {
            insertProduct.execute(command);
            outcome = InboxStatus.APPLIED;
        } else {
            var stored = current.get();
            if (event.revision() < stored.revision()) {
                outcome = InboxStatus.STALE;
            } else if (event.revision() > stored.revision()) {
                if (event.contentHash().equals(stored.contentHash())) {
                    products.advanceRevision(stored.recordId(), event.revision());
                } else {
                    products.replace(stored.recordId(), event.sourceCode(), event.revision(), event.contentHash(), product);
                }
                outcome = InboxStatus.APPLIED;
            } else if (event.contentHash().equals(stored.contentHash())) {
                outcome = InboxStatus.DUPLICATE;
            } else {
                outcome = InboxStatus.CONFLICT;
                message = "Revision " + event.revision() + " has a different content_hash from the current product";
            }
        }
        worker.complete(event.id(), outcome, message);
        return new ProcessInboxEventResult(event.id(), outcome);
    }
}
