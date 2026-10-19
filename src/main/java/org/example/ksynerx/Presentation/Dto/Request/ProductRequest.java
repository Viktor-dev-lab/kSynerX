package org.example.ksynerx.Presentation.Dto.Request;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.UUID;

public record ProductRequest(
        @NotBlank(message = "productId không được để trống")
        @Size(max = 64, message = "productId không được vượt quá 64 ký tự")
        String productId,
        @NotBlank @Size(max = 100)
        String requestId,
        @JsonFormat(without = JsonFormat.Feature.ADJUST_DATES_TO_CONTEXT_TIME_ZONE)
        OffsetDateTime requestDateTime,
        @NotBlank
        String channel
) {
    public ProductRequest {
        requestId = requestId == null ? UUID.randomUUID().toString() : requestId;
        requestDateTime = requestDateTime == null ? OffsetDateTime.now(ZoneOffset.UTC) : requestDateTime;
        channel = channel == null ? "Web" : channel;
    }
}
