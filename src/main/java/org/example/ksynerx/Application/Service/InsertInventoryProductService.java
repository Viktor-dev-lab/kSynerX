package org.example.ksynerx.Application.Service;

import org.example.ksynerx.Application.Dto.Command.InsertInventoryProductCommand;
import org.example.ksynerx.Application.Dto.Result.InsertInventoryProductResult;
import org.example.ksynerx.Application.Usecase.InsertInventoryProductCategoriesUseCase;
import org.example.ksynerx.Application.Usecase.InsertInventoryProductUseCase;
import org.example.ksynerx.Application.Validators.InsertInventoryProductValidator;
import org.example.ksynerx.Domain.Interface.InventoryProductInsertRepository;
import org.springframework.transaction.annotation.Transactional;

public class InsertInventoryProductService implements InsertInventoryProductUseCase {
    private final InventoryProductInsertRepository repository;
    private final InsertInventoryProductValidator validator;
    private final InsertInventoryProductCategoriesUseCase categories;

    public InsertInventoryProductService(InventoryProductInsertRepository repository,
                                         InsertInventoryProductValidator validator,
                                         InsertInventoryProductCategoriesUseCase categories) {
        this.repository = repository;
        this.validator = validator;
        this.categories = categories;
    }

    @Override
    @Transactional
    public InsertInventoryProductResult execute(InsertInventoryProductCommand command) {
        validator.validate(command);
        long productRecordId = repository.insertProduct(command.sourceCode(), command.revision(),
                command.contentHash(), command.product());
        int categoryCount = categories.execute(productRecordId, command.product().categories());
        return new InsertInventoryProductResult(productRecordId, categoryCount);
    }
}
