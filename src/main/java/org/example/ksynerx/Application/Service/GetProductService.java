package org.example.ksynerx.Application.Service;

import org.example.ksynerx.Application.Dto.Command.GetProductByIdCommand;
import org.example.ksynerx.Application.Dto.Result.ProductResult;
import org.example.ksynerx.Application.Usecase.GetProductUseCase;

/** Cung cấp thao tác lấy sản phẩm cho controller thông qua use case. */
public class GetProductService {

    private final GetProductUseCase getProductUseCase;

    public GetProductService(GetProductUseCase getProductUseCase) {
        this.getProductUseCase = getProductUseCase;
    }

    public ProductResult getById(String productId) {
        return getProductUseCase.execute(new GetProductByIdCommand(productId));
    }
}
