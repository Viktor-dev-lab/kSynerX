package org.example.ksynerx.Infrastrucutre;

import org.example.ksynerx.Infrastrucutre.Configuration.InventoryFeignConfiguration;
import org.example.ksynerx.Presentation.Dto.Response.InventoryProductResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import java.util.List;

@FeignClient(
    name = "inventory",
    url = "${integrations.inventory.base-url}",
    configuration = InventoryFeignConfiguration.class
)
public interface InventoryFeignClient {
    @GetMapping(value = "/api/v1/Products", produces = "application/json")
    List<InventoryProductResponse> getProductList();

    @GetMapping(value = "/api/v1/Products/{productId}", produces = "application/json")
    InventoryProductResponse getProductDetail(@PathVariable("productId") int productId);
}
