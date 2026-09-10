package com.comeon.assignment.realitycheck.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

@Schema(description = "Request to start a new reality-check session, or update the interval of an existing one")
public record StartSessionRequest(

        @Schema(description = "How often, in minutes, the player should be prompted", example = "30")
        @Min(value = 1, message = "intervalMinutes must be at least 1")
        @Max(value = 180, message = "intervalMinutes must be at most 180")
        int intervalMinutes) {
}