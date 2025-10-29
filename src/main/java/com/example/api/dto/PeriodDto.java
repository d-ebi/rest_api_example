package com.example.api.dto;

import com.example.api.validation.DateInRange;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.time.LocalDate;

/**
 * 期間情報DTO。
 */
@Schema(name = "PeriodDto", description = "期間情報",
        example = "{\"from\":\"2018/04/01\",\"to\":\"2021/03/31\"}")
@Data
public class PeriodDto {
    /** 期間の開始日（必須・yyyy/MM/dd）。 */
    @NotNull(message = "period.from is required")
    @JsonFormat(pattern = "yyyy/MM/dd")
    @DateInRange(min = "1900/01/01", max = "2099/12/31", message = "period.from out of range")
    @Schema(description = "開始日（yyyy/MM/dd）", example = "2018/04/01", required = true)
    private LocalDate from;

    /** 期間の終了日（必須・yyyy/MM/dd）。 */
    @NotNull(message = "period.to is required")
    @JsonFormat(pattern = "yyyy/MM/dd")
    @DateInRange(min = "1900/01/01", max = "2099/12/31", message = "period.to out of range")
    @Schema(description = "終了日（yyyy/MM/dd）", example = "2021/03/31", required = true)
    private LocalDate to;
}

