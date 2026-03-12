package com.techdeals.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Map;

@Data
public class DealRequest {
    @NotBlank
    private String title;
    private String description;
    private String url;
    @NotNull
    @Positive
    private BigDecimal price;
    private String retailer;
    private String category;
    private String imageUrl;
    private Map<String, String> specs;
    private String cpuModel;
    private String gpuModel;
}
