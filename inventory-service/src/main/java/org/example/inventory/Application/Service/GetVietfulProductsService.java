package org.example.inventory.Application.Service;

import org.example.inventory.Application.Usecase.GetVietfulProductsUseCase;
import org.example.inventory.Domain.Model.VietfulProduct;

import java.util.List;

public class GetVietfulProductsService {
    private final GetVietfulProductsUseCase useCase;

    public GetVietfulProductsService(GetVietfulProductsUseCase useCase) {
        this.useCase = useCase;
    }

    public List<VietfulProduct> getList() {
        return useCase.getList();
    }

    public VietfulProduct getDetail(int productId) {
        return useCase.getDetail(productId);
    }
}
