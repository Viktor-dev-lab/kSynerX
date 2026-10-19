package org.example.ksynerx.Application;

public class InvalidProductIdException extends RuntimeException {

    public InvalidProductIdException() {
        super("productId phải là chuỗi không rỗng và không vượt quá 64 ký tự");
    }
}
