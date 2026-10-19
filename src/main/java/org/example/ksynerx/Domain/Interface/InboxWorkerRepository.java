package org.example.ksynerx.Domain.Interface;

import org.example.ksynerx.Domain.Model.InboxEvent;
import org.example.ksynerx.Domain.Model.InboxStatus;

import java.util.Optional;

public interface InboxWorkerRepository {
    Optional<InboxEvent> claimNext();
    void complete(long eventId, InboxStatus status, String message);
    Optional<InboxStatus> recordFailure(InboxEvent event, boolean permanent, int maxAttempts,
                                        int retryDelaySeconds, String message);
}
