package com.example.api.exception;

import java.util.List;

public class BadRequestException extends RuntimeException {
    private final transient List<FieldErrorDetail> errors;
    public BadRequestException(String message) { super(message); this.errors = null; }
    public BadRequestException(String message, List<FieldErrorDetail> errors) { super(message); this.errors = errors; }
    public List<FieldErrorDetail> getErrors() { return errors; }
}

