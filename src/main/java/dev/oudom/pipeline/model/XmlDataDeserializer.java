package dev.oudom.pipeline.model;

import dev.oudom.pipeline.deserializer.XmlStringDeserializer;

public class XmlDataDeserializer extends XmlStringDeserializer<XmlData> {
    public XmlDataDeserializer() {
        super(XmlData.class);
    }
}
