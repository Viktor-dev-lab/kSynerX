package org.example.ksynerx.Presentation.Dto.Request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.util.List;

public record InventoryProductWebhookRequest(
        @JsonProperty(value = "productId", required = true) @NotNull Integer productId,
        @JsonProperty(value = "revision", required = true) @NotNull @Positive Long revision,
        @JsonProperty(value = "sku", required = true) String sku,
        @JsonProperty(value = "partnerSKU", required = true) String partnerSKU,
        @JsonProperty(value = "productName", required = true) String productName,
        @JsonProperty(value = "assetType", required = true) @Size(max = 50) String assetType,
        @JsonProperty(value = "hasSerial", required = true) @NotNull Boolean hasSerial,
        @JsonProperty(value = "hasExpiration", required = true) @NotNull Boolean hasExpiration,
        @JsonProperty(value = "color", required = true) String color,
        @JsonProperty(value = "size", required = true) String size,
        @JsonProperty(value = "description", required = true) String description,
        @JsonProperty(value = "isActive", required = true) @NotNull Boolean isActive,
        @JsonProperty(value = "categories", required = true) @NotNull @Valid List<@NotNull Category> categories
) {
    public record Category(
            @JsonProperty(value = "categoryCode", required = true) String categoryCode,
            @JsonProperty(value = "categoryName", required = true) String categoryName
    ) { }
}
