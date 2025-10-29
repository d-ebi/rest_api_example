package com.example.api.exception;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.Builder;

import java.util.List;

/**
 * 共通エラーレスポンスDTO。
 * 400/409/422など一部では詳細エラー配列を併せて返却します。
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(name = "ApiErrorResponse", description = "共通エラーレスポンスフォーマット")
@Data
@Builder(toBuilder = true)
public class ApiErrorResponse {
    @Schema(description = "エラーコード", example = "BAD_REQUEST")
    private String code;
    @Schema(description = "メッセージ", example = "Validation failed")
    private String message;
    @Schema(description = "トレースID", example = "f1c2d3e4-5678-90ab-cdef-1234567890ab")
    private String traceId;
    @ArraySchema(arraySchema = @Schema(description = "詳細エラー一覧"), schema = @Schema(implementation = FieldErrorDetail.class))
    private List<FieldErrorDetail> errors;

    public ApiErrorResponse() {}

    public ApiErrorResponse(String code, String message, String traceId) {
        this.code = code;
        this.message = message;
        this.traceId = traceId;
    }

    public ApiErrorResponse(String code, String message, String traceId, List<FieldErrorDetail> errors) {
        this.code = code;
        this.message = message;
        this.traceId = traceId;
        this.errors = errors;
    }

}
