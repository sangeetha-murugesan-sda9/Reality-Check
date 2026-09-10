package com.comeon.assignment.realitycheck.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Confirmation that an acknowledgement was recorded")
public record AcknowledgementResponse(

        @Schema(description = "When the acknowledgement was recorded, formatted in the player's own timezone",
                example = "6 July 26 14:35")
        String acknowledgedAt) {
}