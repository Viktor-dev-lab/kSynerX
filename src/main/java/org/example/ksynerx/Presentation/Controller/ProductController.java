package org.example.ksynerx.Presentation.Controller;

import jakarta.validation.Valid;
import org.example.ksynerx.Application.Dto.Result.ProductResult;
import org.example.ksynerx.Application.Service.GetProductService;
import org.example.ksynerx.Presentation.Contract.ApiResponse;
import org.example.ksynerx.Presentation.Dto.Request.ProductRequest;
import org.example.ksynerx.Presentation.Dto.Response.ProductResponse;
import org.example.ksynerx.Presentation.Mapper.ProductResponseMapper;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/products")
public class ProductController {

    private final GetProductService getProductService;

    public ProductController(GetProductService getProductService) {
        this.getProductService = getProductService;
    }

    @PostMapping
    public ApiResponse<ProductResponse> getById(@Valid @RequestBody ProductRequest request) {
        ProductResult result = getProductService.getById(request.productId());
        ProductResponse response = ProductResponseMapper.from(result);
        return ApiResponse.success(request.requestId(), request.requestDateTime().toString(), request.channel(), response);
    }
}
