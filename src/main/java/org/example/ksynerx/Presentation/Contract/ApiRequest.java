package org.example.ksynerx.Presentation.Contract;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.time.OffsetDateTime;

public record ApiRequest<T> (
        @NotBlank @Size(max = 100) String requestId,
        @NotNull OffsetDateTime requestDateTime,
        @NotBlank @Pattern(regexp = "POLLING|WEBHOOK|EXCEL") String channel,
        @NotNull @Valid T data
){}
