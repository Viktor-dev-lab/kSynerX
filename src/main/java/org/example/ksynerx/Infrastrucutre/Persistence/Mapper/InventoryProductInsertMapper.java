package org.example.ksynerx.Infrastrucutre.Persistence.Mapper;

import org.example.ksynerx.Domain.Model.InventoryProduct;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;

import java.sql.Types;

public final class InventoryProductInsertMapper {
    private InventoryProductInsertMapper() { }

    public static MapSqlParameterSource product(String sourceCode, long revision, String contentHash,
                                               InventoryProduct product) {
        return new MapSqlParameterSource()
                .addValue("sourceCode", sourceCode, Types.VARCHAR)
                .addValue("productId", (long) product.productId())
                .addValue("sku", product.sku(), Types.VARCHAR)
                .addValue("partnerSku", product.partnerSKU(), Types.VARCHAR)
                .addValue("productName", product.productName(), Types.VARCHAR)
                .addValue("assetType", product.assetType(), Types.VARCHAR)
                .addValue("hasSerial", product.hasSerial())
                .addValue("hasExpiration", product.hasExpiration())
                .addValue("color", product.color(), Types.VARCHAR)
                .addValue("size", product.size(), Types.VARCHAR)
                .addValue("description", product.description(), Types.VARCHAR)
                .addValue("isActive", product.isActive())
                .addValue("revision", revision)
                .addValue("contentHash", contentHash, Types.VARCHAR);
    }

    public static MapSqlParameterSource category(long productRecordId, int position,
                                                InventoryProduct.Category category) {
        return new MapSqlParameterSource("productRecordId", productRecordId)
                .addValue("position", position)
                .addValue("categoryCode", category.categoryCode(), Types.VARCHAR)
                .addValue("categoryName", category.categoryName(), Types.VARCHAR);
    }
}
