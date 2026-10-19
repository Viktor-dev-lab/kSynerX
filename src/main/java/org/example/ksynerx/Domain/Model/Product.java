package org.example.ksynerx.Domain.Model;

import java.math.BigDecimal;

/** Domain model thuần Java, không phụ thuộc Spring, HTTP hoặc JDBC. */
public record Product(String productId, String name, BigDecimal price) {
}
