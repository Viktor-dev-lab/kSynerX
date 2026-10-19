package org.example.callback;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(CallbackClientProperties.class)
public class CallbackClientApplication {
    public static void main(String[] args) {
        try (var application = SpringApplication.run(CallbackClientApplication.class, args)) {
            // A command-line client: close the context after the runner finishes.
        }
    }
}
