package com.example.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 職歴情報の更新DTO。フィールドは任意で、指定された値のみ反映されます。
 */
@Schema(name = "CareerHistoryUpdateDto", description = "職歴情報（更新用）")
@Data
public class CareerHistoryUpdateDto {
    @Schema(description = "職歴ID", example = "1")
    private Long id;

    @Schema(description = "職務タイトル", example = "Senior Engineer")
    private String title;

    @Schema(description = "従事期間")
    private PeriodUpdateDto period;
}
