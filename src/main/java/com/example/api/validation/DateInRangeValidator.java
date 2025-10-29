package com.example.api.validation;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class DateInRangeValidator implements ConstraintValidator<DateInRange, LocalDate> {
    private LocalDate min;
    private LocalDate max;
    @Override
    public void initialize(DateInRange constraintAnnotation) {
        DateTimeFormatter f = DateTimeFormatter.ofPattern("yyyy/MM/dd");
        this.min = LocalDate.parse(constraintAnnotation.min(), f);
        this.max = LocalDate.parse(constraintAnnotation.max(), f);
    }

    @Override
    public boolean isValid(LocalDate value, ConstraintValidatorContext context) {
        if (value == null) return true;
        if (value.isBefore(min)) return false;
        if (value.isAfter(max)) return false;
        return true;
    }
}

