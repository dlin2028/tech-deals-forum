package com.techdeals.service;

import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import com.techdeals.dto.DealRequest;
import com.techdeals.dto.DealResponse;
import com.techdeals.dto.DealSearchFilters;
import com.techdeals.elasticsearch.DealDocument;
import com.techdeals.exception.ResourceNotFoundException;
import com.techdeals.model.Deal;
import com.techdeals.model.User;
import com.techdeals.repository.DealRepository;
import com.techdeals.repository.UserRepository;
import com.techdeals.repository.VoteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class DealService {

    private final DealRepository dealRepository;
    private final UserRepository userRepository;
    private final VoteRepository voteRepository;
    private final BenchmarkService benchmarkService;
    private final ElasticsearchOperations elasticsearchOperations;
    private final ElasticsearchSyncService elasticsearchSyncService;

    public DealResponse createDeal(DealRequest request, String username) {
        User user = userRepository.findByUsername(username)
            .orElseThrow(() -> new ResourceNotFoundException("User not found: " + username));

        Deal deal = Deal.builder()
            .title(request.getTitle())
            .description(request.getDescription())
            .url(request.getUrl())
            .price(request.getPrice())
            .retailer(request.getRetailer())
            .category(request.getCategory())
            .imageUrl(request.getImageUrl())
            .specs(request.getSpecs() != null ? request.getSpecs() : new HashMap<>())
            .cpuModel(request.getCpuModel())
            .gpuModel(request.getGpuModel())
            .postedBy(user)
            .build();

        benchmarkService.enrichDealWithBenchmarks(deal);
        deal = dealRepository.save(deal);
        deal.recalculateHotScore();
        deal = dealRepository.save(deal);
        elasticsearchSyncService.indexDeal(deal);

        return convertToResponse(deal, username);
    }

    @Transactional(readOnly = true)
    public DealResponse getDealById(Long id, String username) {
        Deal deal = dealRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Deal not found: " + id));
        return convertToResponse(deal, username);
    }

    @Transactional(readOnly = true)
    public Page<DealResponse> searchDeals(DealSearchFilters filters, Pageable pageable) {
        try {
            BoolQuery.Builder boolBuilder = new BoolQuery.Builder();

            if (filters.getQuery() != null && !filters.getQuery().isBlank()) {
                boolBuilder.must(Query.of(q -> q.multiMatch(mm -> mm
                    .query(filters.getQuery())
                    .fields("title^2", "description")
                )));
            }

            if (filters.getCategory() != null && !filters.getCategory().isBlank()) {
                boolBuilder.filter(Query.of(q -> q.term(t -> t
                    .field("category")
                    .value(filters.getCategory())
                )));
            }

            if (filters.getMinPrice() != null || filters.getMaxPrice() != null) {
                boolBuilder.filter(Query.of(q -> q.range(r -> {
                    r.field("price");
                    if (filters.getMinPrice() != null) {
                        r.gte(co.elastic.clients.json.JsonData.of(filters.getMinPrice().doubleValue()));
                    }
                    if (filters.getMaxPrice() != null) {
                        r.lte(co.elastic.clients.json.JsonData.of(filters.getMaxPrice().doubleValue()));
                    }
                    return r;
                })));
            }

            if (filters.getMinBenchmark() > 0) {
                boolBuilder.filter(Query.of(q -> q.range(r -> r
                    .field("benchmarkScore")
                    .gte(co.elastic.clients.json.JsonData.of(filters.getMinBenchmark()))
                )));
            }

            Sort sort = Sort.by(Sort.Direction.DESC, "hotScore");
            if (filters.getSort() != null) {
                sort = switch (filters.getSort()) {
                    case "new" -> Sort.by(Sort.Direction.DESC, "createdAt");
                    case "price_asc" -> Sort.by(Sort.Direction.ASC, "price");
                    case "price_desc" -> Sort.by(Sort.Direction.DESC, "price");
                    default -> Sort.by(Sort.Direction.DESC, "hotScore");
                };
            }

            NativeQuery nativeQuery = NativeQuery.builder()
                .withQuery(Query.of(q -> q.bool(boolBuilder.build())))
                .withPageable(pageable)
                .withSort(sort)
                .build();

            SearchHits<DealDocument> hits = elasticsearchOperations.search(nativeQuery, DealDocument.class);
            List<DealResponse> responses = hits.getSearchHits().stream()
                .map(hit -> {
                    DealDocument doc = hit.getContent();
                    return dealRepository.findById(Long.parseLong(doc.getId()))
                        .map(deal -> convertToResponse(deal, null))
                        .orElse(null);
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

            return new PageImpl<>(responses, pageable, hits.getTotalHits());
        } catch (Exception e) {
            return dealRepository.findByIsActiveTrue(pageable)
                .map(deal -> convertToResponse(deal, null));
        }
    }

    @Transactional(readOnly = true)
    public List<DealResponse> getHotDeals(Pageable pageable) {
        return dealRepository.findByIsActiveTrueOrderByHotScoreDesc(pageable)
            .stream()
            .map(deal -> convertToResponse(deal, null))
            .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Page<DealResponse> getPersonalizedFeed(String username, Pageable pageable) {
        User user = userRepository.findByUsername(username)
            .orElseThrow(() -> new ResourceNotFoundException("User not found: " + username));

        List<String> userTags = user.getTags();
        if (userTags == null || userTags.isEmpty()) {
            return dealRepository.findByIsActiveTrue(pageable)
                .map(deal -> convertToResponse(deal, username));
        }

        try {
            BoolQuery.Builder boolBuilder = new BoolQuery.Builder();
            boolBuilder.filter(Query.of(q -> q.term(t -> t
                .field("isActive")
                .value(true)
            )));
            boolBuilder.should(Query.of(q -> q.terms(t -> t
                .field("category")
                .terms(tv -> tv.value(userTags.stream()
                    .map(co.elastic.clients.elasticsearch._types.FieldValue::of)
                    .collect(Collectors.toList())))
            )));

            NativeQuery nativeQuery = NativeQuery.builder()
                .withQuery(Query.of(q -> q.bool(boolBuilder.build())))
                .withPageable(pageable)
                .withSort(Sort.by(Sort.Direction.DESC, "hotScore"))
                .build();

            SearchHits<DealDocument> hits = elasticsearchOperations.search(nativeQuery, DealDocument.class);
            List<DealResponse> responses = hits.getSearchHits().stream()
                .map(hit -> {
                    DealDocument doc = hit.getContent();
                    return dealRepository.findById(Long.parseLong(doc.getId()))
                        .map(deal -> convertToResponse(deal, username))
                        .orElse(null);
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

            return new PageImpl<>(responses, pageable, hits.getTotalHits());
        } catch (Exception e) {
            return dealRepository.findByIsActiveTrue(pageable)
                .map(deal -> convertToResponse(deal, username));
        }
    }

    public DealResponse convertToResponse(Deal deal, String username) {
        int cpuBenchmark = benchmarkService.getComponentBenchmark(deal.getCpuModel());
        int gpuBenchmark = benchmarkService.getComponentBenchmark(deal.getGpuModel());
        int benchmarkScore = Math.max(cpuBenchmark, gpuBenchmark);

        String userVote = null;
        if (username != null) {
            Optional<User> user = userRepository.findByUsername(username);
            if (user.isPresent()) {
                userVote = voteRepository.findByUserIdAndDealId(user.get().getId(), deal.getId())
                    .map(v -> v.getVoteType().name())
                    .orElse(null);
            }
        }

        return DealResponse.builder()
            .id(deal.getId())
            .title(deal.getTitle())
            .description(deal.getDescription())
            .url(deal.getUrl())
            .price(deal.getPrice())
            .retailer(deal.getRetailer())
            .category(deal.getCategory())
            .imageUrl(deal.getImageUrl())
            .postedById(deal.getPostedBy() != null ? deal.getPostedBy().getId() : null)
            .postedByUsername(deal.getPostedBy() != null ? deal.getPostedBy().getUsername() : null)
            .createdAt(deal.getCreatedAt())
            .upvoteCount(deal.getUpvoteCount())
            .downvoteCount(deal.getDownvoteCount())
            .isActive(deal.isActive())
            .specs(deal.getSpecs())
            .cpuModel(deal.getCpuModel())
            .gpuModel(deal.getGpuModel())
            .benchmarkScore(benchmarkScore)
            .cpuBenchmark(cpuBenchmark)
            .gpuBenchmark(gpuBenchmark)
            .hotScore(deal.getHotScore())
            .userVote(userVote)
            .build();
    }
}
