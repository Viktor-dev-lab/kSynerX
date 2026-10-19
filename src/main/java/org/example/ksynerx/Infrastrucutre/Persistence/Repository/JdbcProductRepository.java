package org.example.ksynerx.Infrastrucutre.Persistence.Repository;

import org.example.ksynerx.Domain.Interface.ProductRepository;
import org.example.ksynerx.Domain.Model.Product;
import org.example.ksynerx.Infrastrucutre.Persistence.Entity.ProductEntity;
import org.example.ksynerx.Infrastrucutre.Persistence.Mapper.ProductPersistenceMapper;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class JdbcProductRepository implements ProductRepository {

    private static final String FIND_BY_ID_SQL = """
            SELECT product_id, name, price
            FROM products
            WHERE product_id = ?
            """;

    private final JdbcTemplate jdbcTemplate;

    public JdbcProductRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Optional<Product> findById(String productId) {
        try {
            ProductEntity entity = jdbcTemplate.queryForObject(
                    FIND_BY_ID_SQL,
                    (resultSet, rowNumber) -> ProductPersistenceMapper.fromResultSet(resultSet),
                    productId
            );

            return Optional.ofNullable(entity)
                    .map(ProductPersistenceMapper::toDomain);
        } catch (EmptyResultDataAccessException exception) {
            return Optional.empty();
        }
    }
}
