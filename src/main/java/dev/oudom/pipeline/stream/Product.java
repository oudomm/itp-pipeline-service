package dev.oudom.pipeline.stream;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class Product {
    private String code;
    private Integer qty;
}
