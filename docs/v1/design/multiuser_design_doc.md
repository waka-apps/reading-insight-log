# マルチユーザー対応（Cognito + 将来DynamoDB）設計プラン

## 概要

現状の単一ユーザー前提APIを、Cognito JWTでユーザー識別し、ユーザー単位で完全にデータ隔離する。
永続化は将来DynamoDBへ移行する前提で、ドメイン/リポジトリ/コントローラの境界にユーザーIDを必須化し、アクセスパターンに合わせたDynamoDB単一テーブル設計を定義する。

## 変更方針（要点）

- すべてのドメインエンティティにownerUserIdを持たせる
- 全リポジトリ/クエリにuserId必須引数を追加
- Cognito JWTを検証しsubをuserIdとして扱う
- すべてのAPIでユーザーIDはサーバ側で取得し、リクエストからは受け取らない
- DynamoDB単一テーブルでユーザー単位PKを基本に、レビュー期限用GSIを設計

———

## 公開API/インターフェースの変更

### 1) 認証・ユーザーコンテキスト

- 認証: Cognito JWTをMicronaut側で検証
    - iss/aud/expを検証、JWKSをキャッシュ
- UserIdProviderを以下に変更:
    - JWTクレームのsubを返す
    - リクエストヘッダやボディからはユーザーIDを受け取らない

### 2) APIのユーザー境界

- すべてのAPIがユーザーID必須の文脈で動作
- エンドポイントのパスは変更しない（/admin/...維持）
    - 破壊的変更を避けるため
    - ユーザーは認証済みであれば自分のデータのみ操作可能

### 3) ドメインモデル拡張

- Book, InsightにownerUserId: Stringを追加
- Book.createInitial(...) / Insight.createInitial(...)にownerUserId必須化

### 4) リポジトリの境界

- すべてのメソッドにuserIdを追加
    - BooksRepository.create(userId, ...)
    - BooksRepository.list(userId)
    - BooksRepository.findById(userId, bookId)
    - InsightsRepository.add(userId, bookId, ...)
    - InsightsRepository.listByBook(userId, bookId)
    - InsightsRepository.listReviewDue(userId, now)
    - InsightsRepository.updateReviewResult(userId, insightId, ...)
- 他ユーザーのデータを返す経路を完全遮断

———

## DynamoDB設計（将来移行）

### 単一テーブル設計

- テーブル: ReadingInsights
- PK: USER#<userId>
- SK:
    - Book: BOOK#<bookId>
    - Insight: BOOK#<bookId>#INSIGHT#<insightId>
    - Review Index用: REVIEW#<nextReviewAt>#INSIGHT#<insightId>

### GSI（review/today対策）

- GSI1PK: USER#<userId>
- GSI1SK: REVIEW#<nextReviewAt>#INSIGHT#<insightId>
- 取得:
    - GSI1PK = USER#<userId>
    - GSI1SK <= REVIEW#<now>で範囲クエリ

### 主なアクセスパターン

1. ユーザーのBooks一覧
    - PK=USER#id, SK begins_with BOOK#
2. Book配下のInsights一覧
    - PK=USER#id, SK begins_with BOOK#<bookId>#INSIGHT#
3. Review期限一覧
    - GSI1PK=USER#id, GSI1SK <= REVIEW#<now>

———

## 実装ステップ（計画）

1. 認証コンテキスト導入
    - Micronaut Security JWT設定
    - Cognito JWKS/Issuer/Audience設定
    - UserIdProviderをJWTからsub取得に変更
2. ドメイン/リポジトリ変更
    - Book, InsightにownerUserId追加
    - RepositoryにuserId必須引数化
    - In-memory storeはuserIdで分離
        - 例: ConcurrentHashMap<String, ConcurrentHashMap<String, Book>>
3. コントローラ変更
    - すべてのエンドポイントでuserId取得
    - Repository呼び出しに必ずuserIdを渡す
    - Book/Insight存在確認時もユーザー境界で検索
4. DynamoDB移行に備えたインターフェース整備
    - Repositoryをinterface化し実装を差し替え可能に
    - InMemoryBooksRepository / DynamoDbBooksRepositoryの形に整備

———

## テストケース

1. 認証必須
    - 認証なし → 401
    - 無効JWT → 401
2. ユーザー隔離
    - userAのbookをuserBで取得 → 404
    - userAのinsightをuserBでレビュー → 404 or 403
3. 一覧
    - userAのbooks一覧はuserAのみ
    - userBデータが混ざらない
4. review/today
    - userAのみ対象
    - 時間指定で正しい順序・件数

———

## 前提/仮定

- Cognito JWT Authorizerを利用。アプリ側でもJWT検証を行う（二重防御）
    - subクレームをユーザーIDに採用
- APIパスは当面/admin/...のまま維持
- 権限はユーザー単位のみでロールなし
- 強整合＋ハード削除を前提
- DynamoDB移行は後続タスクとして実装分離のみ先行