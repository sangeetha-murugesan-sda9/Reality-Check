package com.comeon.assignment.realitycheck.controller;

import com.comeon.assignment.realitycheck.dto.AcknowledgementResponse;
import com.comeon.assignment.realitycheck.dto.ApiError;
import com.comeon.assignment.realitycheck.dto.RealityCheckSessionResponse;
import com.comeon.assignment.realitycheck.dto.StartSessionRequest;
import com.comeon.assignment.realitycheck.exception.NoActiveSessionException;
import com.comeon.assignment.realitycheck.model.PlayerInfo;
import com.comeon.assignment.realitycheck.model.RealityCheckAcknowledgement;
import com.comeon.assignment.realitycheck.model.RealityCheckSession;
import com.comeon.assignment.realitycheck.service.RealityCheckService;
import com.comeon.assignment.realitycheck.util.PlayerTimeFormatter;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Exposes a player's reality-check session as a REST resource under
 * {@code /players/{playerId}/reality-check}
 */
@RestController
@RequestMapping("/players/{playerId}/reality-check")
@RequiredArgsConstructor
@Tag(name = "Reality Check", description = "Responsible-gaming reality-check sessions")
public class RealityCheckController {

    private final RealityCheckService service;

    @GetMapping
    @Operation(
            summary = "Get a player's current reality-check session",
            description = "Returns the player's active session, including elapsed time, net win/loss, "
                    + "and prompt timestamps formatted in the player's own timezone.")
    @ApiResponse(responseCode = "200", description = "Active session found")
    @ApiResponse(responseCode = "404", description = "Player has no active session",
            content = @Content(schema = @Schema(implementation = ApiError.class)))
    public RealityCheckSessionResponse getStatus(@PathVariable long playerId) {
        RealityCheckSession session = service.findActiveSession(playerId)
                .orElseThrow(() -> new NoActiveSessionException(playerId));
        PlayerInfo player = service.findPlayer(playerId);
        return toResponse(session, player);
    }

    @PostMapping
    @Operation(
            summary = "Start or resume a reality-check session",
            description = "Starts a new session if the player doesn't have one, or updates the reminder "
                    + "interval of their currently active session.")
    @ApiResponse(responseCode = "200", description = "Session started or updated")
    @ApiResponse(responseCode = "400", description = "intervalMinutes out of the allowed 1-180 range",
            content = @Content(schema = @Schema(implementation = ApiError.class)))
    @ApiResponse(responseCode = "404", description = "Player not found",
            content = @Content(schema = @Schema(implementation = ApiError.class)))
    @ApiResponse(responseCode = "409", description = "Player's franchise no longer matches their existing session",
            content = @Content(schema = @Schema(implementation = ApiError.class)))
    public RealityCheckSessionResponse startOrResume(
            @PathVariable long playerId,
            @Valid @RequestBody StartSessionRequest request) {
        RealityCheckSession session = service.startOrResumeSession(playerId, request.intervalMinutes());
        PlayerInfo player = service.findPlayer(playerId);
        return toResponse(session, player);
    }

    @PostMapping("/stop")
    @Operation(summary = "Stop a player's reality-check session")
    @ApiResponse(responseCode = "200", description = "Session stopped")
    @ApiResponse(responseCode = "404", description = "Player has no active session",
            content = @Content(schema = @Schema(implementation = ApiError.class)))
    public RealityCheckSessionResponse stop(@PathVariable long playerId) {
        RealityCheckSession session = service.stopSession(playerId);
        PlayerInfo player = service.findPlayer(playerId);
        return toResponse(session, player);
    }

    @PostMapping("/acknowledgements")
    @Operation(
            summary = "Record that the player acknowledged a reality-check prompt",
            description = "Persists the exact date and time of the acknowledgement, for Compliance reporting. "
                    + "Every acknowledgement is kept, not just the most recent.")
    @ApiResponse(responseCode = "200", description = "Acknowledgement recorded")
    @ApiResponse(responseCode = "404", description = "Player has no active session to acknowledge",
            content = @Content(schema = @Schema(implementation = ApiError.class)))
    public AcknowledgementResponse acknowledge(@PathVariable long playerId) {
        RealityCheckAcknowledgement ack = service.recordAcknowledgement(playerId);
        PlayerInfo player = service.findPlayer(playerId);
        return new AcknowledgementResponse(PlayerTimeFormatter.format(ack.getAcknowledgedAt(), player.timezone()));
    }

    private RealityCheckSessionResponse toResponse(RealityCheckSession session, PlayerInfo player) {
        return new RealityCheckSessionResponse(
                session.getStatus(),
                session.getIntervalMinutes(),
                session.getElapsedSeconds(),
                session.getNetAmountMinor(),
                PlayerTimeFormatter.format(session.getLastPromptAt(), player.timezone()),
                PlayerTimeFormatter.format(session.getNextCheckAt(), player.timezone()));
    }
}