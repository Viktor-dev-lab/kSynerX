package org.example.ksynerx.Application.Service;

import org.example.ksynerx.Application.Dto.Command.EnqueueInventoryProductCommand;
import org.example.ksynerx.Application.Dto.Result.EnqueueInventoryProductResult;
import org.example.ksynerx.Application.Usecase.EnqueueInventoryProductUseCase;
import org.example.ksynerx.Application.Validators.InsertInventoryProductValidator;
import org.example.ksynerx.Domain.Interface.InboxEventRepository;
import org.springframework.transaction.annotation.Transactional;

public class EnqueueInventoryProductService implements EnqueueInventoryProductUseCase {
    private final InboxEventRepository repository;
    private final InventoryProductContentHasher hasher;
    private final InsertInventoryProductValidator validator;

    public EnqueueInventoryProductService(InboxEventRepository repository, InventoryProductContentHasher hasher,
                                          InsertInventoryProductValidator validator) {
        this.repository = repository;
        this.hasher = hasher;
        this.validator = validator;
    }

    @Override
    @Transactional
    public EnqueueInventoryProductResult execute(EnqueueInventoryProductCommand command) {
        if (command == null || command.product() == null) {
            throw new IllegalArgumentException("product is required");
        }
        if (command.channelType() == null) {
            throw new IllegalArgumentException("channelType is required");
        }
        validator.validateProduct(command.sourceCode(), command.product());
        String contentHash = hasher.hash(command.product());
        String payload = hasher.canonicalJson(command.product());
        long channelId = repository.getOrCreateChannel(command.sourceCode(), command.channelType());
        long revision = command.product().revision();
        long eventId = repository.insert(channelId, command.product().productId(), revision, contentHash, payload);
        return new EnqueueInventoryProductResult(eventId, channelId, revision, contentHash);
    }
}
