package org.example.inventory.Presentation.Controller;

import org.example.inventory.Application.Service.GetVietfulProductsService;
import org.example.inventory.Presentation.Dto.Response.VietfulProductResponse;
import org.example.inventory.Presentation.Mapper.VietfulProductResponseMapper;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping(value = "/api/v1/Products", produces = MediaType.APPLICATION_JSON_VALUE)
public class InventoryProductController {
    private final GetVietfulProductsService service;

    public InventoryProductController(GetVietfulProductsService service) {
        this.service = service;
    }

    @GetMapping
    public List<VietfulProductResponse> getList() {
        return service.getList().stream().map(VietfulProductResponseMapper::toResponse).toList();
    }

    @GetMapping("/{productId}")
    public VietfulProductResponse getDetail(@PathVariable("productId") int productId) {
        return VietfulProductResponseMapper.toResponse(service.getDetail(productId));
    }
}
