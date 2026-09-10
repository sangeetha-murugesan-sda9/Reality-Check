
package com.comeon.assignment.realitycheck.exception;

public class PlayerNotFoundException extends RuntimeException {

    public PlayerNotFoundException(long playerId) {
        super("No player found with id " + playerId);
    }
}
