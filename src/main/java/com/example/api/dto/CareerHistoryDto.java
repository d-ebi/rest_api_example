package com.example.api.dto;

import com.example.api.openapi.OpenApiExamples;
import com.example.api.validation.NotNumericOnly;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

/**
 * 職歴情報DTO。
 */
@Schema(name = "CareerHistoryDto", description = "職歴情報",
        example = OpenApiExamples.Career.JSON)
@Data
public class CareerHistoryDto {
    /** 職歴ID。 */
    @Schema(description = "職歴ID", example = OpenApiExamples.Career.ID)
    private Long id;

    /** 職務タイトル（必須・1〜200文字）。 */
    @NotBlank(message = "{career.title.notBlank}")
    @Size(min = 1, max = 200, message = "{career.title.size}")
    @NotNumericOnly(message = "{career.title.notNumeric}")
    @Schema(description = "職務タイトル（1〜200文字）", example = OpenApiExamples.Career.TITLE, required = true, minLength = 1, maxLength = 200, pattern = ".*\\D.*")
    private String title;

    /** 従事期間（必須）。 */
    @NotNull(message = "{career.period.required}")
    @Valid
    @Schema(description = "期間", required = true)
    private PeriodDto period;
}
