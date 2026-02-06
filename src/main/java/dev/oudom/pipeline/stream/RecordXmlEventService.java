package dev.oudom.pipeline.stream;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.oudom.pipeline.model.DebeziumEnvelope;
import dev.oudom.pipeline.model.RecordXml;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class RecordXmlEventService {

    private final ObjectMapper objectMapper;

    @KafkaListener(topics = "ITP.CORE_BANKING.RECORD_XML",
            groupId = "${spring.application.name}")
    public void listenRecordXmlEvent(ConsumerRecord<String, Object> record) {
        try {
            // Check for tombstone (null value)
            if (record.value() == null) {
                log.info("Received tombstone for key: {}", record.key());
                return;
            }
            log.info("listenRecordXmlEvent: {}", record.value());

            // Convert Avro GenericRecord to String, then deserialize
            String jsonString = record.value().toString();

            // Using object mapper to deserialize value (including XML string)
            DebeziumEnvelope<RecordXml> envelope = objectMapper.readValue(
                    jsonString,
                    new TypeReference<>() {}
            );

            log.info("mapped: {}", envelope);

            // Handle different operations using switch
            switch (envelope.getOp()) {
                case "r", "c" -> {
                    // Read (snapshot) or Create (insert)
                    log.info("Operation: {} (Insert/Snapshot)", envelope.getOp());
                    RecordXml after = envelope.getAfter();

                    if (after != null) {
                        log.info("RECID: {}", after.getRecid());
                        log.info("XML Data: {}", after.getXmlData());

                        if (after.getXmlData() != null) {
                            log.info("Name: {}", after.getXmlData().getName());
                            log.info("Role: {}", after.getXmlData().getRole());

                            // Your business logic for INSERT
                            System.out.println("Prepare to insert new record");
                            System.out.println(after.getXmlData().getName());
                        }
                    }
                }

                case "u" -> {
                    // Update
                    log.info("Operation: u (Update)");
                    RecordXml after = envelope.getAfter();

                    if (after != null) {
                        log.info("RECID: {}", after.getRecid());
                        log.info("XML Data: {}", after.getXmlData());

                        if (after.getXmlData() != null) {
                            log.info("Updated Name: {}", after.getXmlData().getName());
                            log.info("Updated Role: {}", after.getXmlData().getRole());

                            // Your business logic for UPDATE
                            System.out.println("Prepare to update existing record");
                            System.out.println("Updated: " + after.getXmlData().getName());
                        }
                    }
                }

                case "d" -> {
                    // Delete
                    log.info("Operation: d (Delete)");
                    RecordXml before = envelope.getBefore();

                    if (before != null) {
                        log.info("Deleted RECID: {}", before.getRecid());

                        // Your business logic for DELETE
                        System.out.println("Prepare to delete existing record");
                        System.out.println("Delete ID = " + before.getRecid());
                    }
                }

                default -> {
                    log.warn("Unknown operation type: {}", envelope.getOp());
                    throw new IllegalStateException("Invalid Operation: " + envelope.getOp());
                }
            }

        } catch (Exception e) {
            log.error("Error processing message", e);
        }
    }
}