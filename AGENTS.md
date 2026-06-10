# AI Agents Instructions (AGENTS.md)

## 1. プロジェクト概要 (Project Overview)
* **システム名:** CareRecordDWH (介護記録データウェアハウス)
* **目的:** LINEプラットフォームをフロントエンドとし、ユーザー（家族）からの介護記録ファクトを受信・蓄積する。
* **基本思想:** 本システムは「DWH（データウェアハウス）」である。蓄積されるデータは客観的な「歴史的事実（ファクト）」であり、将来的な医療機関や行政へのレポート（証拠）としての信頼性（SSOT）を担保することを最優先とする。

## 2. 技術スタック (Tech Stack)
* **言語:** Java 21
* **フレームワーク:** Spring Boot 4.0.x
* **データベース:** PostgreSQL (Dockerコンテナ)
* **O/Rマッパー:** Spring Data JDBC (**※JPA / Hibernate は絶対に使用しないこと**)
* **外部API:** LINE Bot SDK (`line-bot-spring-boot-handler`)

## 3. 絶対的なアーキテクチャ・設計ルール (Strict Rules)

### 3.1. データの不変性 (Immutable Data)
* **原則追記のみ (Append-Only):** システム上からの `UPDATE` および `DELETE` (物理削除) のAPIエンドポイントは作成しない。
* **論理削除:** DBスキーマには将来のための論理削除フラグ (`is_deleted BOOLEAN DEFAULT FALSE`) を用意しているが、現在のMVPフェーズではこれを操作する機能は実装しない。間違えた記録は「訂正の追記」によって相殺する。

### 3.2. ディレクトリ構成 (DDD / Onion Architecture)
パッケージは `tech.doshikawa.carerecord` 配下に以下の4層構造で配置し、依存関係は必ず「外側から内側（Domain）」に向かうこと。
1.  `presentation`: LINEからのWebhookを受け取るController層。ビジネスロジックは一切持たず、Application層へ処理を委譲する。
2.  `application`: ユースケースを実現するService層（ステートマシンの進行など）。
3.  `domain`: 業務の核となるEntity、Enum、Repositoryインターフェースを配置する。
4.  `infrastructure`: Spring Data JDBCによるRepositoryの実装や、LINE APIクライアントなど、外部技術の泥臭い処理を担当する。

### 3.3. 実装上の注意点 (Implementation Guidelines)
* **シンプルさの追求:** 過剰なライブラリや不要な依存関係は追加しない（例: `.env` はSpring Boot標準の `spring.config.import` 機能で読み込むため、`dotenv-java` は不要）。
* **Lombokの活用:** `@RequiredArgsConstructor` (DI用), `@Slf4j`, `@Data` (または `@Getter`/`@Setter`) 等を積極的に活用し、ボイラープレートコードを削減すること。
* **生SQLの許容:** 複雑な集計が必要な場合は、Spring Data JDBCの `@Query` を用いて生SQLを記述することを厭わない。

## 4. エージェントへの指示 (Instructions for AI)
コードを生成する際は、上記のルールを厳守すること。特に「JPAのアノテーション (`@Entity`, `@OneToMany` 等) を使う」「勝手にCRUDのUpdate/Deleteメソッドを作る」といった振る舞いは、本プロジェクトの設計思想に対する重大な違反となるため絶対に行わないこと。

## 5. APIエンドポイント設計（HTTPメソッド・リソース対応表）

本システムは、LINEプラットフォームからのWebhook受信を中心とし、データの「追記（Create）」と「参照（Read）」に特化した設計とする。

**⚠️ 重要制約 (System Policy):**
* **DELETEメソッドの使用制限:** 本APIにおいて `DELETE` メソッドは `/api/v1/sessions/{userId}` (セッション初期化) へのアクセス以外、**一切禁止**とする。
* **物理削除の禁止:** 介護記録（`CareRecord`）に対する `DELETE` は提供しない。不整合データは「訂正の追記」によって対処する。

| リソース (URI) | HTTPメソッド | 概要（処理内容） | アクター / 用途 |
| :--- | :---: | :--- | :--- |
| `/api/v1/webhook/line` | `POST` | **LINEからのイベント受信** | LINEプラットフォーム |
| `/api/v1/records` | `GET` | **介護記録ファクトの一覧取得** | 管理画面 / レポート出力 |
| `/api/v1/records/{recordId}` | `GET` | **特定の介護記録の詳細取得** | 管理画面 / 詳細確認 |
| `/api/v1/records/export` | `GET` | **記録のデータエクスポート** | 外部提出用 |
| `/api/v1/symptoms` | `GET` | **症状マスタ一覧の取得** | UI描画用 |
| `/api/v1/sessions/{userId}` | `DELETE` | **セッションの強制リセット**<br>※本システムで唯一許可されるDELETEメソッド | デバッグ / 状態初期化 |

## 6. コーディング規約 (Java)
* **言語設定 (重要)**:
    * **Javadoc / コメント**: **全て日本語**で記述すること。
    * **ログメッセージ**: `log.info("処理を開始します")` のように**日本語**で記述すること。
    * **例外メッセージ**: エラー内容が直感的に分かる**日本語**にすること。
* **Lombok**: `@Data`, `@Builder`, `@AllArgsConstructor` を活用。
* **Environment**: 環境変数は `.env` で管理。`@Value("${...}")` で注入。
* **Logging**: バッチ処理の開始・終了・エラーは必ずログ出力（`sys.job_logs` への保存も考慮）。

## 参照ファイル
* **DB構成**: ./docker/postgres/init/01_init_schema.sql
* **要件定期書**: ./docs/要件定義書.md
を確認して考慮してください。


## 次回作業予定(2026/06/11)
 1. Domain層の実装（Entityの作成） ★おすすめ
      •  session.user_sessions  や  event.care_records 
      などのテーブルとJavaを繋ぐ Entity
      クラスや、症状カテゴリのEnumを作成します。
  2. Infrastructure層の実装（Repositoryの作成）
      • 「Spring Data
      JDBC」を使って実際にデータを保存・取得する基盤を作成します。
  3. Application層の実装（セッション管理とステートマシンの構築）
      •  LineWebhookDispatcherService 
      を拡張し、LINEの対話状態（フェーズ）を進めるロジックを作っていきます。