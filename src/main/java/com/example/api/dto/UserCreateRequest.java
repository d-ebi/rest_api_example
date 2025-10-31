package com.example.api.dto;

import com.example.api.openapi.OpenApiExamples;
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
 * ユーザー作成のリクエストDTO。
 * 必須項目を含む完全な登録用ペイロードを表現します。
 */
@Schema(name = "UserCreateRequest", description = "ユーザー作成のリクエスト",
        example = OpenApiExamples.Requests.USER_CREATE)
@Data
public class UserCreateRequest {
    /** 氏名（必須・1〜200文字・数字のみ不可）。 */
    @NotBlank(message = "{user.name.notBlank}")
    @Size(min = 1, max = 200, message = "{user.name.size}")
    @NotNumericOnly(message = "{user.name.notNumeric}")
    @Schema(description = "氏名（1〜200文字、数字のみ不可）", example = OpenApiExamples.Users.NAME, required = true, minLength = 1, maxLength = 200, pattern = ".*\\D.*")
    private String name;

    /** 年齢（必須・0〜150）。 */
    @NotNull(message = "{user.age.required}")
    @Min(value = 0, message = "{user.age.min}")
    @Max(value = 150, message = "{user.age.max}")
    @Schema(description = "年齢（0〜150）", example = OpenApiExamples.Users.AGE, required = true, minimum = "0", maximum = "150")
    private Integer age;

    /** 生年月日（必須・yyyy/MM/dd・1900/01/01〜2099/12/31）。 */
    @NotNull(message = "{user.birthday.required}")
    @JsonFormat(pattern = "yyyy/MM/dd")
    @DateInRange(min = "1900/01/01", max = "2099/12/31", message = "{user.birthday.range}")
    @Schema(
            description = "生年月日（yyyy/MM/dd）",
            example = OpenApiExamples.Users.BIRTHDAY,
            required = true,
            pattern = "^(19|20)[0-9]{2}/(0[1-9]|1[0-2])/(0[1-9]|[12][0-9]|3[01])$",
            format = "yyyy/MM/dd",
            implementation = String.class
    )
    private LocalDate birthday;

    /** 身長（任意・整数部3桁・小数1桁・0.0〜300.0）。 */
    @Digits(integer = 3, fraction = 1, message = "{user.height.digits}")
    @DecimalMin(value = "0.0", message = "{user.height.min}")
    @DecimalMax(value = "300.0", message = "{user.height.max}")
    @Schema(description = "身長（整数3桁・小数1桁、0.0〜300.0）", example = OpenApiExamples.Users.HEIGHT, minimum = "0.0", maximum = "300.0", type = "number", format = "double", nullable = true)
    private BigDecimal height;

    /** 郵便番号（任意・8文字・000-0000形式）。 */
    @Pattern(regexp = "\\d{3}-\\d{4}", message = "{user.zip.pattern}")
    @Size(min = 8, max = 8, message = "{user.zip.size}")
    @Schema(description = "郵便番号（000-0000形式）", example = OpenApiExamples.Users.ZIP_CODE, minLength = 8, maxLength = 8, pattern = "^\\d{3}-\\d{4}$", nullable = true)
    private String zipCode;

    /** 職歴（任意・1〜50件）。空配列は不可。 */
    @Size(min = 1, max = 50, message = "{user.career.count}")
    @Valid
    @io.swagger.v3.oas.annotations.media.ArraySchema(
            arraySchema = @Schema(description = "職歴（最大50件）"),
            minItems = 1, maxItems = 50,
            schema = @Schema(implementation = CareerHistoryDto.class)
    )
    private List<CareerHistoryDto> careerHistories;
}
