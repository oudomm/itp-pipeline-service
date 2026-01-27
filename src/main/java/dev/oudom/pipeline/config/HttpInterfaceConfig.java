package dev.oudom.pipeline.config;

import dev.oudom.pipeline.client.AccountClient;
import dev.oudom.pipeline.client.JsonPlaceholderClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class HttpInterfaceConfig {

    @Bean
    public JsonPlaceholderClient jsonPlaceholderClient(HttpInterfaceFactory factory) {
        return factory.createClient(
                "https://jsonplaceholder.typicode.com",
                JsonPlaceholderClient.class);
    }

    @Bean
    public AccountClient accountClient(HttpInterfaceFactory factory) {
        return factory.createClient(
                "http://localhost:20261",
                AccountClient.class);
    }

}
