package org.example.ksynerx.Domain.Exception;

public class ChannelConfigurationException extends RuntimeException {
    public enum Reason { DISABLED, SOURCE_MISMATCH }

    private final Reason reason;

    public ChannelConfigurationException(Reason reason, String message) {
        super(message);
        this.reason = reason;
    }

    public Reason reason() { return reason; }
}
