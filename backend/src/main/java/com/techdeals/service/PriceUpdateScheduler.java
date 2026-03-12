package com.techdeals.service;

import com.techdeals.model.Deal;
import com.techdeals.repository.DealRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class PriceUpdateScheduler {

    private final DealRepository dealRepository;
    private final ElasticsearchSyncService elasticsearchSyncService;

    private static final int BATCH_SIZE = 100;

    @Scheduled(fixedRate = 3600000)
    @Transactional
    public void updatePricesAndScores() {
        log.info("Running scheduled hot score update...");
        int page = 0;
        Page<Deal> batch;
        do {
            batch = dealRepository.findByIsActiveTrue(PageRequest.of(page++, BATCH_SIZE));
            batch.getContent().forEach(deal -> {
                deal.recalculateHotScore();
                dealRepository.save(deal);
                elasticsearchSyncService.updateDealScores(deal);
            });
        } while (batch.hasNext());
        log.info("Hot score update complete.");
    }
}
