package org.example.ksynerx.Presentation.Contract;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.UUID;

public record ApiResponse<T>(
        String requestId,
        String requestDateTime,
        String channel,
        ApiResult result,
        T data
) {
    public static <T> ApiResponse<T> success(T data) {
        return success(defaultRequestId(), currentDateTime(), "Web", data);
    }

    public static <T> ApiResponse<T> success(
            String requestId, String requestDateTime, String channel, T data) {
        return new ApiResponse<>(requestId, requestDateTime, channel, ApiResult.success(), data);
    }

    public static <T> ApiResponse<T> error(
            String requestId, String requestDateTime, String channel, APIResultCode code) {
        return new ApiResponse<>(requestId, requestDateTime, channel, ApiResult.of(code), null);
    }

    public static <T> ApiResponse<T> error(
            String requestId, String requestDateTime, String channel,
            APIResultCode code, String description) {
        return new ApiResponse<>(requestId, requestDateTime, channel,
                new ApiResult(code.getCode(), description), null);
    }

    public static <T> ApiResponse<T> error(APIResultCode code, String description) {
        return error(defaultRequestId(), currentDateTime(), "Web", code, description);
    }

    @JsonProperty("success")
    public boolean successful() {
        return APIResultCode.SUCCESS.getCode().equals(result.responseCode());
    }

    @JsonProperty("code")
    public String legacyCode() {
        return switch (result.responseCode()) {
            case "0000" -> "SUCCESS";
            case "1404" -> "RESOURCE_NOT_FOUND";
            case "3000" -> "INVALID_REQUEST";
            case "1502" -> "UPSTREAM_ERROR";
            case "1503" -> "EXTERNAL_SERVICE_UNAVAILABLE";
            default -> "INTERNAL_SERVER_ERROR";
        };
    }

    @JsonProperty("message")
    public String message() {
        return result.description();
    }

    private static String defaultRequestId() {
        return UUID.randomUUID().toString();
    }

    private static String currentDateTime() {
        return OffsetDateTime.now(ZoneOffset.UTC).toString();
    }
}
