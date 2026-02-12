## Micronaut 4.10.7 Documentation

- [User Guide](https://docs.micronaut.io/4.10.7/guide/index.html)
- [API Reference](https://docs.micronaut.io/4.10.7/api/index.html)
- [Configuration Reference](https://docs.micronaut.io/4.10.7/guide/configurationreference.html)
- [Micronaut Guides](https://guides.micronaut.io/index.html)
---

- [Micronaut Gradle Plugin documentation](https://micronaut-projects.github.io/micronaut-gradle-plugin/latest/)
- [GraalVM Gradle Plugin documentation](https://graalvm.github.io/native-build-tools/latest/gradle-plugin.html)
- [Shadow Gradle Plugin](https://gradleup.com/shadow/)
## Feature ksp documentation

- [Micronaut Kotlin Symbol Processing (KSP) documentation](https://docs.micronaut.io/latest/guide/#kotlin)

- [https://kotlinlang.org/docs/ksp-overview.html](https://kotlinlang.org/docs/ksp-overview.html)


## Feature validation documentation

- [Micronaut Validation documentation](https://micronaut-projects.github.io/micronaut-validation/latest/guide/)


## Feature serialization-jackson documentation

- [Micronaut Serialization Jackson Core documentation](https://micronaut-projects.github.io/micronaut-serialization/latest/guide/)


## Feature micronaut-aot documentation

- [Micronaut AOT documentation](https://micronaut-projects.github.io/micronaut-aot/latest/guide/)

---

## ローカル開発（DynamoDB Local + .env）

### 前提
- Dockerが使える
- `direnv` を利用して `.env` を読み込む（コード変更なし）

### 1. DynamoDB Local を起動
プロジェクトルートで実行する。

```bash
docker compose up -d dynamodb-local
```

### 2. テーブル作成（AWS CLIなし）
Dockerの `amazon/aws-cli` を使う。

```bash
docker run --rm \
  -e AWS_ACCESS_KEY_ID=local \
  -e AWS_SECRET_ACCESS_KEY=local \
  -e AWS_REGION=us-east-1 \
  amazon/aws-cli \
  dynamodb create-table \
  --table-name reading-insight-log-dev \
  --attribute-definitions \
    AttributeName=PK,AttributeType=S \
    AttributeName=SK,AttributeType=S \
    AttributeName=GSI1PK,AttributeType=S \
    AttributeName=GSI1SK,AttributeType=S \
  --key-schema \
    AttributeName=PK,KeyType=HASH \
    AttributeName=SK,KeyType=RANGE \
  --billing-mode PAY_PER_REQUEST \
  --global-secondary-indexes \
    "IndexName=GSI1,KeySchema=[{AttributeName=GSI1PK,KeyType=HASH},{AttributeName=GSI1SK,KeyType=RANGE}],Projection={ProjectionType=ALL}" \
  --endpoint-url http://host.docker.internal:8000
```

### 3. `.env` を用意
`services/api/.env` を作成。

```env
DYNAMODB_ENDPOINT=http://localhost:8000
DYNAMODB_TABLE=reading-insight-log-dev
AWS_REGION=ap-northeast-1
APP_USER_ID=local
DAILY_INSIGHT_LIMIT=25
DAILY_BOOK_LIMIT=5
```

### 4. direnv 設定
`services/api/.envrc` を作成。

```bash
dotenv .env
```

読み込み（初回 or 変更時）:

```bash
direnv allow .
direnv reload
```

確認:

```bash
echo $DYNAMODB_ENDPOINT
```

### 5. API起動
`services/api` で実行。

```bash
./gradlew run
```

### 6. トラブルシュート
- `Unable to load credentials` が出る  
  → `DYNAMODB_ENDPOINT` が読み込まれていない。`direnv allow` / `direnv reload` を確認。
- `Cannot do operations on a non-existent table` が出る  
  → DynamoDB Local にテーブルが無い。手順「2. テーブル作成」を再実行。

---

## 本番（AWS Lambda）向けの方針
- 本番値は環境変数で設定する（`application.yml` に直書きしない）
- `DYNAMODB_ENDPOINT` は未設定のまま（AWS向き）
- テーブル名は `reading-insight-log-prod`
