package com.example.api.dto;

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
        example = """
        {
          "title": "Software Engineer",
          "period": {
            "from": "2018/04/01",
            "to": "2021/03/31"
          }
        }
        """)
@Data
public class CareerHistoryDto {
    /** 職務タイトル（必須・1〜200文字）。 */
    @NotBlank
    @Size(min = 1, max = 200)
    @NotNumericOnly
    @Schema(description = "職務タイトル（1〜200文字）", example = "Software Engineer", required = true, minLength = 1, maxLength = 200)
    private String title;

    /** 従事期間（必須）。 */
    @NotNull
    @Valid
    @Schema(description = "期間", required = true)
    private PeriodDto period;
}

