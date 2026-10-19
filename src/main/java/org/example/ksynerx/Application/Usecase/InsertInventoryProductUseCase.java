package org.example.ksynerx.Application.Usecase;

import org.example.ksynerx.Application.Dto.Command.InsertInventoryProductCommand;
import org.example.ksynerx.Application.Dto.Result.InsertInventoryProductResult;

public interface InsertInventoryProductUseCase {
    InsertInventoryProductResult execute(InsertInventoryProductCommand command);
}
