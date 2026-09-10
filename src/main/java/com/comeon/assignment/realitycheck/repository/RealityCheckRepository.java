package com.comeon.assignment.realitycheck.repository;

import com.comeon.assignment.realitycheck.model.RealityCheckAcknowledgement;
import com.comeon.assignment.realitycheck.model.RealityCheckSession;
import lombok.RequiredArgsConstructor;
import org.jdbi.v3.core.Handle;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.core.statement.StatementContext;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class RealityCheckRepository {

    private static final String ACTIVE = "ACTIVE";

    private final Jdbi jdbi;

    public Optional<RealityCheckSession> findActiveByPlayer(long playerId) {
        try (Handle handle = jdbi.open()) {
            return handle.createQuery(
                            "SELECT * FROM reality_check_session WHERE player_id = :playerId AND status = :status")
                    .bind("playerId", playerId)
                    .bind("status", ACTIVE)
                    .map(this::mapSession)
                    .findOne();
        }
    }

    public List<Long> findActivePlayerIds() {
        try (Handle handle = jdbi.open()) {
            return handle.createQuery("SELECT player_id FROM reality_check_session WHERE status = :status")
                    .bind("status", ACTIVE)
                    .mapTo(Long.class)
                    .list();
        }
    }

    public void insertSession(RealityCheckSession s) {
        try (Handle handle = jdbi.open()) {
            handle.createUpdate("INSERT INTO reality_check_session " +
                            "(player_id, franchise_id, status, interval_minutes, started_at, last_prompt_at, " +
                            " elapsed_seconds, net_amount_minor, next_check_at, version) " +
                            "VALUES (:playerId, :franchiseId, :status, :intervalMinutes, :startedAt, :lastPromptAt, " +
                            " :elapsedSeconds, :netAmountMinor, :nextCheckAt, 0)")
                    .bind("playerId", s.getPlayerId())
                    .bind("franchiseId", s.getFranchiseId())
                    .bind("status", s.getStatus())
                    .bind("intervalMinutes", s.getIntervalMinutes())
                    .bind("startedAt", s.getStartedAt())
                    .bind("lastPromptAt", s.getLastPromptAt())
                    .bind("elapsedSeconds", s.getElapsedSeconds())
                    .bind("netAmountMinor", s.getNetAmountMinor())
                    .bind("nextCheckAt", s.getNextCheckAt())
                    .execute();
        }
    }

    /**
     * Updates a session using optimistic locking: the WHERE clause only matches the row if
     * its version still matches what the caller originally read. If another writer (another
     * request, or the scheduled refresh job) updated it first, this returns false instead of
     * silently overwriting their change - the caller is expected to re-read and retry.
     */
    public boolean updateSession(RealityCheckSession s) {
        try (Handle handle = jdbi.open()) {
            int rowsUpdated = handle.createUpdate("UPDATE reality_check_session SET " +
                            "status = :status, " +
                            "interval_minutes = :intervalMinutes, " +
                            "last_prompt_at = :lastPromptAt, " +
                            "elapsed_seconds = :elapsedSeconds, " +
                            "net_amount_minor = :netAmountMinor, " +
                            "next_check_at = :nextCheckAt, " +
                            "version = version + 1 " +
                            "WHERE id = :id AND version = :version")
                    .bind("status", s.getStatus())
                    .bind("intervalMinutes", s.getIntervalMinutes())
                    .bind("lastPromptAt", s.getLastPromptAt())
                    .bind("elapsedSeconds", s.getElapsedSeconds())
                    .bind("netAmountMinor", s.getNetAmountMinor())
                    .bind("nextCheckAt", s.getNextCheckAt())
                    .bind("id", s.getId())
                    .bind("version", s.getVersion())
                    .execute();
            return rowsUpdated == 1;
        }
    }

    public void insertAcknowledgement(RealityCheckAcknowledgement ack) {
        try (Handle handle = jdbi.open()) {
            handle.createUpdate("INSERT INTO reality_check_acknowledgement " +
                            "(player_id, session_id, acknowledged_at) " +
                            "VALUES (:playerId, :sessionId, :acknowledgedAt)")
                    .bind("playerId", ack.getPlayerId())
                    .bind("sessionId", ack.getSessionId())
                    .bind("acknowledgedAt", ack.getAcknowledgedAt())
                    .execute();
        }
    }

    private RealityCheckSession mapSession(ResultSet rs, StatementContext ctx) throws SQLException {
        RealityCheckSession s = new RealityCheckSession();
        s.setId(rs.getLong("id"));
        s.setPlayerId(rs.getLong("player_id"));
        s.setFranchiseId(rs.getLong("franchise_id"));
        s.setStatus(rs.getString("status"));
        s.setIntervalMinutes(rs.getInt("interval_minutes"));
        s.setStartedAt(rs.getLong("started_at"));
        s.setLastPromptAt(rs.getLong("last_prompt_at"));
        s.setElapsedSeconds(rs.getLong("elapsed_seconds"));
        s.setNetAmountMinor(rs.getLong("net_amount_minor"));
        s.setNextCheckAt(rs.getLong("next_check_at"));
        s.setVersion(rs.getLong("version"));
        return s;
    }
}