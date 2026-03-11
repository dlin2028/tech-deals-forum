package com.techdeals.service;

import com.techdeals.model.Deal;
import com.techdeals.model.Component;
import com.techdeals.repository.ComponentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class BenchmarkService {

    private final ComponentRepository componentRepository;

    public void enrichDealWithBenchmarks(Deal deal) {
        if (deal.getCpuModel() != null && !deal.getCpuModel().isBlank()) {
            getComponentBenchmark(deal.getCpuModel()); // warm the cache
        }
        if (deal.getGpuModel() != null && !deal.getGpuModel().isBlank()) {
            getComponentBenchmark(deal.getGpuModel()); // warm the cache
        }
    }

    @Cacheable("benchmarks")
    public int getComponentBenchmark(String name) {
        if (name == null || name.isBlank()) return 0;
        return componentRepository.findByNameIgnoreCase(name)
            .map(Component::getBenchmarkScore)
            .orElse(0);
    }
}
