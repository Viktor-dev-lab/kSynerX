package org.example.ksynerx.application.service;

import org.example.ksynerx.Application.Dto.Result.ProductResult;
import org.example.ksynerx.Application.InvalidProductIdException;
import org.example.ksynerx.Application.ProductNotFoundException;
import org.example.ksynerx.Application.Service.GetProductService;
import org.example.ksynerx.Application.Usecase.GetProductUseCase;
import org.example.ksynerx.Application.Validators.GetProductByIdValidator;
import org.example.ksynerx.Domain.Interface.ProductRepository;
import org.example.ksynerx.Domain.Model.Product;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class GetProductServiceTest {

    @Test
    void shouldReturnProductWhenItExists() {
        Product product = new Product("PRD001", "Bàn phím cơ", new BigDecimal("1290000.00"));
        ProductResult expected = new ProductResult("PRD001", "Bàn phím cơ", new BigDecimal("1290000.00"));
        ProductRepository repository = productId -> {
            assertEquals("PRD001", productId);
            return Optional.of(product);
        };
        GetProductService service = createService(repository);

        ProductResult actual = service.getById("PRD001");

        assertEquals(expected, actual);
    }

    @Test
    void shouldThrowWhenProductDoesNotExist() {
        ProductRepository repository = id -> Optional.empty();
        GetProductService service = createService(repository);

        ProductNotFoundException exception = assertThrows(
                ProductNotFoundException.class,
                () -> service.getById("PRD999")
        );

        assertEquals("Không tìm thấy sản phẩm với productId = PRD999", exception.getMessage());
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "\t", "\n"})
    void shouldRejectMissingOrBlankProductIdBeforeQueryingRepository(String productId) {
        GetProductService service = createService(id -> {
            throw new AssertionError("Không được truy vấn repository khi productId không hợp lệ");
        });

        assertThrows(InvalidProductIdException.class, () -> service.getById(productId));
    }

    @Test
    void shouldRejectProductIdLongerThanDatabaseLimit() {
        GetProductService service = createService(id -> {
            throw new AssertionError("Không được truy vấn repository khi productId không hợp lệ");
        });

        assertThrows(InvalidProductIdException.class, () -> service.getById("P".repeat(65)));
    }

    private GetProductService createService(ProductRepository repository) {
        return new GetProductService(new GetProductUseCase(repository, new GetProductByIdValidator()));
    }
}
