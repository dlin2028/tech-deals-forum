package com.techdeals.elasticsearch;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Document(indexName = "deals")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DealDocument {

    @Id
    private String id;

    @Field(type = FieldType.Text)
    private String title;

    @Field(type = FieldType.Text)
    private String description;

    @Field(type = FieldType.Keyword)
    private String url;

    @Field(type = FieldType.Double)
    private Double price;

    @Field(type = FieldType.Keyword)
    private String retailer;

    @Field(type = FieldType.Keyword)
    private String category;

    @Field(type = FieldType.Keyword)
    private String imageUrl;

    @Field(type = FieldType.Long)
    private Long postedById;

    @Field(type = FieldType.Date)
    private LocalDateTime createdAt;

    @Field(type = FieldType.Integer)
    private int upvoteCount;

    @Field(type = FieldType.Integer)
    private int downvoteCount;

    @Field(type = FieldType.Boolean)
    private boolean isActive;

    @Field(type = FieldType.Object)
    private Map<String, String> specs;

    @Field(type = FieldType.Keyword)
    private String cpuModel;

    @Field(type = FieldType.Keyword)
    private String gpuModel;

    @Field(type = FieldType.Integer)
    private int cpuBenchmark;

    @Field(type = FieldType.Integer)
    private int gpuBenchmark;

    @Field(type = FieldType.Integer)
    private int benchmarkScore;

    @Field(type = FieldType.Double)
    private double hotScore;

    @Field(type = FieldType.Keyword)
    private List<String> userTags;
}
