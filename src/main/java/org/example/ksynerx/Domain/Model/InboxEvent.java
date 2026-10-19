package org.example.ksynerx.Domain.Model;

public record InboxEvent(long id, String sourceCode, long productId, long revision,
                         String contentHash, String payload, int retryCount) { }
