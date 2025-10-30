package com.example.api.validation;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * 文字列が数字のみで構成されていないことを検証するアノテーション。
 * 氏名やタイトルのように「数字だけは禁止」としたいケースで利用します。
 */
@Target({FIELD, PARAMETER})
@Retention(RUNTIME)
@Constraint(validatedBy = NotNumericOnlyValidator.class)
@Documented
public @interface NotNumericOnly {
    /** メッセージキー。 */
    String message() default "{validation.text.notNumeric}";
    /** バリデーショングループ。 */
    Class<?>[] groups() default {};
    /** Payload。 */
    Class<? extends Payload>[] payload() default {};
}
