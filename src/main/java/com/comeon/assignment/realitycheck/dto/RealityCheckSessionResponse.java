package com.comeon.assignment.realitycheck.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Current state of a player's reality-check session")
public record RealityCheckSessionResponse(

        @Schema(description = "Session status", example = "ACTIVE", allowableValues = {"ACTIVE", "STOPPED"})
        String status,

        @Schema(description = "Reminder interval, in minutes", example = "30")
        int intervalMinutes,

        @Schema(description = "Seconds elapsed in the current session", example = "5400")
        long elapsedSeconds,

        @Schema(description = "Net win/loss so far, in minor currency units (e.g. cents/öre)", example = "-4200")
        long netAmountMinor,

        @Schema(description = "When the player was last prompted, formatted in their own timezone", example = "6 July 26 14:35")
        String lastPromptAt,

        @Schema(description = "When the next reminder is due, formatted in their own timezone", example = "6 July 26 15:05")
        String nextCheckAt) {
}