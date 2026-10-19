package org.example.ksynerx.Application.Usecase;

import org.example.ksynerx.Application.Dto.Command.EnqueueInventoryProductCommand;
import org.example.ksynerx.Application.Dto.Result.EnqueueInventoryProductResult;

public interface EnqueueInventoryProductUseCase {
    EnqueueInventoryProductResult execute(EnqueueInventoryProductCommand command);
}
