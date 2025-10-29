package com.example.api.dto;

import com.example.api.validation.DateInRange;
import com.example.api.validation.NotNumericOnly;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.Valid;
import javax.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * ユーザー作成/更新のリクエストDTO。
 * バリデーションアノテーションで各フィールドの制約を表現します。
 */
@Schema(name = "UserRequest", description = "ユーザー作成/更新のリクエスト",
        example = """
        {
          "name": "Taro Yamada",
          "age": 30,
          "birthday": "1994/04/01",
          "height": 170.5,
          "zipCode": "123-4567",
          "careerHistories": [
            {
              "title": "Software Engineer",
              "period": {
                "from": "2018/04/01",
                "to": "2021/03/31"
              }
            }
          ]
        }
        """)
@Data
public class UserRequest {
    /**
     * 氏名（必須・1〜200文字・数字のみ不可）。
     */
    @NotBlank
    @Size(min = 1, max = 200)
    @NotNumericOnly
    @Schema(description = "氏名（1〜200文字、数字のみ不可）", example = "Taro Yamada", required = true, minLength = 1, maxLength = 200)
    private String name;

    /**
     * 年齢（必須・0〜150）。
     */
    @NotNull
    @Min(0)
    @Max(150)
    @Schema(description = "年齢（0〜150）", example = "30", required = true, minimum = "0", maximum = "150")
    private Integer age;

    /**
     * 生年月日（必須・yyyy/MM/dd・1900/01/01〜2099/12/31）。
     */
    @NotNull
    @JsonFormat(pattern = "yyyy/MM/dd")
    @DateInRange(min = "1900/01/01", max = "2099/12/31", message = "birthday out of range")
    @Schema(description = "生年月日（yyyy/MM/dd）", example = "1994/04/01", required = true, pattern = "^[0-9]{4}/[0-9]{2}/[0-9]{2}$")
    private LocalDate birthday;

    /**
     * 身長（任意・整数部3桁・小数1桁・0.0〜300.0）。
     */
    @Digits(integer = 3, fraction = 1)
    @DecimalMin(value = "0.0")
    @DecimalMax(value = "300.0")
    @Schema(description = "身長（整数3桁・小数1桁、0.0〜300.0）", example = "170.5", minimum = "0.0", maximum = "300.0", type = "number", format = "double")
    private BigDecimal height; // optional

    /**
     * 郵便番号（任意・8文字・000-0000形式）。
     */
    @Pattern(regexp = "\\d{3}-\\d{4}")
    @Size(min = 8, max = 8)
    @Schema(description = "郵便番号（000-0000形式）", example = "123-4567", minLength = 8, maxLength = 8, pattern = "^\\d{3}-\\d{4}$")
    private String zipCode; // optional

    /**
     * 職歴（任意・1〜50件）。空配列は不可。
     */
    @Size(min = 1, max = 50)
    @Valid
    @io.swagger.v3.oas.annotations.media.ArraySchema(
            arraySchema = @Schema(description = "職歴（最大50件）"),
            minItems = 1, maxItems = 50,
            schema = @Schema(implementation = CareerHistoryDto.class)
    )
    private List<CareerHistoryDto> careerHistories; // optional but not empty

}
