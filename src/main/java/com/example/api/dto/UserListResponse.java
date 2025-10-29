package com.example.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * ユーザー一覧レスポンスDTO。
 * 総件数・ページ情報・ユーザー配列を含みます。
 */
@Schema(name = "UserListResponse", description = "ユーザー一覧レスポンス",
        example = """
        {
          "count": 25,
          "page": {"offset":0,"limit":10,"total":25,"hasNext":true},
          "users": [
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
                }
              ]
            }
          ]
        }
        """)
@Data
public class UserListResponse {
    /** 総件数（ページング前の全体件数）。 */
    @Schema(description = "総件数（ページング前）", example = "25")
    private int count;

    /** ページング情報。 */
    @Schema(description = "ページング情報")
    private PageMeta page;

    /** ユーザー一覧。 */
    @io.swagger.v3.oas.annotations.media.ArraySchema(arraySchema = @Schema(description = "ユーザー一覧"), schema = @Schema(implementation = UserResponse.class))
    private List<UserResponse> users;
}
