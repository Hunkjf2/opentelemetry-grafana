package com.example.log.config.exception;

public class ProcessingException extends RuntimeException {
    public ProcessingException(String message, Throwable cause) {
        super(message, cause);
    }
}