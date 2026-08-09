package org.zero_consult.invoices_backend.controllers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.zero_consult.invoices_backend.exceptions.RestControllerException;

@ControllerAdvice
public class ExceptionHandler {
    private static final Logger logger = LoggerFactory.getLogger(ExceptionHandler.class);
    @org.springframework.web.bind.annotation.ExceptionHandler({ RestControllerException.class })
    protected ResponseEntity<String> handleInvalidInputException(RuntimeException ex, WebRequest request) {
        if(ex instanceof RestControllerException restControllerException) {
            logger.error("Exception in rest controller", restControllerException);
            return new ResponseEntity<>(restControllerException.getMessage(), restControllerException.getStatusCode());
        }
        return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
    }
}
