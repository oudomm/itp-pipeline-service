package dev.oudom.pipeline.config;

import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientBuilderConfig {

    @Bean
    @LoadBalanced // !important for loadbalance
    public WebClient.Builder loadBalancedWebClientBuilder() {
        return WebClient.builder();
    }
}
