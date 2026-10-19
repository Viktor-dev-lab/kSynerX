package org.example.ksynerx.Application.Dto.Command;

import org.example.ksynerx.Domain.Model.ChannelType;
import org.example.ksynerx.Domain.Model.InventoryProduct;

public record EnqueueInventoryProductCommand(
        String sourceCode,
        ChannelType channelType,
        InventoryProduct product
) { }
