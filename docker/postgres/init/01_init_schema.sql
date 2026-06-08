-- ===================================================================
-- スキーマの作成（綺麗に分離された3つのドメイン）
-- ===================================================================
CREATE SCHEMA IF NOT EXISTS core;    -- マスタ領域
CREATE SCHEMA IF NOT EXISTS event;   -- トランザクション（ファクト）領域
CREATE SCHEMA IF NOT EXISTS session; -- アプリ状態・システム制御領域

-- ===================================================================
-- 1. マスタ面 (coreスキーマ)
-- ===================================================================

-- 症状分類名マスタ
CREATE TABLE core.symptom_category_name (
    id SERIAL PRIMARY KEY,
    symptom_category_name VARCHAR(255) NOT NULL
);

-- 症状名マスタ
CREATE TABLE core.symptom_name (
    id SERIAL PRIMARY KEY,
    symptom_category_id INTEGER NOT NULL REFERENCES core.symptom_category_name(id),
    symptom_name VARCHAR(255) NOT NULL
);

-- 登場人物（Who/To Who）マスタ
CREATE TABLE core.people_care (
    id INTEGER PRIMARY KEY, -- 100〜900番台の世代コード等を想定
    relationship VARCHAR(255) NOT NULL,
    sort_order INTEGER NOT NULL,
    is_visible BOOLEAN NOT NULL DEFAULT TRUE
);

-- ===================================================================
-- 2. アプリ管理・ステート面 (sessionスキーマ)
-- ===================================================================

-- ユーザーセッション管理
CREATE TABLE session.user_sessions (
    user_id VARCHAR(255) PRIMARY KEY,
    session_id UUID NOT NULL,
    current_phase VARCHAR(50) NOT NULL,
    temp_data JSONB NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- LINE Webhookイベントログ（重複検知・順序制御の盾）
CREATE TABLE session.line_webhook_events (
    event_id VARCHAR(255) PRIMARY KEY,
    user_id VARCHAR(255) NOT NULL,
    session_id UUID, -- 未ログイン・初期状態はNULL
    event_type VARCHAR(50) NOT NULL,
    line_timestamp TIMESTAMPTZ NOT NULL,
    status VARCHAR(20) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Webhookログ検索・順序検証用インデックス
CREATE INDEX idx_line_webhooks_user_id_time ON session.line_webhook_events(user_id, line_timestamp DESC);

-- ===================================================================
-- 3. トランザクション・ファクト面 (eventスキーマ)
-- ===================================================================

-- 介護記録（親）
CREATE TABLE event.care_records (
    id BIGINT PRIMARY KEY, -- Java側でSnowflake ID等を採番して挿入
    user_id VARCHAR(255) NOT NULL,
    target_id INTEGER NOT NULL REFERENCES core.people_care(id),
    to_who_id INTEGER REFERENCES core.people_care(id), -- ★困った時以外はNULL許容で計算量激減
    onset_at TIMESTAMPTZ NOT NULL,
    timezone VARCHAR(50) NOT NULL,
    duration VARCHAR(50) NOT NULL,
    remitted_at TIMESTAMPTZ NOT NULL,
    memo TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- to_who_id の部分インデックス（NULL以外の行だけ高速検索・集計する用）
CREATE INDEX idx_care_records_to_who ON event.care_records(to_who_id) WHERE to_who_id IS NOT NULL;

-- 介護記録明細（子：症例ループ用）
CREATE TABLE event.care_record_details (
    id BIGINT PRIMARY KEY, -- 明細用ID (Snowflake or BIGSERIAL想定)
    care_records_id BIGINT NOT NULL REFERENCES event.care_records(id) ON DELETE CASCADE,
    symptom_id INTEGER NOT NULL REFERENCES core.symptom_name(id)
);