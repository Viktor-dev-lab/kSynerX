package org.example.ksynerx.Infrastrucutre.Configuration;

import feign.Logger;
import feign.Request;
import feign.Retryer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import java.util.concurrent.TimeUnit;

public class InventoryFeignConfiguration {
    @Bean
    public Request.Options inventoryOptions(
            @Value("${integrations.inventory.connect-timeout-ms:3000}") int connectTimeout,
            @Value("${integrations.inventory.read-timeout-ms:10000}") int readTimeout) {
        if (connectTimeout <= 0 || readTimeout <= 0) throw new IllegalArgumentException("Timeout phải lớn hơn 0");
        return new Request.Options(connectTimeout, TimeUnit.MILLISECONDS, readTimeout, TimeUnit.MILLISECONDS, false);
    }

    @Bean
    public Retryer inventoryRetryer() { return Retryer.NEVER_RETRY; }

    @Bean
    public Logger.Level inventoryLogLevel() { return Logger.Level.NONE; }
}
