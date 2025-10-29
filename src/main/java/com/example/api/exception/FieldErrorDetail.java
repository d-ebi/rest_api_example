package com.example.api.exception;

import com.example.api.openapi.OpenApiExamples;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.Builder;

/**
 * バリデーション等の詳細エラー項目。
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(name = "FieldErrorDetail", description = "バリデーション等の詳細エラー")
@Data
@Builder(toBuilder = true)
public class FieldErrorDetail {
    @Schema(description = "詳細エラーコード", example = OpenApiExamples.Errors.DETAIL_CODE)
    private String code;
    @Schema(description = "理由（要求仕様に合わせキー名はreson）", example = OpenApiExamples.Errors.DETAIL_REASON)
    private String reson; // intentionally matching requested key spelling
    @Schema(description = "対象フィールド", example = OpenApiExamples.Errors.DETAIL_FIELD)
    private String field;
    @Schema(description = "制約情報（任意、構造は実装依存）", example = OpenApiExamples.Errors.DETAIL_CONSTRAINTS)
    private Object constraints;

    public FieldErrorDetail() {}

    public FieldErrorDetail(String code, String reson, String field, Object constraints) {
        this.code = code;
        this.reson = reson;
        this.field = field;
        this.constraints = constraints;
    }

}
