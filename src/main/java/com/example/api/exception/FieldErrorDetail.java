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
    @Schema(description = "理由", example = OpenApiExamples.Errors.DETAIL_REASON)
    private String reason;
    @Schema(description = "対象フィールド", example = OpenApiExamples.Errors.DETAIL_FIELD)
    private String field;
    @Schema(description = "エラー発生箇所", example = "body")
    private String location;
    @Schema(description = "制約情報（任意、構造は実装依存）", example = OpenApiExamples.Errors.DETAIL_CONSTRAINTS)
    private Object constraints;

    public FieldErrorDetail() {}

    public FieldErrorDetail(String code, String reason, String field, String location, Object constraints) {
        this.code = code;
        this.reason = reason;
        this.field = field;
        this.location = location;
        this.constraints = constraints;
    }

}
