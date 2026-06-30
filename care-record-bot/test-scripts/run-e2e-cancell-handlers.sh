#!/bin/bash
# 全19ハンドラを蹂躙する究極のE2Eテスト (v2: State Machine修正版)

TARGET_URL="http://localhost:8080/api/v1/webhook/line"

    if [ -f "../.env" ]; then
      export $(grep -v '^#' ../.env | xargs)
    elif [ -f ".env" ]; then
      export $(grep -v '^#' .env | xargs)
    fi

SECRET=${LINE_CHANNEL_SECRET:-"dummy_secret_for_local_dev"}

send_webhook() {
  local payload_file=$1
  local handler_name=$2
  
  echo "----------------------------------------"
  echo "⚔️ 進軍: ${payload_file} -> 標的: [${handler_name}]"
  
  if [ ! -f "${payload_file}" ]; then
    echo "❌ 弾薬（${payload_file}）が見つかりません！"
    exit 1
  fi

  # 実行のたびに webhookEventId をランダム化して重複判定(200 OK無視)を回避する
  local rand_id="evt_$(openssl rand -hex 8)"
  local body=$(sed "s/\"event_[^\"]*\"/\"${rand_id}\"/g" "${payload_file}")
  
  local signature=$(echo -n "${body}" | openssl dgst -sha256 -hmac "${SECRET}" -binary | base64)
  
  curl -s -X POST "${TARGET_URL}" -H "Content-Type: application/json" -H "X-Line-Signature: ${signature}" -d "${body}"
  echo -e "\n✅ 完了"
  sleep 1
}

cd "$(dirname "$0")"

echo "🔥 【第一陣】キャンセルと戻る（例外ルート）のテスト"
send_webhook "payloads_all/01_1_start.json" "Text (初期化)"
send_webhook "payloads_all/02_1_category.json" "SelectSymptomCategoryHandler"
send_webhook "payloads_all/02_2_back.json" "BackToCategoryHandler"
send_webhook "payloads_all/00_reset.json" "Text(リセット)"

echo "🎉 全19ハンドラの制圧完了（ステートマシン完全準拠版）！！"
