package org.example.ksynerx.Application.Validators;

import org.example.ksynerx.Application.Dto.Command.GetProductByIdCommand;
import org.example.ksynerx.Application.InvalidProductIdException;

public class GetProductByIdValidator {

    public void validate(GetProductByIdCommand command) {
        if (command == null || command.productId() == null
                || command.productId().isBlank() || command.productId().length() > 64) {
            throw new InvalidProductIdException();
        }
    }
}
