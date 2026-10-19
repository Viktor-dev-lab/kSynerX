package org.example.ksynerx.Infrastrucutre;

import org.example.ksynerx.Domain.Model.InventoryProduct;
import org.example.ksynerx.Presentation.Dto.Response.InventoryProductResponse;

public final class InventoryProductMapper {
    private InventoryProductMapper() { }

    public static InventoryProduct fromResponse(InventoryProductResponse response) {
        return new InventoryProduct(
                response.productId(), response.revision(), response.sku(), response.partnerSKU(), response.productName(),
                response.assetType(), response.hasSerial(), response.hasExpiration(), response.color(),
                response.size(), response.description(), response.isActive(), response.categories());
    }
}
