package dev.oudom.pipeline.model;

import lombok.Data;

@Data
public class Source {
    private String version;
    private String connector;
    private String name;
    private Long ts_ms;
    private String snapshot;
    private String db;
    private String schema;
    private String table;
    private String scn;
}
