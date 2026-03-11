package com.techdeals.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DealResponse {
    private Long id;
    private String title;
    private String description;
    private String url;
    private BigDecimal price;
    private String retailer;
    private String category;
    private String imageUrl;
    private Long postedById;
    private String postedByUsername;
    private LocalDateTime createdAt;
    private int upvoteCount;
    private int downvoteCount;
    private boolean isActive;
    private Map<String, String> specs;
    private String cpuModel;
    private String gpuModel;
    private int benchmarkScore;
    private int cpuBenchmark;
    private int gpuBenchmark;
    private double hotScore;
    private String userVote;
}
