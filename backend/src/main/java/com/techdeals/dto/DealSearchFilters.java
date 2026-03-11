package com.techdeals.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class DealSearchFilters {
    private String query;
    private String category;
    private BigDecimal minPrice;
    private BigDecimal maxPrice;
    private String cpuBrand;
    private String gpuBrand;
    private int minBenchmark;
    private String sort;
}
