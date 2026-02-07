## フェーズ1：フロント（Nuxt）

目的：アプリの全体像を掴む

### 1.1 Nuxt初期セットアップ

- [ ] Nuxt 4 SPA プロジェクト作成（apps/admin）
- [ ] ルーティング確認（pages/ 配下）
- [ ] 最低限のレイアウト作成（ヘッダのみでOK）

### 1.2 本一覧 `/books`（ダミーデータ）

- [ ] ダミーBookデータ3件用意
- [ ] 本一覧を表示
- [ ] 本をクリックして `/books/:bookId` に遷移
- [ ] 「新規本」ボタン配置（動かなくてOK）

### 1.3 本詳細 `/books/:bookId`

- [ ] 選択した本の情報表示（ダミー）
- [ ] 「＋Insight」ボタン配置
- [ ] Insight一覧（ダミー）表示

### 1.4 Insight入力 `/books/:bookId/new-insight`

- [ ] Quote入力欄（必須）
- [ ] Interpretation入力欄（必須）
- [ ] Tags入力欄（任意）
- [ ] 保存ボタン（console.logでOK）

### 1.5 Review `/review`

- [ ] ダミーInsightを表示
- [ ] 「思い出せた / 怪しい」ボタン配置

---

## フェーズ2：Kotlin APIをローカルで作る（AWSなし）

目的：**Kotlinの学習**

### 2.1 APIプロジェクト初期化

- [ ] Kotlinプロジェクト作成（services/api）
- [ ] MicronautでHTTPサーバ起動
- [ ] ヘルスチェック `/health` 実装

### 2.2 ドメインモデル

- [ ] BookId / InsightId（value class）
- [ ] Book データクラス
- [ ] Insight データクラス
- [ ] ReviewResult（REMEMBERED / UNCERTAIN）

### 2.3 Repository（インメモリ）

- [ ] BooksRepository（in-memory）
  - [ ] create
  - [ ] list（最近更新順）
- [ ] InsightsRepository（in-memory）
  - [ ] add
  - [ ] listByBook
  - [ ] listReviewDueToday
  - [ ] updateReviewResult

### 2.4 API実装（ローカル）

- [ ] POST /admin/books
- [ ] GET /admin/books
- [ ] POST /admin/books/{bookId}/insights
- [ ] GET /admin/books/{bookId}/insights
- [ ] GET /admin/review/today
- [ ] POST /admin/insights/{insightId}/review

### 2.5 バリデーション

- [ ] title必須
- [ ] quote必須
- [ ] interpretation必須
- [ ] tags正規化（trim）

---

## フェーズ3：Nuxt ↔ Kotlin API を接続する

目的：**フロント × バックエンドの理解**

- [ ] NuxtからAPIをfetch
- [ ] 本一覧をAPIデータで表示
- [ ] Insight追加をAPI経由で保存
- [ ] Review画面をAPI経由で表示
- [ ] 評価ボタンでReview APIを叩く

---

## フェーズ4：DynamoDBに差し替え

目的：DB設計とクエリ理解

- [ ] DynamoDBテーブル作成（オンデマンド）
- [ ] PK/SK設計反映
- [ ] GSI1作成
- [ ] RepositoryをDynamoDB実装に差し替え
- [ ] 本一覧 / 本詳細 / Reviewが動くことを確認

---

## フェーズ5：AWSサーバレス化

目的：AWS理解

- [ ] Kotlin APIをLambda対応
- [ ] API Gateway（HTTP API）作成
- [ ] CORS設定
- [ ] Lambdaログ確認（CloudWatch）

---

## フェーズ6：フロントをAWSにデプロイ

目的：ホスティング理解

- [ ] Nuxt build
- [ ] S3バケット作成
- [ ] ビルド成果物アップロード
- [ ] CloudFront作成
- [ ] SPA fallback設定（403/404 → index.html）

---

## フェーズ7：仕上げ

- [ ] READMEのStatus更新
- [ ] スクリーンショット撮影
- [ ] v1完了チェック

---

## v1完了条件（DoD）

- [ ] 本を作成・一覧できる
- [ ] 本を選んでInsightを追加できる
- [ ] Quote + Interpretation が必須
- [ ] Review画面で復習できる
- [ ] 評価に応じて次回復習日が更新される
- [ ] AWS上で動作する

[TODO(old)](https://www.notion.so/TODO-old-2f7dcaf36250806481d7f917e611c341?pvs=21)
