package com.example.api.exception;

import java.util.List;

public class UnprocessableEntityException extends RuntimeException {
    private final transient List<FieldErrorDetail> errors;
    public UnprocessableEntityException(String message, List<FieldErrorDetail> errors) { super(message); this.errors = errors; }
    public List<FieldErrorDetail> getErrors() { return errors; }
}

