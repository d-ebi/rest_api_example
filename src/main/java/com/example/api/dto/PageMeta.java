package com.example.api.dto;

import com.example.api.openapi.OpenApiExamples;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * ページングメタ情報DTO。
 */
@Schema(name = "PageMeta", description = "ページング情報",
        example = OpenApiExamples.Page.META_JSON)
@Data
public class PageMeta {
    /** 取得開始位置（0以上）。 */
    @Schema(description = "オフセット", example = OpenApiExamples.Page.OFFSET, minimum = "0")
    private int offset;

    /** 取得件数（0は全件相当）。 */
    @Schema(description = "取得件数", example = OpenApiExamples.Page.LIMIT, minimum = "0")
    private int limit;

    /** 総件数（ページング前）。 */
    @Schema(description = "総件数（ページング前）", example = OpenApiExamples.Page.TOTAL, minimum = "0")
    private int total;

    /** 次のページが存在するか。 */
    @Schema(description = "次のページが存在するか", example = OpenApiExamples.Page.HAS_NEXT)
    private boolean hasNext;
}
