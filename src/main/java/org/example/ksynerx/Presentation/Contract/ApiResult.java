package org.example.ksynerx.Presentation.Contract;

public record ApiResult(String responseCode, String description) {

    public static ApiResult success() {
        return of(APIResultCode.SUCCESS);
    }

    public static ApiResult of(APIResultCode code) {
        return new ApiResult(code.getCode(), code.getMessage());
    }
}
