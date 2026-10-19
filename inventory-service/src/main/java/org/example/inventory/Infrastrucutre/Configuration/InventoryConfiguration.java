package org.example.inventory.Infrastrucutre.Configuration;

import org.example.inventory.Application.Service.GetVietfulProductsService;
import org.example.inventory.Application.Usecase.GetVietfulProductsUseCase;
import org.example.inventory.Domain.Interface.VietfulProductSource;
import org.example.inventory.Infrastrucutre.FakerVietfulProductSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class InventoryConfiguration {
    @Bean
    public VietfulProductSource vietfulProductSource() {
        return new FakerVietfulProductSource();
    }

    @Bean
    public GetVietfulProductsUseCase getVietfulProductsUseCase(VietfulProductSource source) {
        return new GetVietfulProductsUseCase(source);
    }

    @Bean
    public GetVietfulProductsService getVietfulProductsService(GetVietfulProductsUseCase useCase) {
        return new GetVietfulProductsService(useCase);
    }
}
