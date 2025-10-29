package com.example.api.validation;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.*;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Target({FIELD})
@Retention(RUNTIME)
@Constraint(validatedBy = DateInRangeValidator.class)
@Documented
public @interface DateInRange {
    String message() default "{validation.date.range}";
    String min(); // inclusive, format yyyy/MM/dd
    String max(); // inclusive, format yyyy/MM/dd
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
