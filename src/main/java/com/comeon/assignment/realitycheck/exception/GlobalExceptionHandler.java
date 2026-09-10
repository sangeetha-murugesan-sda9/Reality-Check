package com.comeon.assignment.realitycheck.exception;

import com.comeon.assignment.realitycheck.dto.ApiError;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;


@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(PlayerNotFoundException.class)
    public ResponseEntity<ApiError> handlePlayerNotFound(PlayerNotFoundException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ApiError("PLAYER_NOT_FOUND", e.getMessage()));
    }

    @ExceptionHandler(NoActiveSessionException.class)
    public ResponseEntity<ApiError> handleNoActiveSession(NoActiveSessionException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ApiError("NO_ACTIVE_SESSION", e.getMessage()));
    }

    @ExceptionHandler(FranchiseMismatchException.class)
    public ResponseEntity<ApiError> handleFranchiseMismatch(FranchiseMismatchException e) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(new ApiError("FRANCHISE_MISMATCH", e.getMessage()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleUnexpected(Exception e) {
        log.error("Unhandled exception", e);
        return ResponseEntity.internalServerError()
                .body(new ApiError("INTERNAL_ERROR", "Something went wrong. Please try again."));
    }
}