package org.example.inventory.Presentation.Mapper;

import org.example.inventory.Domain.Model.VietfulProduct;
import org.example.inventory.Presentation.Dto.Response.VietfulProductResponse;

public final class VietfulProductResponseMapper {
    private VietfulProductResponseMapper() { }

    public static VietfulProductResponse toResponse(VietfulProduct product) {
        return new VietfulProductResponse(
                product.productId(), product.sku(), product.partnerSKU(), product.productName(),
                product.assetType(), product.hasSerial(), product.hasExpiration(), product.color(),
                product.size(), product.description(), product.isActive(), product.categories());
    }
}
