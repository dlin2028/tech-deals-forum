package com.techdeals.service;

import com.techdeals.elasticsearch.DealDocument;
import com.techdeals.model.Deal;
import com.techdeals.repository.DealDocumentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class ElasticsearchSyncService {

    private final DealDocumentRepository dealDocumentRepository;
    private final BenchmarkService benchmarkService;

    public void indexDeal(Deal deal) {
        try {
            DealDocument doc = convertToDocument(deal);
            dealDocumentRepository.save(doc);
        } catch (Exception e) {
            log.warn("Failed to index deal {} in Elasticsearch: {}", deal.getId(), e.getMessage());
        }
    }

    public void updateDealScores(Deal deal) {
        try {
            dealDocumentRepository.findById(String.valueOf(deal.getId())).ifPresent(doc -> {
                doc.setHotScore(deal.getHotScore());
                doc.setUpvoteCount(deal.getUpvoteCount());
                doc.setDownvoteCount(deal.getDownvoteCount());
                dealDocumentRepository.save(doc);
            });
        } catch (Exception e) {
            log.warn("Failed to update deal scores in Elasticsearch for deal {}: {}", deal.getId(), e.getMessage());
        }
    }

    private DealDocument convertToDocument(Deal deal) {
        int cpuBenchmark = benchmarkService.getComponentBenchmark(deal.getCpuModel());
        int gpuBenchmark = benchmarkService.getComponentBenchmark(deal.getGpuModel());

        return DealDocument.builder()
            .id(String.valueOf(deal.getId()))
            .title(deal.getTitle())
            .description(deal.getDescription())
            .url(deal.getUrl())
            .price(deal.getPrice() != null ? deal.getPrice().doubleValue() : null)
            .retailer(deal.getRetailer())
            .category(deal.getCategory())
            .imageUrl(deal.getImageUrl())
            .postedById(deal.getPostedBy() != null ? deal.getPostedBy().getId() : null)
            .createdAt(deal.getCreatedAt())
            .upvoteCount(deal.getUpvoteCount())
            .downvoteCount(deal.getDownvoteCount())
            .isActive(deal.isActive())
            .specs(deal.getSpecs())
            .cpuModel(deal.getCpuModel())
            .gpuModel(deal.getGpuModel())
            .cpuBenchmark(cpuBenchmark)
            .gpuBenchmark(gpuBenchmark)
            .benchmarkScore(Math.max(cpuBenchmark, gpuBenchmark))
            .hotScore(deal.getHotScore())
            .userTags(deal.getPostedBy() != null ? deal.getPostedBy().getTags() : null)
            .build();
    }
}
