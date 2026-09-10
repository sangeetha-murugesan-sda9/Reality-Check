package com.comeon.assignment.realitycheck.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Error response body")
public record ApiError(

        @Schema(description = "Machine-readable error code", example = "NO_ACTIVE_SESSION")
        String code,

        @Schema(description = "Human-readable error detail", example = "No active reality-check session for player 1003")
        String message) {
}