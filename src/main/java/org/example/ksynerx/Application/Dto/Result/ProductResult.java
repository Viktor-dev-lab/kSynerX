package org.example.ksynerx.Application.Dto.Result;

import org.example.ksynerx.Domain.Model.Product;

import java.math.BigDecimal;

/** Output model của application, không trả domain model trực tiếp ra API. */
public record ProductResult(String productId, String name, BigDecimal price) {

    public static ProductResult from(Product product) {
        return new ProductResult(product.productId(), product.name(), product.price());
    }
}
