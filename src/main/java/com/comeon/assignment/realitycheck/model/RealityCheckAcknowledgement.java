package com.comeon.assignment.realitycheck.model;

import lombok.Data;

/**
 * A single record of a player acknowledging a reality-check prompt. We keep one row per
 * acknowledgement.So Compliance can pull
 * a full history for reporting
 */
@Data
public class RealityCheckAcknowledgement {
    private long id;
    private long playerId;
    private long sessionId;
    private long acknowledgedAt;
}