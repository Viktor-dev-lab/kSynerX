package org.example.ksynerx.Application;

public class ProductNotFoundException extends RuntimeException {

    public ProductNotFoundException(String productId) {
        super("Không tìm thấy sản phẩm với productId = " + productId);
    }
}
