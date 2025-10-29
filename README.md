# REST API サンプル（Spring Boot 2.7 / Java 17 / SQLite）

このプロジェクトは、SQLite を利用した JSON 専用の REST API です。Actuator のヘルスチェックと、ビルド時に `api-docs.yml` を生成する OpenAPI 対応を含みます。

## 事前準備（初回のみ）

アプリ起動前に、データ保存用ディレクトリと SQLite DB を作成してください。

```console
# PowerShell / Bash いずれでも可
mkdir ./data
sqlite3 ./data/app.db "PRAGMA user_version = 1;"
```

上記で `./data/app.db` が作成され、起動時に `src/main/resources/schema.sql` が適用されます。

## ビルド

```
mvn clean package
```

OpenAPI の YAML は、生成タスク実行時に `target/api-docs.yml` へ出力されます。


```
mvn verify -DskipTests -Dspring-boot.start.wait=1000
```

## 起動

```
mvn spring-boot:run
```

アプリ起動後、DB は `./data/app.db` を使用します。テーブル定義は `src/main/resources/schema.sql` に基づいて作成されます。

## エンドポイント一覧（v1）

- GET `/api/v1/users`
  - クエリ: `name`(任意, 1–200), `limit`(0–100, 既定10), `offset`(0, 既定0)
  - 振る舞い: `name` 部分一致、`count` は非ページングの総件数
- POST `/api/v1/users`
  - 本文: ユーザー作成（JSON）
  - 成功時: 201 + `Location: /api/v1/users/{id}`
- GET `/api/v1/users/{user_id}`
- PUT `/api/v1/users/{user_id}`
  - 本文: ユーザー更新（JSON）
- DELETE `/api/v1/users/{user_id}`
- Actuator Health: GET `/actuator/health`

全エンドポイントの `Content-Type` / `Accept` は `application/json` を使用します（Health を除く）。

## サンプル curl

一覧（全件 or フィルタ）

```bash
curl -s "http://localhost:8080/api/v1/users" | jq .
curl -s "http://localhost:8080/api/v1/users?name=Tar&limit=5&offset=0" | jq .
```

作成（201, Location ヘッダ）

```bash
curl -i -X POST "http://localhost:8080/api/v1/users" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Taro Yamada",
    "age": 30,
    "birthday": "1994/04/01",
    "height": 170.5,
    "zipCode": "123-4567",
    "careerHistories": [
      {
        "title": "Software Engineer",
        "period": { "from": "2018/04/01", "to": "2021/03/31" }
      }
    ]
  }'
```

ID 取得（200）

```bash
curl -s "http://localhost:8080/api/v1/users/1" | jq .
```

更新（204）

```bash
curl -i -X PUT "http://localhost:8080/api/v1/users/1" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Taro Yamada",
    "age": 31,
    "birthday": "1994/04/01",
    "height": 171.0,
    "zipCode": "123-4567",
    "careerHistories": [
      {
        "title": "Senior Engineer",
        "period": { "from": "2021/04/01", "to": "2024/03/31" }
      }
    ]
  }'
```

削除（204）

```bash
curl -i -X DELETE "http://localhost:8080/api/v1/users/1"
```

ヘルスチェック

```bash
curl -s "http://localhost:8080/actuator/health" | jq .
```
