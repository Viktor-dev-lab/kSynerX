package org.example.ksynerx.Infrastrucutre.Persistence.Repository;

import org.example.ksynerx.Domain.Interface.InventoryProductInsertRepository;
import org.example.ksynerx.Domain.Interface.InventoryProductSnapshotRepository;
import org.example.ksynerx.Domain.Model.InventoryProduct;
import org.example.ksynerx.Infrastrucutre.Persistence.Mapper.InventoryProductInsertMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class JdbcInventoryProductSnapshotRepository implements InventoryProductSnapshotRepository {
    private final NamedParameterJdbcTemplate jdbc;
    private final InventoryProductInsertRepository inserts;

    public JdbcInventoryProductSnapshotRepository(NamedParameterJdbcTemplate jdbc, InventoryProductInsertRepository inserts) {
        this.jdbc = jdbc;
        this.inserts = inserts;
    }

    @Override
    public Optional<CurrentProduct> findForUpdate(String sourceCode, int productId) {
        return jdbc.query("""
                SELECT id, revision, content_hash FROM products
                WHERE source_code = :sourceCode AND product_id = :productId FOR UPDATE
                """, new MapSqlParameterSource("sourceCode", sourceCode).addValue("productId", (long) productId),
                (row, index) -> new CurrentProduct(row.getLong("id"), row.getLong("revision"),
                        row.getString("content_hash"))).stream().findFirst();
    }

    @Override
    public void replace(long recordId, String sourceCode, long revision, String contentHash, InventoryProduct product) {
        var parameters = InventoryProductInsertMapper.product(sourceCode, revision, contentHash, product)
                .addValue("recordId", recordId);
        jdbc.update("""
                UPDATE products SET sku = :sku, partner_sku = :partnerSku, product_name = :productName,
                    asset_type = :assetType, has_serial = :hasSerial, has_expiration = :hasExpiration,
                    color = :color, size = :size, description = :description, is_active = :isActive,
                    revision = :revision, content_hash = :contentHash, updated_at = clock_timestamp()
                WHERE id = :recordId
                """, parameters);
        jdbc.update("DELETE FROM product_categories WHERE product_record_id = :recordId", parameters);
        for (int position = 0; position < product.categories().size(); position++) {
            inserts.insertCategory(recordId, position, product.categories().get(position));
        }
    }

    @Override
    public void advanceRevision(long recordId, long revision) {
        jdbc.update("UPDATE products SET revision = :revision, updated_at = clock_timestamp() WHERE id = :recordId",
                new MapSqlParameterSource("recordId", recordId).addValue("revision", revision));
    }
}
