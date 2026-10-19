package org.example.ksynerx.Presentation.Controller;

import org.example.ksynerx.Infrastrucutre.InventoryFeignClient;
import org.example.ksynerx.Infrastrucutre.InventoryProductMapper;
import org.example.ksynerx.Presentation.Contract.ApiResponse;
import org.example.ksynerx.Presentation.Dto.Request.InventoryProductRequest;
import org.example.ksynerx.Presentation.Dto.Response.InventoryProductResponse;
import org.example.ksynerx.Presentation.Mapper.InventoryProductResponseMapper;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/inventory/products")
public class InventoryProductController {
    private final InventoryFeignClient inventory;

    public InventoryProductController(InventoryFeignClient inventory) {
        this.inventory = inventory;
    }

    @GetMapping
    public ApiResponse<List<InventoryProductResponse>> getProducts() {
        return ApiResponse.success(inventory.getProductList().stream()
                .map(InventoryProductMapper::fromResponse)
                .map(InventoryProductResponseMapper::toResponse)
                .toList());
    }

    @GetMapping("/{productId}")
    public ApiResponse<InventoryProductResponse> getProduct(
            @PathVariable("productId") InventoryProductRequest request) {
        var product = InventoryProductMapper.fromResponse(inventory.getProductDetail(request.productId()));
        return ApiResponse.success(InventoryProductResponseMapper.toResponse(product));
    }
}
