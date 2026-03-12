package com.techdeals.dto;

import com.techdeals.model.ComponentType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ComponentDto {
    private Long id;
    private String name;
    private String brand;
    private ComponentType type;
    private int benchmarkScore;
    private String benchmarkSource;
    private Map<String, String> specs;
}
