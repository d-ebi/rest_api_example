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
    @Schema(description = "氏名", example = OpenApiExamples.Users.NAME)
    private String name;

    @Schema(description = "年齢", example = OpenApiExamples.Users.AGE)
    private Integer age;

    @JsonFormat(pattern = "yyyy/MM/dd")
    @Schema(description = "生年月日（yyyy/MM/dd）", example = OpenApiExamples.Users.BIRTHDAY)
    private LocalDate birthday;

    @Schema(description = "身長", example = OpenApiExamples.Users.HEIGHT)
    private BigDecimal height;

    @Schema(description = "郵便番号", example = OpenApiExamples.Users.ZIP_CODE)
    private String zipCode;

    @Valid
    @io.swagger.v3.oas.annotations.media.ArraySchema(arraySchema = @Schema(description = "更新対象職歴一覧"), schema = @Schema(implementation = CareerHistoryUpdateDto.class))
    private List<CareerHistoryUpdateDto> careerHistories;
}
