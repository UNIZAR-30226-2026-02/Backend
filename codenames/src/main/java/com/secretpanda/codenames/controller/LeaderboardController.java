package com.secretpanda.codenames.controller;

import java.security.Principal;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.secretpanda.codenames.dto.social.RankingDTO;
import com.secretpanda.codenames.service.LeaderboardService;

@RestController
@RequestMapping("/api/leaderboard")
public class LeaderboardController {

    private final LeaderboardService leaderboardService;

    public LeaderboardController(LeaderboardService leaderboardService) {
        this.leaderboardService = leaderboardService;
    }

    @GetMapping("/global")
    public ResponseEntity<List<RankingDTO>> getGlobalRanking() {
        return ResponseEntity.ok(leaderboardService.getGlobalRanking());
    }

    @GetMapping("/amigos")
    public ResponseEntity<List<RankingDTO>> getFriendsRanking(Principal principal) {
        return ResponseEntity.ok(leaderboardService.getFriendsRanking(principal.getName()));
    }
}