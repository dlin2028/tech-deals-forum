package com.techdeals.controller;

import com.techdeals.dto.ComponentDto;
import com.techdeals.model.Component;
import com.techdeals.repository.ComponentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/components")
@RequiredArgsConstructor
public class ComponentController {

    private final ComponentRepository componentRepository;

    @GetMapping("/search")
    public ResponseEntity<List<ComponentDto>> searchComponents(@RequestParam String q) {
        List<ComponentDto> results = componentRepository.findByNameContainingIgnoreCase(q)
            .stream()
            .map(this::toDto)
            .collect(Collectors.toList());
        return ResponseEntity.ok(results);
    }

    @PostMapping
    public ResponseEntity<ComponentDto> createComponent(@RequestBody ComponentDto dto) {
        Component component = Component.builder()
            .name(dto.getName())
            .brand(dto.getBrand())
            .type(dto.getType())
            .benchmarkScore(dto.getBenchmarkScore())
            .benchmarkSource(dto.getBenchmarkSource())
            .specs(dto.getSpecs() != null ? dto.getSpecs() : new HashMap<>())
            .build();
        Component saved = componentRepository.save(component);
        return ResponseEntity.status(HttpStatus.CREATED).body(toDto(saved));
    }

    private ComponentDto toDto(Component c) {
        return ComponentDto.builder()
            .id(c.getId())
            .name(c.getName())
            .brand(c.getBrand())
            .type(c.getType())
            .benchmarkScore(c.getBenchmarkScore())
            .benchmarkSource(c.getBenchmarkSource())
            .specs(c.getSpecs())
            .build();
    }
}
