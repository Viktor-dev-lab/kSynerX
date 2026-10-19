package org.example.ksynerx.Domain.Interface;

import org.example.ksynerx.Domain.Model.Product;

import java.util.Optional;

/** Repository abstraction được infrastructure triển khai. */
public interface ProductRepository {

    Optional<Product> findById(String productId);
}
