package com.example.api.openapi;

import com.example.api.exception.ErrorCatalog;

/**
 * OpenAPIのexample属性で利用する値を一元管理する定義クラス。
 */
public final class OpenApiExamples {
    private OpenApiExamples() {
    }

    public static final class Users {
        private Users() {
        }

        public static final String NAME = "Taro Yamada";
        public static final String AGE = "30";
        public static final String AGE_UPDATE = "31";
        public static final String BIRTHDAY = "1994/04/01";
        public static final String HEIGHT = "170.5";
        public static final String HEIGHT_UPDATE = "171.0";
        public static final String ZIP_CODE = "123-4567";
        public static final String SEARCH_NAME = "Tar";
        public static final String ID = "1";
    }

    public static final class Career {
        private Career() {
        }

        public static final String TITLE = "Software Engineer";
        public static final String TITLE_UPDATE = "Senior Engineer";
        public static final String ID = "1";
        public static final String JSON = """
                {
                  "id": 1,
                  "title": "Software Engineer",
                  "period": {
                    "from": "2018/04/01",
                    "to": "2021/03/31"
                  }
                }
                """;
    }

    public static final class Period {
        private Period() {
        }

        public static final String RANGE_JSON = """
                {
                  "from": "2018/04/01",
                  "to": "2021/03/31"
                }
                """;
        public static final String FROM = "2018/04/01";
        public static final String TO = "2021/03/31";
        public static final String FROM_UPDATE = "2021/04/01";
        public static final String TO_UPDATE = "2024/03/31";
    }

    public static final class Page {
        private Page() {
        }

        public static final String OFFSET = "0";
        public static final String LIMIT = "10";
        public static final String TOTAL = "25";
        public static final String HAS_NEXT = "true";
        public static final String META_JSON = """
                {
                  "offset": 0,
                  "limit": 10,
                  "total": 25,
                  "hasNext": true
                }
                """;
    }

    public static final class Requests {
        private Requests() {
        }

        public static final String USER_CREATE = """
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
                """;

        public static final String USER_UPDATE = """
                {
                  "name": "Taro Yamada",
                  "age": 31,
                  "birthday": "1994/04/01",
                  "height": 171.0,
                  "zipCode": "123-4567",
                  "careerHistories": [
                    {
                      "id": 2,
                      "title": "Senior Engineer",
                      "period": {
                        "from": "2021/04/01",
                        "to": "2024/03/31"
                      }
                    }
                  ]
                }
                """;
    }

    public static final class Responses {
        private Responses() {
        }

        public static final String USER_LIST = """
                {
                  "count": 25,
                  "page": {"offset":0,"limit":10,"total":25,"hasNext":true},
                  "users": [
                    {
                      "id": 1,
                      "name": "Taro Yamada",
                      "age": 30,
                      "birthday": "1994/04/01",
                      "height": 170.5,
                      "zipCode": "123-4567",
                      "careerHistories": [
                        {
                          "id": 1,
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
                """;

        public static final String USER_DETAIL = """
                {
                  "id": 1,
                  "name": "Taro Yamada",
                  "age": 30,
                  "birthday": "1994/04/01",
                  "height": 170.5,
                  "zipCode": "123-4567",
                  "careerHistories": [
                    {
                      "id": 1,
                      "title": "Software Engineer",
                      "period": {
                        "from": "2018/04/01",
                        "to": "2021/03/31"
                      }
                    },
                    {
                      "id": 2,
                      "title": "Senior Engineer",
                      "period": {
                        "from": "2021/04/01",
                        "to": "2024/03/31"
                      }
                    }
                  ]
                }
                """;
    }

    public static final class Headers {
        private Headers() {
        }

        public static final String LOCATION = "/api/v1/users/1";
    }

    public static final class ErrorResponses {
        private ErrorResponses() {
        }

        public static final String BAD_REQUEST = """
                {
                  "code": "BAD_REQUEST",
                  "message": "入力内容に誤りがあります",
                  "traceId": "aaaaaaaa-bbbb-cccc-dddd-eeeeeeeeeeee",
                  "errors": [
                    {
                      "code": "VALIDATION_ERROR",
                      "reason": "limitは0〜100の範囲で指定してください",
                      "location": "query",
                      "field": "limit",
                      "constraints": {
                        "invalidValue": "-1",
                        "expectedType": "Integer"
                      }
                    }
                  ]
                }
                """;

        public static final String UNPROCESSABLE_ENTITY = """
                {
                  "code": "UNPROCESSABLE_ENTITY",
                  "message": "リクエスト内容に不備があります",
                  "traceId": "bbbbbbbb-cccc-dddd-eeee-ffffffffffff",
                  "errors": [
                    {
                      "code": "INVALID_PERIOD",
                      "reason": "period.fromはperiod.to以前の日付を指定してください",
                      "location": "body",
                      "field": "careerHistories.period",
                      "constraints": {
                        "invalidPeriod": true
                      }
                    }
                  ]
                }
                """;

        public static final String NOT_FOUND = """
                {
                  "code": "NOT_FOUND",
                  "message": "リソースが見つかりません",
                  "traceId": "cccccccc-dddd-eeee-ffff-000000000000"
                }
                """;

        public static final String METHOD_NOT_ALLOWED = """
                {
                  "code": "METHOD_NOT_ALLOWED",
                  "message": "許可されていないHTTPメソッドです",
                  "traceId": "dddddddd-eeee-ffff-0000-111111111111"
                }
                """;

        public static final String NOT_ACCEPTABLE = """
                {
                  "code": "NOT_ACCEPTABLE",
                  "message": "受理できないメディアタイプです",
                  "traceId": "eeeeeeee-ffff-0000-1111-222222222222"
                }
                """;

        public static final String CONFLICT = """
                {
                  "code": "CONFLICT",
                  "message": "リソースが重複しています",
                  "traceId": "ffffffff-0000-1111-2222-333333333333",
                  "errors": [
                    {
                      "code": "DUPLICATE",
                      "reason": "nameは既に存在しています",
                      "location": "body",
                      "field": "name",
                      "constraints": {
                        "unique": true
                      }
                    }
                  ]
                }
                """;

        public static final String INTERNAL_SERVER_ERROR = """
                {
                  "code": "INTERNAL_SERVER_ERROR",
                  "message": "予期しないエラーが発生しました",
                  "traceId": "99999999-aaaa-bbbb-cccc-dddddddddddd"
                }
                """;
    }

    public static final class Errors {
        private Errors() {
        }

        public static final String CODE = ErrorCatalog.Codes.BAD_REQUEST;
        public static final String MESSAGE = ErrorCatalog.Messages.VALIDATION_FAILED;
        public static final String TRACE_ID = "f1c2d3e4-5678-90ab-cdef-1234567890ab";
        public static final String DETAIL_CODE = ErrorCatalog.DetailCodes.VALIDATION_ERROR;
        public static final String DETAIL_REASON = "氏名は必須です";
        public static final String DETAIL_FIELD = "name";
        public static final String DETAIL_LOCATION = "body";
        public static final String DETAIL_CONSTRAINTS = """
                {
                  "min": 1,
                  "max": 200
                }
                """;
    }
}
