package org.example.ksynerx.Domain.Interface;

import org.example.ksynerx.Domain.Model.ChannelType;

public interface InboxEventRepository {
    long getOrCreateChannel(String sourceCode, ChannelType type);
    void lockProduct(String sourceCode, int productId);
    long insert(long channelId, int productId, long revision, String contentHash, String payload);
}
