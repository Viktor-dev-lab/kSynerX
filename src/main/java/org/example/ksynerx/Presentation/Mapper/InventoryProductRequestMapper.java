package org.example.ksynerx.Presentation.Mapper;

import org.example.ksynerx.Presentation.Dto.Request.InventoryProductRequest;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class InventoryProductRequestMapper implements Converter<String, InventoryProductRequest> {
    @Override
    public InventoryProductRequest convert(String productId) {
        return new InventoryProductRequest(Integer.parseInt(productId));
    }
}
