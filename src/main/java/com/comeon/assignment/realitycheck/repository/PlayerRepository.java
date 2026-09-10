package com.comeon.assignment.realitycheck.repository;

import com.comeon.assignment.realitycheck.model.PlayerInfo;
import lombok.RequiredArgsConstructor;
import org.jdbi.v3.core.Handle;
import org.jdbi.v3.core.Jdbi;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class PlayerRepository {

    private final Jdbi jdbi;

    public Optional<PlayerInfo> findById(long playerId) {
        try (Handle handle = jdbi.open()) {
            return handle.createQuery(
                            "SELECT id, franchise_id, timezone FROM player WHERE id = :playerId")
                    .bind("playerId", playerId)
                    .map((rs, ctx) -> new PlayerInfo(
                            rs.getLong("id"),
                            rs.getLong("franchise_id"),
                            rs.getString("timezone")))
                    .findOne();
        }
    }
}