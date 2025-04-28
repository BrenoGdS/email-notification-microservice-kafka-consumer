package com.appsdeveloperblog.ws.emailnotificationmicroservice.exception;

import org.springframework.dao.DataIntegrityViolationException;

public class NonRetryableException extends RuntimeException {

    public NonRetryableException(String message) {
        super(message);
    }

    public NonRetryableException(String message, Throwable cause) {
        super(message, cause);
    }

    public NonRetryableException(RuntimeException ex) {
    }
}