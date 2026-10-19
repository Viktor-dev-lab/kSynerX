package org.example.ksynerx.Presentation.Mapper;

import org.example.ksynerx.Domain.Model.InventoryProduct;
import org.example.ksynerx.Presentation.Dto.Response.InventoryProductResponse;

public final class InventoryProductResponseMapper {
    private InventoryProductResponseMapper() { }

    public static InventoryProductResponse toResponse(InventoryProduct product) {
        return new InventoryProductResponse(
                product.productId(), product.sku(), product.partnerSKU(), product.productName(),
                product.assetType(), product.hasSerial(), product.hasExpiration(), product.color(), product.size(),
                product.description(), product.isActive(), product.categories());
    }
}
