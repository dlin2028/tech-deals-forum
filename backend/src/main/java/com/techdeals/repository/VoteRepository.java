package com.techdeals.repository;

import com.techdeals.model.Vote;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface VoteRepository extends JpaRepository<Vote, Long> {
    Optional<Vote> findByUserIdAndDealId(Long userId, Long dealId);
    List<Vote> findByDealId(Long dealId);
}
