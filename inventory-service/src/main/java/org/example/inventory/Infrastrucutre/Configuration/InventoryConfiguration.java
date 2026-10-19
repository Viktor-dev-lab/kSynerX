package org.example.inventory.Infrastrucutre.Configuration;

import org.example.inventory.Application.Service.GetVietfulProductsService;
import org.example.inventory.Application.Usecase.GetVietfulProductsUseCase;
import org.example.inventory.Domain.Interface.VietfulProductSource;
import org.example.inventory.Infrastrucutre.FakerVietfulProductSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.beans.factory.annotation.Value;

import java.nio.file.Path;

@Configuration
public class InventoryConfiguration {
    @Bean
    public VietfulProductSource vietfulProductSource(@Value("${inventory.state-file}") String stateFile,
                                                     @Value("${inventory.seed}") long seed) {
        return new FakerVietfulProductSource(Path.of(stateFile), seed);
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
