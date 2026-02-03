package dev.oudom.pipeline.stream;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.function.Consumer;
import java.util.function.Function;

@Configuration
public class StreamConfig {

    // Supplier for producing message into kafka topic
    // Function for processing message and send to destination kafka topic
    // Consumer for consuming message from kafka topic

    @Bean
    public Function<Product, Product> processProductDetail() {
        return product -> {
            System.out.println("Old product: " + product.getCode());
            System.out.println("Old product: " + product.getQty());

            // Processing
            product.setCode("ISTAD-" + product.getCode());

            // Producing
            return product;
        };
    }

    @Bean
    public Consumer<Product> processProduct() { // must be the same - processProduct
        return product -> {
            System.out.println("obj product: " + product.getCode());
            System.out.println("obj product: " + product.getQty());
        };
    }

    // A simple processor: Takes a string, makes it uppercase, and sends it on
    @Bean
    public Consumer<String> processMessage() {
        return input -> {
            System.out.println("Processing: " + input);
        };
    }


}