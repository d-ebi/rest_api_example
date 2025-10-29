package com.example.api.dto;

import com.example.api.openapi.OpenApiExamples;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * ユーザー一覧レスポンスDTO。
 * 総件数・ページ情報・ユーザー配列を含みます。
 */
@Schema(name = "UserListResponse", description = "ユーザー一覧レスポンス",
        example = OpenApiExamples.Responses.USER_LIST)
@Data
public class UserListResponse {
    /** 総件数（ページング前の全体件数）。 */
    @Schema(description = "総件数（ページング前）", example = OpenApiExamples.Page.TOTAL)
    private int count;

    /** ページング情報。 */
    @Schema(description = "ページング情報")
    private PageMeta page;

    /** ユーザー一覧。 */
    @io.swagger.v3.oas.annotations.media.ArraySchema(arraySchema = @Schema(description = "ユーザー一覧"), schema = @Schema(implementation = UserResponse.class))
    private List<UserResponse> users;
}
