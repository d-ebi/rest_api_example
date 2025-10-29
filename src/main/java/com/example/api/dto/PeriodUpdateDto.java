package com.example.api.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDate;

/**
 * 期間情報の更新DTO。nullを許容し、指定された値のみ更新に利用します。
 */
@Schema(name = "PeriodUpdateDto", description = "期間情報（更新用）")
@Data
public class PeriodUpdateDto {
    @JsonFormat(pattern = "yyyy/MM/dd")
    @Schema(description = "開始日（yyyy/MM/dd）", example = "2018/04/01")
    private LocalDate from;

    @JsonFormat(pattern = "yyyy/MM/dd")
    @Schema(description = "終了日（yyyy/MM/dd）", example = "2021/03/31")
    private LocalDate to;
}
