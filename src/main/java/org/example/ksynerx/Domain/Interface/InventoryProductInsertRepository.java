package org.example.ksynerx.Domain.Interface;

import org.example.ksynerx.Domain.Model.InventoryProduct;

public interface InventoryProductInsertRepository {
    long insertProduct(String sourceCode, long revision, String contentHash, InventoryProduct product);
    void insertCategory(long productRecordId, int position, InventoryProduct.Category category);
}
