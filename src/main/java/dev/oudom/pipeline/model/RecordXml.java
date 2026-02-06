package dev.oudom.pipeline.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import dev.oudom.pipeline.deserializer.XmlStringDeserializer;
import lombok.Data;

@Data
public class RecordXml {

    @JsonProperty("RECID")
    private String recid;

    @JsonProperty("XMLDATA")
    @JsonDeserialize(using = XmlDataDeserializer.class)
    private XmlData xmlData;
}