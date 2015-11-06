package org.atorma.tictactoe.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandlingAdvice {
    private static final Logger LOGGER = LoggerFactory.getLogger("org.atorma.tictactoe");


    @ExceptionHandler
    public void logAndRethrow(Exception e) throws Exception {
        LOGGER.error("Unhandled exception", e);
        throw e;
    }

}
