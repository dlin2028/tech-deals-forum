package com.techdeals.repository;

import com.techdeals.elasticsearch.DealDocument;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DealDocumentRepository extends ElasticsearchRepository<DealDocument, String> {
    List<DealDocument> findByCategory(String category);
    List<DealDocument> findByIsActiveTrue();
}
