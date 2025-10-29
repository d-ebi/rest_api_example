package com.example.api.controller;

import com.example.api.dto.PageMeta;
import com.example.api.dto.UserListResponse;
import com.example.api.dto.UserRequest;
import com.example.api.dto.UserResponse;
import com.example.api.exception.ApiErrorResponse;
import com.example.api.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.headers.Header;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.Size;
import java.net.URI;
import java.util.List;

import lombok.RequiredArgsConstructor;

/**
 * ユーザーリソースのRESTコントローラ。
 * JSONのみを取り扱い、一覧取得・作成・更新・削除・単一取得を提供します。
 */
@RestController
@RequestMapping(value = "/api/v1/users", produces = MediaType.APPLICATION_JSON_VALUE)
@Validated
@Tag(name = "Users")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    /**
     * ユーザーの一覧を取得します。
     * @param name 名前の部分一致フィルタ（任意）
     * @param limit 取得件数（0〜100、既定値10）
     * @param offset オフセット（0以上、既定値0）
     * @return ページ情報とユーザーの配列を含むレスポンス
     */
    @GetMapping
    @Operation(summary = "ユーザー一覧取得")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OK",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = UserListResponse.class),
                            examples = @ExampleObject(value = """
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
                            """))),
            @ApiResponse(responseCode = "400", description = "不正なパラメータ",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "対象なし",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "405", description = "メソッド不許可",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "406", description = "Not Acceptable",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "422", description = "処理不能（例: 期間の矛盾）",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "サーバエラー",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    public ResponseEntity<UserListResponse> list(
            @Parameter(description = "名前の部分一致フィルタ", example = "Tar",
                    schema = @Schema(minLength = 1, maxLength = 200, pattern = ".*\\D.*"))
            @RequestParam(name = "name", required = false)
            @Size(min = 1, max = 200)
            @javax.validation.constraints.Pattern(regexp = ".*\\D.*", message = "must contain at least one non-digit") String name,
            @Parameter(description = "取得件数", example = "10",
                    schema = @Schema(minimum = "0", maximum = "100"))
            @RequestParam(name = "limit", defaultValue = "10") @Min(0) @Max(100) int limit,
            @Parameter(description = "開始オフセット", example = "0",
                    schema = @Schema(minimum = "0"))
            @RequestParam(name = "offset", defaultValue = "0") @Min(0) int offset
    ) {
        int totalCount = userService.count(name);
        List<UserResponse> userResponses = userService.list(name, limit, offset);
        PageMeta pageMeta = new PageMeta();
        pageMeta.setLimit(limit);
        pageMeta.setOffset(offset);
        pageMeta.setTotal(totalCount);
        pageMeta.setHasNext(offset + limit < totalCount);
        UserListResponse userListResponse = new UserListResponse();
        userListResponse.setCount(totalCount);
        userListResponse.setPage(pageMeta);
        userListResponse.setUsers(userResponses);
        return ResponseEntity.ok(userListResponse);
    }

    /**
     * ユーザーを新規作成します。
     * @param userRequest ユーザー作成リクエストボディ
     * @return Locationヘッダのみを含む201応答
     */
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "ユーザー作成")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Created",
                    headers = {@Header(name = "Location", description = "作成したリソースのURI",
                            schema = @Schema(type = "string", example = "/api/v1/users/1"))}),
            @ApiResponse(responseCode = "400", description = "不正なリクエスト",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "重複（name一意制約）",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "422", description = "処理不能（期間エラーなど）",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "405", description = "メソッド不許可",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "406", description = "Not Acceptable",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "サーバエラー",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    @io.swagger.v3.oas.annotations.parameters.RequestBody(required = true,
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = UserRequest.class),
                    examples = @ExampleObject(value = """
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
                    """)))
    public ResponseEntity<Void> create(@Valid @RequestBody UserRequest userRequest) {
        Long createdUserId = userService.create(userRequest);
        HttpHeaders headers = new HttpHeaders();
        headers.setLocation(URI.create("/api/v1/users/" + createdUserId));
        return new ResponseEntity<>(headers, HttpStatus.CREATED);
    }

    /**
     * 指定IDのユーザーを更新します。
     * @param userId ユーザーID
     * @param userRequest ユーザー更新リクエストボディ
     * @return 本文なしの204応答
     */
    @PutMapping(value = "/{user_id}", consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "ユーザー更新")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "No Content"),
            @ApiResponse(responseCode = "400", description = "不正なリクエスト",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "405", description = "メソッド不許可",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "406", description = "Not Acceptable",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "サーバエラー",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    @io.swagger.v3.oas.annotations.parameters.RequestBody(required = true,
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = UserRequest.class),
                    examples = @ExampleObject(value = """
                    {
                      "name": "Taro Yamada",
                      "age": 31,
                      "birthday": "1994/04/01",
                      "height": 171.0,
                      "zipCode": "123-4567",
                      "careerHistories": [
                        {
                          "title": "Senior Engineer",
                          "period": {
                            "from": "2021/04/01",
                            "to": "2024/03/31"
                          }
                        }
                      ]
                    }
                    """)))
    public ResponseEntity<Void> update(@PathVariable("user_id") Long userId, @Valid @RequestBody UserRequest userRequest) {
        userService.update(userId, userRequest);
        return ResponseEntity.noContent().build();
    }

    /**
     * 指定IDのユーザーを削除します。
     * @param userId ユーザーID
     * @return 本文なしの204応答
     */
    @DeleteMapping(value = "/{user_id}")
    @Operation(summary = "ユーザー削除")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "No Content"),
            @ApiResponse(responseCode = "400", description = "不正なリクエスト",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "405", description = "メソッド不許可",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "406", description = "Not Acceptable",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "サーバエラー",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    public ResponseEntity<Void> delete(@PathVariable("user_id") Long userId) {
        userService.delete(userId);
        return ResponseEntity.noContent().build();
    }

    /**
     * 指定IDのユーザーを取得します。
     * @param userId ユーザーID
     * @return ユーザー情報
     */
    @GetMapping(value = "/{user_id}")
    @Operation(summary = "ユーザー取得")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OK",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = UserResponse.class),
                            examples = @ExampleObject(value = """
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
                            """))),
            @ApiResponse(responseCode = "400", description = "不正なリクエスト",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "対象なし",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "405", description = "メソッド不許可",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "406", description = "Not Acceptable",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "サーバエラー",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    public ResponseEntity<UserResponse> get(@PathVariable("user_id") Long userId) {
        UserResponse userResponse = userService.get(userId);
        return ResponseEntity.ok(userResponse);
    }
}
