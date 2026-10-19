package org.example.ksynerx.Presentation.Dto.Response;

import java.math.BigDecimal;

public record ProductResponse(String productId, String name, BigDecimal price) {
}
