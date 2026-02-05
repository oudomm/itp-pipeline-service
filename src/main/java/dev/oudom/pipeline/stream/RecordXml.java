package dev.oudom.pipeline.stream;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RecordXml {
    private String recid;
    private String xmldata;  // XML will come as string from Avro

    // Helper method to parse XML if needed
    public XmlData parseXmlData() {
        // You can add XML parsing logic here if needed
        // For now, we'll keep the raw XML string
        return null;
    }
}
