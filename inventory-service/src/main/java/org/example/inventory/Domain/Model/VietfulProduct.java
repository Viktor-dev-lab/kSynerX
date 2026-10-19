package org.example.inventory.Domain.Model;

import java.util.List;

public record VietfulProduct(
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
        boolean isActive,
        List<Category> categories
) {
    public VietfulProduct {
        if (revision <= 0) throw new IllegalArgumentException("revision must be positive");
        categories = List.copyOf(categories);
    }

    public VietfulProduct withRevision(long value) {
        return new VietfulProduct(productId, value, sku, partnerSKU, productName, assetType,
                hasSerial, hasExpiration, color, size, description, isActive, categories);
    }

    public record Category(String categoryCode, String categoryName) { }
}
