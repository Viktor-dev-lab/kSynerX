package org.example.ksynerx.Infrastrucutre.Configuration;

import org.example.ksynerx.Application.Service.InsertInventoryProductService;
import org.example.ksynerx.Application.Usecase.InsertInventoryProductCategoriesUseCase;
import org.example.ksynerx.Application.Validators.InsertInventoryProductValidator;
import org.example.ksynerx.Domain.Interface.InventoryProductInsertRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class InventoryProductInsertConfiguration {
    @Bean
    public InsertInventoryProductValidator insertInventoryProductValidator() {
        return new InsertInventoryProductValidator();
    }

    @Bean
    public InsertInventoryProductCategoriesUseCase insertInventoryProductCategoriesUseCase(
            InventoryProductInsertRepository repository) {
        return new InsertInventoryProductCategoriesUseCase(repository);
    }

    @Bean
    public InsertInventoryProductService insertInventoryProductService(
            InventoryProductInsertRepository repository, InsertInventoryProductValidator validator,
            InsertInventoryProductCategoriesUseCase categories) {
        return new InsertInventoryProductService(repository, validator, categories);
    }
}
