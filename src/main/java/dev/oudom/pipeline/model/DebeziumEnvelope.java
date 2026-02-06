package dev.oudom.pipeline.model;

import lombok.Data;

import javax.xml.transform.Source;

@Data
public class DebeziumEnvelope<T> {
    private String op;  // "c", "r", "u", "d"
    private T before;
    private T after;
}
