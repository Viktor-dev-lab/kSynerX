package org.example.callback;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("callback")
public record CallbackClientProperties(
        String inventoryBaseUrl, String webhookUrl, Integer productId,
        String snapshotFile, String saveSnapshotFile, int repeat, int concurrency,
        int maxAttempts, long retryDelayMs, int timeoutMs
) {
    public CallbackClientProperties {
        if (repeat <= 0 || concurrency <= 0 || maxAttempts <= 0 || retryDelayMs < 0 || timeoutMs <= 0) {
            throw new IllegalArgumentException("Callback counts/timeouts must be positive; retry delay must be nonnegative");
        }
    }
}
