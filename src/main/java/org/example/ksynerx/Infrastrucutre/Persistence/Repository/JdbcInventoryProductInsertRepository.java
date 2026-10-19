package org.example.ksynerx.Infrastrucutre.Persistence.Repository;

import org.example.ksynerx.Domain.Interface.InventoryProductInsertRepository;
import org.example.ksynerx.Domain.Model.InventoryProduct;
import org.example.ksynerx.Infrastrucutre.Persistence.Mapper.InventoryProductInsertMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.Objects;

@Repository
public class JdbcInventoryProductInsertRepository implements InventoryProductInsertRepository {
    private static final String INSERT_PRODUCT = """
            INSERT INTO products (
                source_code, product_id, sku, partner_sku, product_name, asset_type,
                has_serial, has_expiration, color, size, description, is_active, revision, content_hash
            ) VALUES (
                :sourceCode, :productId, :sku, :partnerSku, :productName, :assetType,
                :hasSerial, :hasExpiration, :color, :size, :description, :isActive, :revision, :contentHash
            ) RETURNING id
            """;
    private static final String INSERT_CATEGORY = """
            INSERT INTO product_categories (product_record_id, position, category_code, category_name)
            VALUES (:productRecordId, :position, :categoryCode, :categoryName)
            """;

    private final NamedParameterJdbcTemplate jdbc;

    public JdbcInventoryProductInsertRepository(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public long insertProduct(String sourceCode, long revision, String contentHash, InventoryProduct product) {
        return Objects.requireNonNull(jdbc.queryForObject(INSERT_PRODUCT,
                InventoryProductInsertMapper.product(sourceCode, revision, contentHash, product), Long.class));
    }

    @Override
    public void insertCategory(long productRecordId, int position, InventoryProduct.Category category) {
        jdbc.update(INSERT_CATEGORY, InventoryProductInsertMapper.category(productRecordId, position, category));
    }
}
