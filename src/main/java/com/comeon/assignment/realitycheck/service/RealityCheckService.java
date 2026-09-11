package com.comeon.assignment.realitycheck.service;

import com.comeon.assignment.realitycheck.exception.FranchiseMismatchException;
import com.comeon.assignment.realitycheck.exception.NoActiveSessionException;
import com.comeon.assignment.realitycheck.exception.PlayerNotFoundException;
import com.comeon.assignment.realitycheck.model.PlayerInfo;
import com.comeon.assignment.realitycheck.model.RealityCheckAcknowledgement;
import com.comeon.assignment.realitycheck.model.RealityCheckSession;
import com.comeon.assignment.realitycheck.repository.PlayerRepository;
import com.comeon.assignment.realitycheck.repository.RealityCheckRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

@Service
@RequiredArgsConstructor
public class RealityCheckService {

    private static final String ACTIVE = "ACTIVE";
    private static final String STOPPED = "STOPPED";
    private static final int MAX_UPDATE_ATTEMPTS = 3;

    private final RealityCheckRepository sessionRepository;
    private final PlayerRepository playerRepository;

    public Optional<RealityCheckSession> findActiveSession(long playerId) {
        return sessionRepository.findActiveByPlayer(playerId);
    }

    public PlayerInfo findPlayer(long playerId) {
        return playerRepository.findById(playerId)
                .orElseThrow(() -> new PlayerNotFoundException(playerId));
    }

    public RealityCheckSession startOrResumeSession(long playerId, int intervalMinutes) {
        PlayerInfo player = findPlayer(playerId);

        Optional<RealityCheckSession> existing = sessionRepository.findActiveByPlayer(playerId);
        if (existing.isEmpty()) {
            RealityCheckSession session = newSession(player, intervalMinutes);
            sessionRepository.insertSession(session);
            return sessionRepository.findActiveByPlayer(playerId)
                    .orElseThrow(() -> new IllegalStateException(
                            "Session for player " + playerId + " vanished immediately after insert"));
        }

        RealityCheckSession session = existing.get();
        if (session.getFranchiseId() != player.franchiseId()) {
            throw new FranchiseMismatchException(playerId);
        }

        return updateWithRetry(session, s -> applyInterval(s, intervalMinutes));
    }

    public RealityCheckSession stopSession(long playerId) {
        RealityCheckSession session = sessionRepository.findActiveByPlayer(playerId)
                .orElseThrow(() -> new NoActiveSessionException(playerId));
        return updateWithRetry(session, s -> s.setStatus(STOPPED));
    }

    public List<Long> activePlayerIds() {
        return sessionRepository.findActivePlayerIds();
    }

    /**
     * Called by the scheduled refresh job. Recomputes elapsed time for a player's active
     * session and, if a reminder is due, marks it as prompted. Returns the session only when
     * a reminder was actually triggered, so the caller knows whether to notify the player.
     */
    public Optional<RealityCheckSession> refreshIfDue(long playerId) {
        for (int attempt = 0; attempt < MAX_UPDATE_ATTEMPTS; attempt++) {
            Optional<RealityCheckSession> current = sessionRepository.findActiveByPlayer(playerId);
            if (current.isEmpty()) {
                return Optional.empty();
            }

            RealityCheckSession session = current.get();
            long now = Instant.now().getEpochSecond();
            session.setElapsedSeconds(now - session.getStartedAt());

            boolean promptDue = now >= session.getNextCheckAt();
            if (promptDue) {
                session.setLastPromptAt(now);
                session.setNextCheckAt(now + (long) session.getIntervalMinutes() * 60);
            }

            if (sessionRepository.updateSession(session)) {
                return promptDue ? Optional.of(session) : Optional.empty();
            }
            // Someone else updated this session between our read and write - reread and retry.
        }
        throw new IllegalStateException(
                "Could not refresh session for player " + playerId + " after " + MAX_UPDATE_ATTEMPTS + " attempts");
    }

    public RealityCheckAcknowledgement recordAcknowledgement(long playerId) {
        RealityCheckSession session = sessionRepository.findActiveByPlayer(playerId)
                .orElseThrow(() -> new NoActiveSessionException(playerId));

        RealityCheckAcknowledgement ack = new RealityCheckAcknowledgement();
        ack.setPlayerId(playerId);
        ack.setSessionId(session.getId());
        ack.setAcknowledgedAt(Instant.now().getEpochSecond());
        sessionRepository.insertAcknowledgement(ack);
        return ack;
    }

    private RealityCheckSession newSession(PlayerInfo player, int intervalMinutes) {
        long now = Instant.now().getEpochSecond();
        RealityCheckSession session = new RealityCheckSession();
        session.setPlayerId(player.id());
        session.setFranchiseId(player.franchiseId());
        session.setStatus(ACTIVE);
        session.setIntervalMinutes(intervalMinutes);
        session.setStartedAt(now);
        session.setLastPromptAt(now);
        session.setElapsedSeconds(0);
        session.setNetAmountMinor(0);
        session.setNextCheckAt(now + (long) intervalMinutes * 60);
        return session;
    }

    private void applyInterval(RealityCheckSession session, int intervalMinutes) {
        long now = Instant.now().getEpochSecond();
        session.setElapsedSeconds(now - session.getStartedAt());
        session.setIntervalMinutes(intervalMinutes);
        if (now >= session.getNextCheckAt()) {
            session.setLastPromptAt(now);
            session.setNextCheckAt(now + (long) intervalMinutes * 60);
        }
    }

    /**
     * Applies a mutation and writes it, retrying with a fresh read if another writer won the
     * optimistic-lock race in between. At this traffic volume a couple of retries is plenty;
     * a genuinely hot row would call for a different approach (e.g. a queue), but that's well
     * beyond what this service needs.
     */
    private RealityCheckSession updateWithRetry(RealityCheckSession session, Consumer<RealityCheckSession> mutation) {
        RealityCheckSession current = session;
        for (int attempt = 0; attempt < MAX_UPDATE_ATTEMPTS; attempt++) {
            mutation.accept(current);
            if (sessionRepository.updateSession(current)) {
                return current;
            }
            current = sessionRepository.findActiveByPlayer(current.getPlayerId())
                    .orElseThrow(() -> new NoActiveSessionException(current.getPlayerId()));
        }
        throw new IllegalStateException(
                "Could not update session for player " + session.getPlayerId() + " after " + MAX_UPDATE_ATTEMPTS + " attempts");
    }
}