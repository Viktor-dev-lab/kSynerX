package org.example.ksynerx.Application.Dto.Command;

import org.example.ksynerx.Domain.Model.InventoryProduct;

public record InsertInventoryProductCommand(
        String sourceCode,
        long revision,
        String contentHash,
        InventoryProduct product
) { }
