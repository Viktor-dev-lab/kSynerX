package org.example.ksynerx.Presentation.Mapper;

import org.example.ksynerx.Application.Dto.Result.ProductResult;
import org.example.ksynerx.Presentation.Dto.Response.ProductResponse;

public final class ProductResponseMapper {

    private ProductResponseMapper() {
    }

    public static ProductResponse from(ProductResult result) {
        return new ProductResponse(result.productId(), result.name(), result.price());
    }
}
