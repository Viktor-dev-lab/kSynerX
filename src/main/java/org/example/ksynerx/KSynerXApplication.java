package org.example.ksynerx;

import org.example.ksynerx.Infrastrucutre.InventoryFeignClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@EnableFeignClients(clients = InventoryFeignClient.class)
public class KSynerXApplication {

    public static void main(String[] args) {
        SpringApplication.run(KSynerXApplication.class, args);
    }

}
