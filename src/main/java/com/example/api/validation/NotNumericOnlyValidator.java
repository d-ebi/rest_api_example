package com.example.api.validation;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class NotNumericOnlyValidator implements ConstraintValidator<NotNumericOnly, String> {
    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null) return true; // optional fields allowed
        String trimmed = value.trim();
        if (trimmed.isEmpty()) return true; // handled elsewhere with @NotBlank when required
        return trimmed.matches(".*\\D.*");
    }
}

