package com.techdeals.model;

import jakarta.persistence.*;
import lombok.*;

import java.util.HashMap;
import java.util.Map;

@Entity
@Table(name = "components")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Component {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    private String brand;

    @Enumerated(EnumType.STRING)
    private ComponentType type;

    private int benchmarkScore;

    private String benchmarkSource;

    @ElementCollection
    @CollectionTable(name = "component_specs", joinColumns = @JoinColumn(name = "component_id"))
    @MapKeyColumn(name = "spec_key")
    @Column(name = "spec_value")
    @Builder.Default
    private Map<String, String> specs = new HashMap<>();
}
