package org.example.inventory.Application.Usecase;

import org.example.inventory.Application.ProductNotFoundException;
import org.example.inventory.Domain.Interface.VietfulProductSource;
import org.example.inventory.Domain.Model.VietfulProduct;

import java.util.List;

public class GetVietfulProductsUseCase {
    private final VietfulProductSource source;

    public GetVietfulProductsUseCase(VietfulProductSource source) {
        this.source = source;
    }

    public List<VietfulProduct> getList() {
        return source.getProducts();
    }

    public VietfulProduct getDetail(int productId) {
        return source.getProduct(productId)
                .orElseThrow(() -> new ProductNotFoundException(Integer.toString(productId)));
    }
}
