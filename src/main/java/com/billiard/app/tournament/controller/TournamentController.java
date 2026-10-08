package com.billiard.app.tournament.controller;

import com.billiard.app.common.response.ApiResponse;
import com.billiard.app.tournament.dto.SaveTournamentRequest;
import com.billiard.app.tournament.dto.SetWinnerRequest;
import com.billiard.app.tournament.dto.TournamentDetailResponse;
import com.billiard.app.tournament.dto.TournamentMatchResponse;
import com.billiard.app.tournament.dto.TournamentResponse;
import com.billiard.app.tournament.service.TournamentService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/tournaments")
public class TournamentController {

    private final TournamentService tournamentService;

    public TournamentController(TournamentService tournamentService) {
        this.tournamentService = tournamentService;
    }

    @GetMapping
    public ApiResponse<List<TournamentResponse>> listTournaments() {
        return ApiResponse.success(tournamentService.listTournaments());
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<TournamentDetailResponse> createTournament(@Valid @RequestBody SaveTournamentRequest request) {
        return ApiResponse.success(tournamentService.createTournament(request));
    }

    @GetMapping("/{id}")
    public ApiResponse<TournamentDetailResponse> getTournament(@PathVariable UUID id) {
        return ApiResponse.success(tournamentService.getDetail(id));
    }

    @PutMapping("/{id}")
    public ApiResponse<TournamentDetailResponse> updateTournament(
            @PathVariable UUID id, @Valid @RequestBody SaveTournamentRequest request) {
        return ApiResponse.success(tournamentService.updateTournament(id, request));
    }

    @PostMapping("/{id}/setup")
    public ApiResponse<TournamentDetailResponse> setupTournament(@PathVariable UUID id) {
        return ApiResponse.success(tournamentService.setupTournament(id));
    }

    @PatchMapping("/{id}/matches/{matchId}/winner")
    public ApiResponse<TournamentMatchResponse> setMatchWinner(
            @PathVariable UUID id, @PathVariable UUID matchId, @Valid @RequestBody SetWinnerRequest request) {
        return ApiResponse.success(tournamentService.setMatchWinner(id, matchId, request));
    }
}
