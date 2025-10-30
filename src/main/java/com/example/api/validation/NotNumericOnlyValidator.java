package com.example.api.validation;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

/**
 * {@link NotNumericOnly} の検証ロジック。
 * nullや空白は許容し、数字以外の文字が1つ以上含まれているかをチェックします。
 */
public class NotNumericOnlyValidator implements ConstraintValidator<NotNumericOnly, String> {
    /**
     * 値が数字のみで構成されていないか判定します。
     *
     * @param value   入力値
     * @param context バリデーションコンテキスト
     * @return 数字のみでなければtrue
     */
    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null) return true; // optional fields allowed
        String trimmed = value.trim();
        if (trimmed.isEmpty()) return true; // handled elsewhere with @NotBlank when required
        return trimmed.matches(".*\\D.*");
    }
}
