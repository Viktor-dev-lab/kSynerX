package org.example.inventory.Presentation.Dto.Response;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.example.inventory.Domain.Model.VietfulProduct.Category;

import java.util.List;

public record VietfulProductResponse(
        int productId,
        long revision,
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
