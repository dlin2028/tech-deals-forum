package com.techdeals.controller;

import com.techdeals.dto.PriceHistoryPoint;
import com.techdeals.service.PriceHistoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/deals/{id}/price-history")
@RequiredArgsConstructor
public class PriceHistoryController {

    private final PriceHistoryService priceHistoryService;

    @GetMapping
    public ResponseEntity<List<PriceHistoryPoint>> getPriceHistory(@PathVariable Long id) {
        return ResponseEntity.ok(priceHistoryService.getPriceHistory(id));
    }
}
