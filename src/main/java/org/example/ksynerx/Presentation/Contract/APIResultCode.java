package org.example.ksynerx.Presentation.Contract;

public enum APIResultCode {
    SUCCESS("0000", "Success"),
    VALIDATION_ERROR("3000", "Invalid request data"),
    INVALID_REQUEST("3000", "Invalid request data"),
    RESOURCE_NOT_FOUND("1404", "Resource not found"),
    USER_NOT_FOUND("1404", "User not found"),
    INTERNAL_SERVER_ERROR("0600", "Internal server error"),
    SYSTEM_ERROR("0600", "Internal server error");

    private final String code;
    private final String message;

    APIResultCode(String code, String message) {
        this.code = code;
        this.message = message;
    }

    public String getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
}
