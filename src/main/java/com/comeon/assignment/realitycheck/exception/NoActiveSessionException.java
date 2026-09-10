package com.comeon.assignment.realitycheck.exception;

public class NoActiveSessionException extends RuntimeException {

    public NoActiveSessionException(long playerId) {
        super("No active reality-check session for player " + playerId);
    }
}