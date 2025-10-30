package com.example.api.exception;

import com.example.api.openapi.OpenApiExamples;
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
    @Schema(description = "メッセージ", example = "入力内容に誤りがあります")
    private String message;
    @Schema(description = "トレースID", example = "f1c2d3e4-5678-90ab-cdef-1234567890ab")
    private String traceId;
    @ArraySchema(arraySchema = @Schema(description = "詳細エラー一覧"), schema = @Schema(implementation = FieldErrorDetail.class))
    private List<FieldErrorDetail> errors;

    /** デフォルトコンストラクタ（シリアライザ向け）。 */
    public ApiErrorResponse() {}

    /**
     * 詳細エラーなしのレスポンスを生成します。
     *
     * @param code    エラーコード
     * @param message メッセージ
     * @param traceId トレースID
     */
    public ApiErrorResponse(String code, String message, String traceId) {
        this.code = code;
        this.message = message;
        this.traceId = traceId;
    }

    /**
     * 詳細エラー付きレスポンスを生成します。
     *
     * @param code    エラーコード
     * @param message メッセージ
     * @param traceId トレースID
     * @param errors  詳細エラー一覧
     */
    public ApiErrorResponse(String code, String message, String traceId, List<FieldErrorDetail> errors) {
        this.code = code;
        this.message = message;
        this.traceId = traceId;
        this.errors = errors;
    }

}
