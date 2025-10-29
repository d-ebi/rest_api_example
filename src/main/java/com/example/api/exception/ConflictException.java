package com.example.api.exception;

import java.util.List;

public class ConflictException extends RuntimeException {
    private final transient List<FieldErrorDetail> errors;
    public ConflictException(String message, List<FieldErrorDetail> errors) { super(message); this.errors = errors; }
    public List<FieldErrorDetail> getErrors() { return errors; }
}

