package com.example.api.validation;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * {@link DateInRange} アノテーションの検証ロジック。
 * {@link java.time.LocalDate} の値を対象に、宣言された日付範囲へ収まっているか判定します。
 */
public class DateInRangeValidator implements ConstraintValidator<DateInRange, LocalDate> {
    private LocalDate min;
    private LocalDate max;

    /**
     * アノテーションから許容範囲を初期化します。
     *
     * @param constraintAnnotation 適用されたアノテーション
     */
    @Override
    public void initialize(DateInRange constraintAnnotation) {
        DateTimeFormatter f = DateTimeFormatter.ofPattern("yyyy/MM/dd");
        this.min = LocalDate.parse(constraintAnnotation.min(), f);
        this.max = LocalDate.parse(constraintAnnotation.max(), f);
    }

    /**
     * 値が範囲内に収まるかを検証します。
     *
     * @param value   入力値
     * @param context バリデーションコンテキスト
     * @return 範囲内の場合はtrue
     */
    @Override
    public boolean isValid(LocalDate value, ConstraintValidatorContext context) {
        if (value == null) return true;
        if (value.isBefore(min)) return false;
        if (value.isAfter(max)) return false;
        return true;
    }
}
