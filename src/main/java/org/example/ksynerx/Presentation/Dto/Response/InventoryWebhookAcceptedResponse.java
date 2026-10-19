package org.example.ksynerx.Presentation.Dto.Response;

public record InventoryWebhookAcceptedResponse(
        long eventId, long channelId, int productId, long revision, String contentHash
) { }
