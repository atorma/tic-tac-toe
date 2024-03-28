package org.atorma.tictactoe.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandlingAdvice {
    private static final Logger LOGGER = LoggerFactory.getLogger("org.atorma.tictactoe");


    @ExceptionHandler
    public ResponseEntity<Void> logAndReturnInternalServerError(Exception e) {
        LOGGER.error("Unhandled exception", e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
    }

}
