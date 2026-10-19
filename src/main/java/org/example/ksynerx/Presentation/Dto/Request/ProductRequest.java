package org.example.ksynerx.Presentation.Dto.Request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.UUID;

public record ProductRequest(
        @NotBlank(message = "productId không được để trống")
        @Size(max = 64, message = "productId không được vượt quá 64 ký tự")
        String productId,
        @NotBlank @Size(max = 100)
        String requestId,
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime requestDateTime,
        @NotBlank
        String channel
) {
    public ProductRequest {
        requestId = requestId == null ? UUID.randomUUID().toString() : requestId;
        requestDateTime = requestDateTime == null ? OffsetDateTime.now(ZoneOffset.UTC) : requestDateTime;
        channel = channel == null ? "Web" : channel;
    }
}
