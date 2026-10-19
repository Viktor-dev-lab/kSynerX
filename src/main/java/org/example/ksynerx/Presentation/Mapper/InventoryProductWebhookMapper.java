package org.example.ksynerx.Presentation.Mapper;

import org.example.ksynerx.Domain.Model.InventoryProduct;
import org.example.ksynerx.Presentation.Dto.Request.InventoryProductWebhookRequest;

public final class InventoryProductWebhookMapper {
    private InventoryProductWebhookMapper() { }

    public static InventoryProduct toProduct(InventoryProductWebhookRequest request) {
        return new InventoryProduct(request.productId(), request.revision(), request.sku(), request.partnerSKU(),
                request.productName(), request.assetType(), request.hasSerial(), request.hasExpiration(),
                request.color(), request.size(), request.description(), request.isActive(),
                request.categories().stream().map(category ->
                        new InventoryProduct.Category(category.categoryCode(), category.categoryName())).toList());
    }
}
