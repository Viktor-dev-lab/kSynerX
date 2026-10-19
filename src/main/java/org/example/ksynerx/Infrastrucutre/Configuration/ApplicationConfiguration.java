package org.example.ksynerx.Infrastrucutre.Configuration;

import org.example.ksynerx.Application.Service.GetProductService;
import org.example.ksynerx.Application.Usecase.GetProductUseCase;
import org.example.ksynerx.Application.Validators.GetProductByIdValidator;
import org.example.ksynerx.Domain.Interface.ProductRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** Composition root nối application với infrastructure. */
@Configuration
public class ApplicationConfiguration {

    @Bean
    public GetProductByIdValidator getProductByIdValidator() {
        return new GetProductByIdValidator();
    }

    @Bean
    public GetProductUseCase getProductUseCase(
            ProductRepository productRepository,
            GetProductByIdValidator validator
    ) {
        return new GetProductUseCase(productRepository, validator);
    }

    @Bean
    public GetProductService getProductService(GetProductUseCase getProductUseCase) {
        return new GetProductService(getProductUseCase);
    }
}
