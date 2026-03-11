package com.techdeals.service;

import com.techdeals.exception.DuplicateVoteException;
import com.techdeals.exception.ResourceNotFoundException;
import com.techdeals.model.Deal;
import com.techdeals.model.User;
import com.techdeals.model.Vote;
import com.techdeals.model.VoteType;
import com.techdeals.repository.DealRepository;
import com.techdeals.repository.UserRepository;
import com.techdeals.repository.VoteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class VoteService {

    private final VoteRepository voteRepository;
    private final DealRepository dealRepository;
    private final UserRepository userRepository;
    private final ElasticsearchSyncService elasticsearchSyncService;

    public void castVote(Long dealId, String username, String voteTypeStr) {
        User user = userRepository.findByUsername(username)
            .orElseThrow(() -> new ResourceNotFoundException("User not found: " + username));
        Deal deal = dealRepository.findById(dealId)
            .orElseThrow(() -> new ResourceNotFoundException("Deal not found: " + dealId));

        VoteType voteType = VoteType.valueOf(voteTypeStr.toUpperCase());
        Optional<Vote> existingVote = voteRepository.findByUserIdAndDealId(user.getId(), dealId);

        if (existingVote.isPresent()) {
            Vote vote = existingVote.get();
            if (vote.getVoteType() == voteType) {
                throw new DuplicateVoteException("Already voted " + voteType + " on this deal");
            }
            if (vote.getVoteType() == VoteType.UP) {
                deal.setUpvoteCount(deal.getUpvoteCount() - 1);
                deal.setDownvoteCount(deal.getDownvoteCount() + 1);
            } else {
                deal.setDownvoteCount(deal.getDownvoteCount() - 1);
                deal.setUpvoteCount(deal.getUpvoteCount() + 1);
            }
            vote.setVoteType(voteType);
            voteRepository.save(vote);
        } else {
            Vote vote = Vote.builder()
                .user(user)
                .deal(deal)
                .voteType(voteType)
                .build();
            voteRepository.save(vote);
            if (voteType == VoteType.UP) {
                deal.setUpvoteCount(deal.getUpvoteCount() + 1);
            } else {
                deal.setDownvoteCount(deal.getDownvoteCount() + 1);
            }
        }

        deal.recalculateHotScore();
        dealRepository.save(deal);
        elasticsearchSyncService.updateDealScores(deal);
    }

    public void removeVote(Long dealId, String username) {
        User user = userRepository.findByUsername(username)
            .orElseThrow(() -> new ResourceNotFoundException("User not found: " + username));
        Deal deal = dealRepository.findById(dealId)
            .orElseThrow(() -> new ResourceNotFoundException("Deal not found: " + dealId));

        Vote vote = voteRepository.findByUserIdAndDealId(user.getId(), dealId)
            .orElseThrow(() -> new ResourceNotFoundException("Vote not found"));

        if (vote.getVoteType() == VoteType.UP) {
            deal.setUpvoteCount(Math.max(0, deal.getUpvoteCount() - 1));
        } else {
            deal.setDownvoteCount(Math.max(0, deal.getDownvoteCount() - 1));
        }

        voteRepository.delete(vote);
        deal.recalculateHotScore();
        dealRepository.save(deal);
        elasticsearchSyncService.updateDealScores(deal);
    }
}
