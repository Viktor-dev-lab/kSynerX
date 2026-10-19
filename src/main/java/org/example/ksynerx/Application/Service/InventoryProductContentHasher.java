package org.example.ksynerx.Application.Service;

import org.example.ksynerx.Domain.Model.InventoryProduct;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.DeserializationFeature;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.nio.charset.StandardCharsets;
import java.util.HexFormat;
import java.util.Map;
import java.util.TreeMap;

public class InventoryProductContentHasher {
    private final JsonMapper json = JsonMapper.builder()
            .enable(DeserializationFeature.FAIL_ON_MISSING_CREATOR_PROPERTIES)
            .enable(DeserializationFeature.FAIL_ON_NULL_FOR_PRIMITIVES)
            .build();

    public InventoryProduct fromJson(String payload) {
        return json.readValue(payload, InventoryProduct.class);
    }

    public String hash(InventoryProduct product) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                    .digest(json.writeValueAsString(businessContent(product)).getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 is unavailable", exception);
        }
    }

    public String canonicalJson(InventoryProduct product) {
        var content = businessContent(product);
        content.put("revision", product.revision());
        return json.writeValueAsString(content);
    }

    private Map<String, Object> businessContent(InventoryProduct product) {
        Map<String, Object> content = new TreeMap<>();
        content.put("productId", product.productId());
        content.put("sku", product.sku());
        content.put("partnerSKU", product.partnerSKU());
        content.put("productName", product.productName());
        content.put("assetType", product.assetType());
        content.put("hasSerial", product.hasSerial());
        content.put("hasExpiration", product.hasExpiration());
        content.put("color", product.color());
        content.put("size", product.size());
        content.put("description", product.description());
        content.put("isActive", product.isActive());
        content.put("categories", product.categories().stream().map(category -> {
            Map<String, Object> value = new TreeMap<>();
            value.put("categoryCode", category.categoryCode());
            value.put("categoryName", category.categoryName());
            return value;
        }).toList());

        return content;
    }
}
