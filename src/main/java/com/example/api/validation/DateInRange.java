package com.example.api.validation;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.*;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * 日付型のフィールドに対して許容する範囲を指定するバリデーションアノテーション。
 * {@code yyyy/MM/dd} 形式の文字列で包含範囲（最小値・最大値）を指定します。
 */
@Target({FIELD})
@Retention(RUNTIME)
@Constraint(validatedBy = DateInRangeValidator.class)
@Documented
public @interface DateInRange {
    /** メッセージキー。 */
    String message() default "{validation.date.range}";
    /** 最小値（含む）。形式: yyyy/MM/dd */
    String min();
    /** 最大値（含む）。形式: yyyy/MM/dd */
    String max();
    /** バリデーショングループ。 */
    Class<?>[] groups() default {};
    /** Payload。 */
    Class<? extends Payload>[] payload() default {};
}
