package org.example.ksynerx.Domain.Interface;

import org.example.ksynerx.Domain.Model.InventoryProduct;

import java.util.Optional;

public interface InventoryProductSnapshotRepository {
    Optional<CurrentProduct> findForUpdate(String sourceCode, int productId);
    void replace(long recordId, String sourceCode, long revision, String contentHash, InventoryProduct product);
    void advanceRevision(long recordId, long revision);

    record CurrentProduct(long recordId, long revision, String contentHash) { }
}
