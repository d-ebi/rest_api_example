package com.example.api.dto;

import com.example.api.openapi.OpenApiExamples;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.Valid;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * ユーザー更新のリクエストDTO。未指定項目は変更しません。
 */
@Schema(name = "UserUpdateRequest", description = "ユーザー更新のリクエスト",
        example = OpenApiExamples.Requests.USER_UPDATE)
@Data
public class UserUpdateRequest {
    @Schema(description = "氏名", example = OpenApiExamples.Users.NAME, minLength = 1, maxLength = 200, pattern = ".*\\D.*")
    private String name;

    @Schema(description = "年齢", example = OpenApiExamples.Users.AGE, minimum = "0", maximum = "150")
    private Integer age;

    @JsonFormat(pattern = "yyyy/MM/dd")
    @Schema(description = "生年月日（yyyy/MM/dd）", example = OpenApiExamples.Users.BIRTHDAY, pattern = "^(19|20)[0-9]{2}/(0[1-9]|1[0-2])/(0[1-9]|[12][0-9]|3[01])$", format = "yyyy/MM/dd", implementation = String.class)
    private LocalDate birthday;

    @Schema(description = "身長", example = OpenApiExamples.Users.HEIGHT, minimum = "0.0", maximum = "300.0", type = "number", format = "double", nullable = true)
    private BigDecimal height;

    @Schema(description = "郵便番号", example = OpenApiExamples.Users.ZIP_CODE, minLength = 8, maxLength = 8, pattern = "^\\d{3}-\\d{4}$", nullable = true)
    private String zipCode;

    @Valid
    @io.swagger.v3.oas.annotations.media.ArraySchema(arraySchema = @Schema(description = "更新対象職歴一覧"), schema = @Schema(implementation = CareerHistoryUpdateDto.class))
    private List<CareerHistoryUpdateDto> careerHistories;
}
