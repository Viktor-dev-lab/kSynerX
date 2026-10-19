package org.example.ksynerx.Presentation.Controller;

import org.example.ksynerx.Infrastrucutre.InventoryFeignClient;
import org.example.ksynerx.Presentation.Contract.ApiResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/inventory/products")
public class InventoryProductController {
    private final InventoryFeignClient inventory;

    public InventoryProductController(InventoryFeignClient inventory) {
        this.inventory = inventory;
    }

    @GetMapping
    public ApiResponse<List<Map<String, Object>>> getProducts() {
        return ApiResponse.success(inventory.getProductList());
    }

    @GetMapping("/{productId}")
    public ApiResponse<Map<String, Object>> getProduct(@PathVariable long productId) {
        return ApiResponse.success(inventory.getProductDetail(productId));
    }
}
