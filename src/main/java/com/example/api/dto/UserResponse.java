package com.example.api.dto;

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
            },
            {
              "title": "Senior Engineer",
              "period": {
                "from": "2021/04/01",
                "to": "2024/03/31"
              }
            }
          ]
        }
        """)
@Data
@Builder(toBuilder = true)
public class UserResponse {
    /** 氏名。 */
    @Schema(description = "氏名", example = "Taro Yamada")
    private String name;

    /** 年齢。 */
    @Schema(description = "年齢", example = "30")
    private Integer age;

    /** 生年月日（yyyy/MM/dd）。 */
    @JsonFormat(pattern = "yyyy/MM/dd")
    @Schema(description = "生年月日（yyyy/MM/dd）", example = "1994/04/01")
    private LocalDate birthday;

    /** 身長。 */
    @Schema(description = "身長", example = "170.5")
    private BigDecimal height;

    /** 郵便番号。 */
    @Schema(description = "郵便番号", example = "123-4567")
    private String zipCode;

    /** 職歴一覧。 */
    @io.swagger.v3.oas.annotations.media.ArraySchema(arraySchema = @Schema(description = "職歴"), schema = @Schema(implementation = CareerHistoryDto.class))
    private List<CareerHistoryDto> careerHistories;
}

