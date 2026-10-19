package org.example.ksynerx.Infrastrucutre.Persistence.Mapper;

import org.example.ksynerx.Domain.Model.Product;
import org.example.ksynerx.Infrastrucutre.Persistence.Entity.ProductEntity;

import java.sql.ResultSet;
import java.sql.SQLException;

public final class ProductPersistenceMapper {

    private ProductPersistenceMapper() {
    }

    public static ProductEntity fromResultSet(ResultSet resultSet) throws SQLException {
        return new ProductEntity(
                resultSet.getString("product_id"),
                resultSet.getString("name"),
                resultSet.getBigDecimal("price")
        );
    }

    public static Product toDomain(ProductEntity entity) {
        return new Product(entity.productId(), entity.name(), entity.price());
    }
}
