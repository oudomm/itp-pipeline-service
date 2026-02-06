package dev.oudom.pipeline.stream;

import ITP.CORE_BANKING.RECORD_XML.Envelope;
import lombok.extern.slf4j.Slf4j;
import org.apache.avro.generic.GenericRecord;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.Message;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.ByteArrayInputStream;
import java.util.function.Consumer;
import java.util.function.Function;

@Configuration
@Slf4j
public class StreamConfig {

    // Supplier for producing message into kafka topic
    // Function for processing message and send to destination kafka topic
    // Consumer for consuming message from kafka topic

    @Bean
    public Consumer<Message<Envelope>> captureEnvelope() {
        return record -> {
            System.out.println("Dbz envelope: " + record.getPayload().getAfter());
        };
    }

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

    // Oracle CDC Processor for RECORD_XML table
    @Bean
    public Consumer<GenericRecord> processOracleDbChanges() {
        return avroRecord -> {
            try {
                log.info("=== Oracle CDC Event Received ===");
                log.info("Full Avro Record: {}", avroRecord);

                // Debezium wraps the actual data in an envelope
                // The structure is: { "before": {...}, "after": {...}, "op": "c/u/d", ... }

                // Get the operation type
                String operation = avroRecord.get("op") != null ?
                        avroRecord.get("op").toString() : "unknown";
                log.info("Operation type: {}", operation);

                // Get the 'after' state (for INSERT and UPDATE)
                GenericRecord afterRecord = null;
                if (avroRecord.get("after") != null) {
                    afterRecord = (GenericRecord) avroRecord.get("after");
                }

                // Get the 'before' state (for UPDATE and DELETE)
                GenericRecord beforeRecord = null;
                if (avroRecord.get("before") != null) {
                    beforeRecord = (GenericRecord) avroRecord.get("before");
                }

                // Process based on operation
                if ("c".equals(operation) || "u".equals(operation)) {
                    // CREATE or UPDATE - use 'after'
                    if (afterRecord != null) {
                        RecordXml recordXml = extractRecordXml(afterRecord);
                        log.info("Converted to RecordXml: {}", recordXml);

                        // Parse the XML content
                        if (recordXml.getXmldata() != null) {
                            XmlData xmlData = parseXmlContent(recordXml.getXmldata());
                            log.info("Parsed XML Data - Name: {}, Role: {}",
                                    xmlData.getName(), xmlData.getRole());

                            // Here you can do whatever you need with the data
                            // e.g., save to another database, send to another service, etc.
                            processRecordXmlData(recordXml, xmlData, operation);
                        }
                    }
                } else if ("d".equals(operation)) {
                    // DELETE - use 'before'
                    if (beforeRecord != null) {
                        RecordXml recordXml = extractRecordXml(beforeRecord);
                        log.info("Deleted RecordXml: {}", recordXml);
                        handleDeletedRecord(recordXml);
                    }
                } else if ("r".equals(operation)) {
                    // READ (snapshot) - use 'after'
                    if (afterRecord != null) {
                        RecordXml recordXml = extractRecordXml(afterRecord);
                        log.info("Snapshot RecordXml: {}", recordXml);
                    }
                }

            } catch (Exception e) {
                log.error("Error processing Oracle CDC event", e);
                // Don't throw exception to avoid stopping the stream
                // In production, you might want to send to a dead letter queue
            }
        };
    }

    /**
     * Extract RecordXml from Avro GenericRecord
     */
    private RecordXml extractRecordXml(GenericRecord record) {
        RecordXml recordXml = new RecordXml();

        // Extract RECID
        if (record.get("RECID") != null) {
            recordXml.setRecid(record.get("RECID").toString());
        }

        // Extract XMLDATA (Oracle XMLTYPE comes as string)
        if (record.get("XMLDATA") != null) {
            recordXml.setXmldata(record.get("XMLDATA").toString());
        }

        return recordXml;
    }

    /**
     * Parse XML string content to XmlData object
     */
    private XmlData parseXmlContent(String xmlString) {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(new ByteArrayInputStream(xmlString.getBytes()));

            Element root = doc.getDocumentElement();

            String name = getElementValue(root, "name");
            String role = getElementValue(root, "role");

            return new XmlData(name, role);

        } catch (Exception e) {
            log.error("Error parsing XML content: {}", xmlString, e);
            return new XmlData(null, null);
        }
    }

    /**
     * Helper method to get element value from XML
     */
    private String getElementValue(Element parent, String tagName) {
        try {
            return parent.getElementsByTagName(tagName).item(0).getTextContent();
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Process the extracted and parsed data
     */
    private void processRecordXmlData(RecordXml recordXml, XmlData xmlData, String operation) {
        log.info("Processing {} operation for RECID: {}", operation, recordXml.getRecid());
        log.info("Name: {}, Role: {}", xmlData.getName(), xmlData.getRole());

        // Add your business logic here:
        // - Save to database
        // - Send to another service
        // - Transform and forward to another Kafka topic
        // - Update cache
        // etc.
    }

    /**
     * Handle deleted records
     */
    private void handleDeletedRecord(RecordXml recordXml) {
        log.info("Handling deletion for RECID: {}", recordXml.getRecid());

        // Add your deletion logic here:
        // - Remove from database
        // - Invalidate cache
        // - Send notification
        // etc.
    }
}