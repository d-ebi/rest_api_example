package com.example.api.dto;

import com.example.api.openapi.OpenApiExamples;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * ユーザー取得レスポンスDTO。
 */
@Schema(name = "UserResponse", description = "ユーザー取得レスポンス",
        example = OpenApiExamples.Responses.USER_DETAIL)
@Data
@Builder(toBuilder = true)
public class UserResponse {
    /** 氏名。 */
    @Schema(description = "氏名", example = OpenApiExamples.Users.NAME)
    private String name;

    /** 年齢。 */
    @Schema(description = "年齢", example = OpenApiExamples.Users.AGE)
    private Integer age;

    /** 生年月日（yyyy/MM/dd）。 */
    @JsonFormat(pattern = "yyyy/MM/dd")
    @Schema(description = "生年月日（yyyy/MM/dd）", example = OpenApiExamples.Users.BIRTHDAY)
    private LocalDate birthday;

    /** 身長。 */
    @Schema(description = "身長", example = OpenApiExamples.Users.HEIGHT)
    private BigDecimal height;

    /** 郵便番号。 */
    @Schema(description = "郵便番号", example = OpenApiExamples.Users.ZIP_CODE)
    private String zipCode;

    /** 職歴一覧。 */
    @io.swagger.v3.oas.annotations.media.ArraySchema(arraySchema = @Schema(description = "職歴"), schema = @Schema(implementation = CareerHistoryDto.class))
    private List<CareerHistoryDto> careerHistories;
}
