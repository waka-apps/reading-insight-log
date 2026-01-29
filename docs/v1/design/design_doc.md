# 読書インサイト記録アプリ

Design Document（v1.0）

---

## 1. このドキュメントの位置づけ

- 本アプリケーションの設計判断を固定するためのドキュメント
- 実装・UI・インフラの判断は原則この内容に従う
- 軽微な仕様変更はマイナーバージョンを上げて追記する（v1.x）
- 大きな機能追加はディレクトリを新規作成し、別ドキュメントとして記載する（docs/v2）

---

## 2. 目的（Why）

### 主目的

- 読書内容を「自分の言葉」に変換して保存する
- 一定期間後に再提示し、復習・定着を促す
- 入力負荷を最小化し、継続できることを最優先する

---

## 3. 非目的（v1ではやらないこと）

- SEO対策
- 公開ブログ機能
- 高度な検索（全文検索・複雑な条件）
- マルチテナント
- 強いトランザクション保証
- プッシュ通知 / Web Push

---

## 4. 基本方針

- 管理画面は Nuxt SPA
- 本単位で知識を整理する
- アウトプットの最小単位は Insight
- 必須入力は最小限にする

---

## 5. 画面構成（Nuxt SPA）

### 5.1 本一覧 `/books`

- 起点画面
- 並び順：最近更新順
- 表示
  - タイトル
  - 著者（任意）
  - Insight数
  - 最終更新日時
- 操作
  - 新規本追加
  - 各行に「＋Insight」

---

### 5.2 本詳細 `/books/:bookId`

- 本情報（タイトル・著者）
- この本に Insight を追加
- Insight一覧（時系列）

---

### 5.3 Insight入力 `/books/:bookId/new-insight`

### 必須

- Quote（引用）
- Interpretation（自分の言葉での解釈）

### 任意

- Tags（チップ入力）

---

### 5.4 Review（復習） `/review`

- 今日復習すべき Insight を表示
  条件：nextReviewAt <= 今日
- 表示内容
  - Quote
  - Interpretation
- 評価（2択）
  - 思い出せた
  - 怪しい

※ v1では通知は行わず、アプリ内表示のみ

---

## 6. データモデル（Kotlin）

### 6.1 ID型

- BookId：文字列をラップした value class
- InsightId：文字列をラップした value class

（Kotlinでは @JvmInline value class を使用する想定）

---

### 6.2 Book

- id: BookId
- title: String（必須）
- author: String?（任意）
- updatedAt: Instant

---

### 6.3 Insight

- id: InsightId
- bookId: BookId
- quote: String
- interpretation: String
- tags: List<String>
- createdAt: Instant
- nextReviewAt: Instant
- reviewIntervalDays: Int

---

## 7. 復習ロジック（確定）

- 初回登録
  - reviewIntervalDays = 1
  - nextReviewAt = 作成日時 + 1日
- 復習評価
  - 思い出せた
    - interval を 2倍にする
  - 怪しい
    - interval を 1日に戻す
- 次回復習日
  - now + interval 日

---

## 8. API設計（HTTP API）

### Books

- GET /admin/books
- POST /admin/books
  - title（必須）
  - author（任意）

### Insights

- POST /admin/books/{bookId}/insights
- GET /admin/books/{bookId}/insights
- GET /admin/review/today
- POST /admin/insights/{insightId}/review
  - result = REMEMBERED | UNCERTAIN

---

## 9. DynamoDB設計（1テーブル）

### 共通

- Partition Key：USER#{userId}

### Books

- Sort Key：BOOK#{bookId}
- 属性
  - title
  - author
  - updatedAt

### Insights

- Sort Key：INSIGHT#{createdAt}#{insightId}
- 属性
  - bookId
  - quote
  - interpretation
  - tags
  - nextReviewAt
  - reviewIntervalDays

### GSI（本詳細用）

- GSI1PK：USER#{userId}#BOOK#{bookId}
- GSI1SK：INSIGHT#{createdAt}#{insightId}

---

## 10. インフラ構成（v1）

- フロントエンド
  - S3 + CloudFront
  - SPA fallback（403/404 → index.html）
- バックエンド
  - API Gateway（HTTP API）
  - AWS Lambda（Kotlin）
- データストア
  - DynamoDB（オンデマンド）
- 認証
  - v1：簡易方式
  - 将来：Cognito User Pool

---

## 11. 実装順（固定）

1. 本一覧 / 本追加
2. Insight追加（必須2項目＋タグ）
3. Review画面（今日の復習）
4. 復習評価API

---

## 12. v1で満たせること

- 読書内容を自分の言葉で残せる
- 本単位で知識を整理・参照できる
- 一定期間後に自動で復習できる
- 入力負荷が低く、継続しやすい

---

## 13. 変更ルール

- UI変更：v1.x
- データモデル変更：マイグレーション方針を必ず記載
- 思想変更（目的・必須項目）：v2.0 として分離
