package com.techdeals.service;

import com.techdeals.dto.PriceHistoryPoint;
import com.techdeals.exception.ResourceNotFoundException;
import com.techdeals.model.Deal;
import com.techdeals.model.PriceHistory;
import com.techdeals.repository.DealRepository;
import com.techdeals.repository.PriceHistoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PriceHistoryService {

    private final PriceHistoryRepository priceHistoryRepository;
    private final DealRepository dealRepository;

    public void addPricePoint(Long dealId, BigDecimal price, String source) {
        Deal deal = dealRepository.findById(dealId)
            .orElseThrow(() -> new ResourceNotFoundException("Deal not found: " + dealId));

        PriceHistory history = PriceHistory.builder()
            .deal(deal)
            .price(price)
            .source(source)
            .build();

        priceHistoryRepository.save(history);
    }

    public List<PriceHistoryPoint> getPriceHistory(Long dealId) {
        return priceHistoryRepository.findByDealIdOrderByTimestampAsc(dealId)
            .stream()
            .map(ph -> PriceHistoryPoint.builder()
                .price(ph.getPrice())
                .timestamp(ph.getTimestamp())
                .build())
            .collect(Collectors.toList());
    }
}
