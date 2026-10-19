package org.example.ksynerx.Application.Usecase;

import org.example.ksynerx.Application.Dto.Command.GetProductByIdCommand;
import org.example.ksynerx.Application.Dto.Result.ProductResult;
import org.example.ksynerx.Application.ProductNotFoundException;
import org.example.ksynerx.Application.Validators.GetProductByIdValidator;
import org.example.ksynerx.Domain.Interface.ProductRepository;
import org.example.ksynerx.Domain.Model.Product;

public class GetProductUseCase {

    private final ProductRepository productRepository;
    private final GetProductByIdValidator validator;

    public GetProductUseCase(
            ProductRepository productRepository,
            GetProductByIdValidator validator
    ) {
        this.productRepository = productRepository;
        this.validator = validator;
    }

    public ProductResult execute(GetProductByIdCommand command) {
        validator.validate(command);

        Product product = productRepository.findById(command.productId())
                .orElseThrow(() -> new ProductNotFoundException(command.productId()));

        return ProductResult.from(product);
    }
}
