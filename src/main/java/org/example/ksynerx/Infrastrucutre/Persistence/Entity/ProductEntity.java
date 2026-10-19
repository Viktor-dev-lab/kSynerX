package org.example.ksynerx.Infrastrucutre.Persistence.Entity;

import java.math.BigDecimal;

/** Biểu diễn một bản ghi của bảng products tại persistence layer. */
public record ProductEntity(String productId, String name, BigDecimal price) {
}
