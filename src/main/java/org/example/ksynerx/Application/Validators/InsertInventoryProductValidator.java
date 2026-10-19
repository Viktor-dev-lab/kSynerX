package org.example.ksynerx.Application.Validators;

import org.example.ksynerx.Application.Dto.Command.InsertInventoryProductCommand;

import java.util.regex.Pattern;

public class InsertInventoryProductValidator {
    private static final Pattern CONTENT_HASH = Pattern.compile("[0-9a-f]{64}");

    public void validate(InsertInventoryProductCommand command) {
        if (command == null || command.product() == null) {
            throw new IllegalArgumentException("product is required");
        }
        if (command.sourceCode() == null || command.sourceCode().isBlank()) {
            throw new IllegalArgumentException("sourceCode is required");
        }
        checkLength(command.sourceCode(), 100, "sourceCode");
        if (command.revision() <= 0) {
            throw new IllegalArgumentException("revision must be greater than zero");
        }
        if (command.contentHash() == null || !CONTENT_HASH.matcher(command.contentHash()).matches()) {
            throw new IllegalArgumentException("contentHash must contain 64 lowercase hexadecimal characters");
        }
        checkLength(command.product().assetType(), 50, "assetType");
    }

    private void checkLength(String value, int maximum, String field) {
        if (value != null && value.codePointCount(0, value.length()) > maximum) {
            throw new IllegalArgumentException(field + " must not exceed " + maximum + " characters");
        }
    }
}
