package org.example.ksynerx.Application.Dto.Result;

public record EnqueueInventoryProductResult(
        long eventId,
        long channelId,
        long revision,
        String contentHash
) { }
