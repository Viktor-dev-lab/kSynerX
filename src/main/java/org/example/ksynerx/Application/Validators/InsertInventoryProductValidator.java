package org.example.ksynerx.Application.Validators;

import org.example.ksynerx.Application.Dto.Command.InsertInventoryProductCommand;
import org.example.ksynerx.Domain.Model.InventoryProduct;

import java.util.regex.Pattern;

public class InsertInventoryProductValidator {
    private static final Pattern CONTENT_HASH = Pattern.compile("[0-9a-f]{64}");

    public void validate(InsertInventoryProductCommand command) {
        if (command == null) {
            throw new IllegalArgumentException("product is required");
        }
        validateProduct(command.sourceCode(), command.product());
        if (command.revision() <= 0) {
            throw new IllegalArgumentException("revision must be greater than zero");
        }
        if (command.revision() != command.product().revision()) {
            throw new IllegalArgumentException("revision must match the source snapshot");
        }
        if (command.contentHash() == null || !CONTENT_HASH.matcher(command.contentHash()).matches()) {
            throw new IllegalArgumentException("contentHash must contain 64 lowercase hexadecimal characters");
        }
    }

    public void validateProduct(String sourceCode, InventoryProduct product) {
        if (product == null) {
            throw new IllegalArgumentException("product is required");
        }
        if (product.revision() <= 0) {
            throw new IllegalArgumentException("source revision must be greater than zero");
        }
        if (sourceCode == null || sourceCode.isBlank()) {
            throw new IllegalArgumentException("sourceCode is required");
        }
        checkLength(sourceCode, 100, "sourceCode");
        checkLength(product.assetType(), 50, "assetType");
    }

    private void checkLength(String value, int maximum, String field) {
        if (value != null && value.codePointCount(0, value.length()) > maximum) {
            throw new IllegalArgumentException(field + " must not exceed " + maximum + " characters");
        }
    }
}
