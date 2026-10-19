package org.example.inventory.Domain.Interface;

import org.example.inventory.Domain.Model.VietfulProduct;

import java.util.List;
import java.util.Optional;

public interface VietfulProductSource {
    List<VietfulProduct> getProducts();
    Optional<VietfulProduct> getProduct(int productId);
}
