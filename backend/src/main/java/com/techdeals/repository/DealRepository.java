package com.techdeals.repository;

import com.techdeals.model.Deal;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DealRepository extends JpaRepository<Deal, Long> {
    Page<Deal> findByIsActiveTrue(Pageable pageable);
    Page<Deal> findByIsActiveTrueOrderByHotScoreDesc(Pageable pageable);
    Page<Deal> findByCategory(String category, Pageable pageable);
}
