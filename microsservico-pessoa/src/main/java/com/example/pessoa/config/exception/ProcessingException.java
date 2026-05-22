package com.example.pessoa.config.exception;

public class ProcessingException extends RuntimeException {
    public ProcessingException(String message, Throwable cause) {
        super(message, cause);
    }
}