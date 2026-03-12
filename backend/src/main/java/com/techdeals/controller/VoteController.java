package com.techdeals.controller;

import com.techdeals.dto.VoteRequest;
import com.techdeals.service.VoteService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/deals/{id}/vote")
@RequiredArgsConstructor
public class VoteController {

    private final VoteService voteService;

    @PostMapping
    public ResponseEntity<Void> castVote(
        @PathVariable Long id,
        @RequestBody VoteRequest voteRequest,
        @AuthenticationPrincipal UserDetails userDetails) {
        voteService.castVote(id, userDetails.getUsername(), voteRequest.getVoteType());
        return ResponseEntity.ok().build();
    }

    @DeleteMapping
    public ResponseEntity<Void> removeVote(
        @PathVariable Long id,
        @AuthenticationPrincipal UserDetails userDetails) {
        voteService.removeVote(id, userDetails.getUsername());
        return ResponseEntity.noContent().build();
    }
}
