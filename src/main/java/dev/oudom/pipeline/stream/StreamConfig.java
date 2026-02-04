package dev.oudom.pipeline.stream;

import lombok.extern.slf4j.Slf4j;
import org.apache.avro.generic.GenericRecord;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.function.Consumer;
import java.util.function.Function;

@Configuration
@Slf4j
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

    // CDC Processor - handles Avro messages from Debezium
    @Bean
    public Function<GenericRecord, Product> processDbChanges() {
        return avroRecord -> {
            try {
                log.info("=== CDC Event Received ===");
                log.info("Avro Record: {}", avroRecord);

                String code = avroRecord.get("code") != null ? avroRecord.get("code").toString() : null;
                Integer qty = avroRecord.get("qty") != null ? (Integer) avroRecord.get("qty") : null;

                Product product = new Product();
                product.setCode(code);
                product.setQty(qty);

                log.info("Converted to Product: {}", product);
                return product;
            } catch (Exception e) {
                log.error("Error processing CDC event", e);
                throw new RuntimeException("Failed to process CDC event", e);
            }
        };
    }
}