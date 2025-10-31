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
mvn verify -Dspring-boot.start.wait=1000
```

## 起動
### 通常起動
```
mvn spring-boot:run
```

### 構造化ログ
```
mvn spring-boot:run -Dspring-boot.run.profiles=structured
```

アプリ起動後、DB は `./data/app.db` を使用します。テーブル定義は `src/main/resources/schema.sql` に基づいて作成されます。

## Swagger

起動後、以下にアクセスすることでSwagger UIにアクセスすることができます。

http://localhost:8080/swagger-ui.html

## Dreddによる契約テスト

Spring Boot が生成する OpenAPI 3.0 ドキュメントは Dredd が未対応のメタ情報を含むため、Dredd 実行時に警告が表示されていました。`dredd/api-docs-dredd.yml` には Dredd 向けに不要な属性を除いたサニタイズ済みの OpenAPI を用意しています。以下の手順で警告無しに契約テストを実行できます。

```
# SQLite を初期化
rm -f data/app.db
sqlite3 data/app.db "PRAGMA user_version = 1;"

# Spring Boot をバックグラウンド起動
mvn spring-boot:run > spring.log 2>&1 & SERVER_PID=$!

# Dredd を実行（Node.js の内部警告も抑止）
NODE_NO_WARNINGS=1 dredd dredd/api-docs-dredd.yml http://localhost:8080/ \
  --hookfiles=hooks.js --no-color

# テスト完了後にアプリを停止
kill $SERVER_PID
```

実行結果は `dredd.log` などにリダイレクトすることで保存できます。

## jar起動
起動時は以下のように起動することで、構造化ログの出力有無、Swagger UIの提供有無を切り替えられます。

```
java -Dspring-boot.run.profiles=structured -jar target/rest-api-example-0.0.1-SNAPSHOT.jar --swagger.ui.enabled=false
```

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

## Schemathesisによる自動テスト

OpenAPI 定義 (`target/api-docs.yml`) を用いたプロパティベーステストを Schemathesis で実行できます。以下の手順で実行してください。

1. OpenAPI の最新化とビルド
   ```bash
   mvn clean verify
   ```
2. Spring Boot のバックグラウンド起動
   ```bash
   mvn spring-boot:run > spring.log 2>&1 & SERVER_PID=$!
   ```
3. Schemathesis の実行（仮想環境を有効化）
   ```bash
   source ~/.venvs/schemathesis/bin/activate
   python schemathesis_runner.py
   ```
   - デフォルトで `schemathesis_runner.py` は `target/api-docs.yml` を参照し、詳細ログと統計をコンソールへ出力します。
4. テスト終了後、Spring Boot を停止
   ```bash
   kill $SERVER_PID
   ```

必要に応じて `schemathesis.toml` や `schemathesis_hooks.py` を編集し、テスト範囲やログ出力を調整してください。

## Allureレポートのローカル確認方法

1. JUnitテスト実行およびAllureレポートの出力
   ```bash
   mvn clean verify
   ```

2. Pythonで簡易HTTPサーバを立てhttp://localhost:30080 で確認

   ```bash
  python3 -m http.server 38080 --directory ./target/site/allure-maven-plugin/
   ```