package org.example.inventory.Domain.Model;

import java.util.List;

public record VietfulProduct(
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
        boolean isActive,
        List<Category> categories
) {
    public VietfulProduct {
        categories = List.copyOf(categories);
    }

    public record Category(String categoryCode, String categoryName) { }
}
