package com.techdeals.controller;

import com.techdeals.dto.DealRequest;
import com.techdeals.dto.DealResponse;
import com.techdeals.dto.DealSearchFilters;
import com.techdeals.service.DealService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/deals")
@RequiredArgsConstructor
public class DealController {

    private final DealService dealService;

    @GetMapping
    public ResponseEntity<Page<DealResponse>> getDeals(
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size);
        DealSearchFilters filters = new DealSearchFilters();
        return ResponseEntity.ok(dealService.searchDeals(filters, pageable));
    }

    @PostMapping
    public ResponseEntity<DealResponse> createDeal(
        @Valid @RequestBody DealRequest request,
        @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(dealService.createDeal(request, userDetails.getUsername()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<DealResponse> getDeal(
        @PathVariable Long id,
        @AuthenticationPrincipal UserDetails userDetails) {
        String username = userDetails != null ? userDetails.getUsername() : null;
        return ResponseEntity.ok(dealService.getDealById(id, username));
    }

    @GetMapping("/search")
    public ResponseEntity<Page<DealResponse>> searchDeals(
        @RequestParam(required = false) String query,
        @RequestParam(required = false) String category,
        @RequestParam(required = false) BigDecimal minPrice,
        @RequestParam(required = false) BigDecimal maxPrice,
        @RequestParam(required = false) String cpuBrand,
        @RequestParam(required = false) String gpuBrand,
        @RequestParam(defaultValue = "0") int minBenchmark,
        @RequestParam(required = false) String sort,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "20") int size) {
        DealSearchFilters filters = new DealSearchFilters();
        filters.setQuery(query);
        filters.setCategory(category);
        filters.setMinPrice(minPrice);
        filters.setMaxPrice(maxPrice);
        filters.setCpuBrand(cpuBrand);
        filters.setGpuBrand(gpuBrand);
        filters.setMinBenchmark(minBenchmark);
        filters.setSort(sort);
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(dealService.searchDeals(filters, pageable));
    }

    @GetMapping("/hot")
    public ResponseEntity<List<DealResponse>> getHotDeals(
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(dealService.getHotDeals(pageable));
    }

    @GetMapping("/feed")
    public ResponseEntity<Page<DealResponse>> getFeed(
        @AuthenticationPrincipal UserDetails userDetails,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(dealService.getPersonalizedFeed(userDetails.getUsername(), pageable));
    }
}
