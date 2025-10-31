package com.example.api.dto;

import com.example.api.openapi.OpenApiExamples;
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
        example = OpenApiExamples.Period.RANGE_JSON)
@Data
public class PeriodDto {
    /** 期間の開始日（必須・yyyy/MM/dd）。 */
    @NotNull(message = "{period.from.required}")
    @JsonFormat(pattern = "yyyy/MM/dd")
    @DateInRange(min = "1900/01/01", max = "2099/12/31", message = "{period.from.range}")
    @Schema(description = "開始日（yyyy/MM/dd）", example = OpenApiExamples.Period.FROM, required = true, pattern = "^(19|20)[0-9]{2}/(0[1-9]|1[0-2])/(0[1-9]|[12][0-9]|3[01])$", format = "yyyy/MM/dd", implementation = String.class)
    private LocalDate from;

    /** 期間の終了日（必須・yyyy/MM/dd）。 */
    @NotNull(message = "{period.to.required}")
    @JsonFormat(pattern = "yyyy/MM/dd")
    @DateInRange(min = "1900/01/01", max = "2099/12/31", message = "{period.to.range}")
    @Schema(description = "終了日（yyyy/MM/dd）", example = OpenApiExamples.Period.TO, required = true, pattern = "^(19|20)[0-9]{2}/(0[1-9]|1[0-2])/(0[1-9]|[12][0-9]|3[01])$", format = "yyyy/MM/dd", implementation = String.class)
    private LocalDate to;
}
