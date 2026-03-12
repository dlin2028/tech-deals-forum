package com.techdeals.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Entity
@Table(name = "deals")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Deal {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    private String url;

    private BigDecimal price;

    private String retailer;

    private String category;

    private String imageUrl;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "posted_by_id")
    private User postedBy;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @Builder.Default
    private int upvoteCount = 0;

    @Builder.Default
    private int downvoteCount = 0;

    @Builder.Default
    private boolean isActive = true;

    @ElementCollection
    @CollectionTable(name = "deal_specs", joinColumns = @JoinColumn(name = "deal_id"))
    @MapKeyColumn(name = "spec_key")
    @Column(name = "spec_value")
    @Builder.Default
    private Map<String, String> specs = new HashMap<>();

    private String cpuModel;

    private String gpuModel;

    @Builder.Default
    private double hotScore = 0.0;

    public void recalculateHotScore() {
        if (this.createdAt == null) {
            this.hotScore = 0.0;
            return;
        }
        long hoursSincePost = Duration.between(this.createdAt, LocalDateTime.now()).toHours();
        this.hotScore = (this.upvoteCount - this.downvoteCount) / Math.pow(hoursSincePost + 2, 1.8);
    }
}
