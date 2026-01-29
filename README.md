## Overview

**reading-insight-log** は、

読書で得た内容を「自分の言葉」に変換して記録し、

一定期間後に再提示することで **理解と定着を高める** ための個人向けアプリです。

単なる読書ログではなく、

- 引用（Quote）
- 自分の解釈（Interpretation）

を最小単位として蓄積し、

**復習を前提にした学習サイクル**を作ることを目的としています。

---

## Motivation

スキルアップのために技術書・ビジネス書を読む量を増やしたい一方で、

- 読んだ内容が定着しない
- アウトプットが感想止まりになる
- 後から参照しづらい

といった課題を感じていました。

このプロジェクトは、

- 入力負荷を最小化する
- 「自分の言葉で書く」ことを必須にする
- 一定期間後に自動で復習できる

という設計により、

**継続可能な学習・復習の仕組み**を作ることを狙っています。

---

## Key Concepts

- Book
  読書対象の本。すべてのインサイトは本に紐づく。
- Insight
  学習の最小単位。
  - Quote（引用）
  - Interpretation（自分の言葉での解釈）
- Review
  登録した Insight を一定期間後に再提示し、
  「思い出せた / 怪しい」の2択で評価する。

---

## Features (MVP v1)

- 本の作成・一覧表示
- 本を選択して Insight を追加
- Quote + Interpretation を必須とした入力
- タグによる軽い分類
- 今日復習すべき Insight の表示
- シンプルなスペースドリピティション（復習間隔制御）

---

## Tech Stack

### Frontend

- Nuxt 4 (SPA)
- Hosted on Amazon S3 + CloudFront

### Backend

- Kotlin
- AWS Lambda
- API Gateway (HTTP API)

### Data Store

- Amazon DynamoDB (on-demand)

### Others

- Designed for low-cost, serverless operation
- Public repository for learning & portfolio purposes

---

## Project Structure

- apps/admin
  Nuxt SPA (管理画面)
- services/api
  Kotlin Lambda API
- docs/design
  設計ドキュメント（Design Doc）
- docs/plan
  実装TODOチェックリスト

---

## Documentation

- Design Document
  docs/v1/design/design_doc.md
- Implementation TODO
  docs/v1/plan/todo.md

これらのドキュメントは、

設計判断・スコープ・実装順を明確にするためにリポジトリに含めています。

---

## License

MIT License

---

## Note

This is a personal learning project.

APIs, data models, and implementation details may change as the project evolves.
