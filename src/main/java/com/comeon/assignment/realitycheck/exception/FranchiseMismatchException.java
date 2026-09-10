package com.comeon.assignment.realitycheck.exception;

public class FranchiseMismatchException extends RuntimeException {

    public FranchiseMismatchException(long playerId) {
        super("Player " + playerId + " belongs to a different franchise than their active session");
    }
}