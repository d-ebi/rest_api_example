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
    /** ユーザーID。 */
    @Schema(description = "ユーザーID", example = OpenApiExamples.Users.ID)
    private Long id;

    /** 氏名。 */
    @Schema(description = "氏名", example = OpenApiExamples.Users.NAME)
    private String name;

    /** 年齢。 */
    @Schema(description = "年齢", example = OpenApiExamples.Users.AGE)
    private Integer age;

    /** 生年月日（yyyy/MM/dd）。 */
    @JsonFormat(pattern = "yyyy/MM/dd")
    @Schema(description = "生年月日（yyyy/MM/dd）", example = OpenApiExamples.Users.BIRTHDAY, pattern = "^(19|20)[0-9]{2}/(0[1-9]|1[0-2])/(0[1-9]|[12][0-9]|3[01])$", format = "yyyy/MM/dd", implementation = String.class)
    private LocalDate birthday;

    /** 身長。 */
    @Schema(description = "身長", example = OpenApiExamples.Users.HEIGHT, minimum = "0.0", maximum = "300.0", type = "number", format = "double", nullable = true)
    private BigDecimal height;

    /** 郵便番号。 */
    @Schema(description = "郵便番号", example = OpenApiExamples.Users.ZIP_CODE, minLength = 8, maxLength = 8, pattern = "^\\d{3}-\\d{4}$", nullable = true)
    private String zipCode;

    /** 職歴一覧。 */
    @io.swagger.v3.oas.annotations.media.ArraySchema(arraySchema = @Schema(description = "職歴"), schema = @Schema(implementation = CareerHistoryDto.class))
    private List<CareerHistoryDto> careerHistories;
}
