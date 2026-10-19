package org.example.ksynerx.Application.Usecase;

import org.example.ksynerx.Domain.Interface.InventoryProductInsertRepository;
import org.example.ksynerx.Domain.Model.InventoryProduct.Category;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public class InsertInventoryProductCategoriesUseCase {
    private final InventoryProductInsertRepository repository;

    public InsertInventoryProductCategoriesUseCase(InventoryProductInsertRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public int execute(long productRecordId, List<Category> categories) {
        if (productRecordId <= 0) throw new IllegalArgumentException("productRecordId must be greater than zero");
        List<Category> values = List.copyOf(categories);
        for (int position = 0; position < values.size(); position++) {
            repository.insertCategory(productRecordId, position, values.get(position));
        }
        return values.size();
    }
}
