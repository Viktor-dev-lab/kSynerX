package org.example.ksynerx.Presentation.Dto.Response;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.example.ksynerx.Domain.Model.InventoryProduct.Category;

import java.util.List;

public record InventoryProductResponse(
        int productId,
        String sku,
        String partnerSKU,
        String productName,
        String assetType,
        boolean hasSerial,
        boolean hasExpiration,
        String color,
        String size,
        String description,
        @JsonProperty("isActive") boolean isActive,
        List<Category> categories
) { }
