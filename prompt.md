以下の内容でSpring Bootプロジェクトを作成してください。また、CREATE TABLE文もリクエスト/レスポンス内容を参照し作成してください。

# プロジェクト概要
* Spring Boot2.7系の最新
* Java 17
* REST API
* ログイン機能なし
* Spring Actuatorはヘルスチェックのみ有効
* OpenAPIに対応（ビルド時に出力、名称は`api-docs.yml`)
* リクエスト/レスポンスは全て`application/json`
* データベースはSQLite

# 共通エラーレスポンス
```json
{
  "code": "string",
  "message": "string",
  "traceId": "string"
}
```

# 400,409,442の個別レスポンス
```json
{
  "code": "string",
  "message": "string",
  "traceId": "string",
  "errors": [
    "code": "string",
    "reason": "string",
    "field": "string",
    "constraints": "object"
  ]
}
```

# エンドポイント
## GET /api/v1/users
### とりうるHTTP Status
* 200
* 400
* 404
* 405
* 406
* 422
* 500

### クエリ文字列
`name={string}&limit={integer}&offset={integer}`

### レスポンス(200)
#### Header
追加情報なし

#### Body
```json
{
  "count": "integer",
  "page": {
    "offset": "integer",
    "limit": "integer",
    "total": "integer",
    "hasNext": true
  }
  users: [
    {
      "name": "string",
      "age" : "integer",
      "birthday": "string",
      "height": "number",
      "zipCode": "string",
      "careerHistories": [
        {
          "title": "string",
          "period": {
            "from": "string",
            "to": "string"
          }
        }
      ]
    }
  ]
}
```
### 制約
#### name
* String
* 任意
* Null非許容
* 空文字（スペースのみ含む）非許容
* 数値非許容
* 最小1文字
* 最大200文字

### limit
* Integer
* 任意
* デフォルト10（省略時）
* Null非許容
* 最小0
* 最大100

### offset
* Integer
* 任意
* デフォルト0（省略時）
* Null非許容
* 最小0

### 仕様
* `name`を指定可能、部分一致検索
* `name`を省略した場合は全件取得
* `count`は非ページング時の総数

## POST /api/v1/users
### とりうるHTTP Status
* 201
* 400
* 405
* 406
* 409
* 422
* 500

### リクエストボディ
```json
{
  "name": "string",
  "age" : "integer",
  "birthday": "string",
  "height": "number",
  "zipCode": "string",
  "careerHistories": [
    {
      "title": "string",
      "period": {
        "from": "string",
        "to": "string"
      }
    }
  ]
}
```

### レスポンス(201)
#### Header
Location: {URI}

#### Body
空

### 制約
#### name
* String
* 必須
* Null非許容
* 空文字（スペースのみ含む）非許容
* 数値非許容
* 最小1文字
* 最大200文字
* 重複不可

#### age
* Integer
* 必須
* Null非許容
* 整数のみ許容
* 最小0
* 最大150

#### birthday
* Date
* 必須
* Null非許容
* yyyy/MM/dd
* 最小1900/01/01
* 最大2099/12/31

#### height
* Double
* 任意
* Null非許容
* 整数部最大3桁
* 小数部1桁
* 最小0.0
* 最大300.0

#### zipCode
* String
* 任意
* Null非許容
* 空文字（スペースのみ含む）非許容
* 文字列数8桁
* 000-0000の形式のみ許容

#### careerHistories
* List
* 任意
* 空リスト非許容
* 最大サイズ50

#### title
* String
* 必須
* Null非許容
* 空文字（スペースのみ含む）非許容
* 数値非許容
* 最小1文字
* 最大200文字

#### period
* Object
* 必須
* Null非許容

#### period.from
* Date
* 必須
* Null非許容
* yyyy/MM/dd
* 最小1900/01/01
* 最大2099/12/31

#### period.to
* Date
* 必須
* Null非許容
* yyyy/MM/dd
* 最小1900/01/01
* 最大2099/12/31

### その他の制約
period.from > period.toはhttpStatus=422

## PUT /api/v1/users/{user_id}
### とりうるHTTP Status
* 204
* 400
* 405
* 406
* 500

### リクエストボディ
POSTと同様

### レスポンス(204)
#### Header
追加情報なし

#### Body
空

## DELETE /api/v1/users/{user_id}
### とりうるHTTP Status
* 204
* 400
* 405
* 406
* 500

### リクエストボディ
なし

### レスポンス(204)
#### Header
追加情報なし

#### Body
空

## GET /api/v1/users/{user_id}
### とりうるHTTP Status
* 200
* 400
* 404
* 405
* 406
* 500

### レスポンス(204)
#### Header
追加情報なし

#### Body
```json
{
  "name": "string",
  "age" : "integer",
  "birthday": "string",
  "height": "number",
  "zipCode": "string",
  "careerHistories": [
    {
      "title": "string",
      "period": {
        "from": "string",
        "to": "string"
      }
    }
  ]
}
```
