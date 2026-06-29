#!/bin/bash
# 偉大なる女王の命により、"困った"シナリオのE2Eテストを実行する投石機

TARGET_URL="http://localhost:8080/api/v1/webhook/line"

# もし環境変数 LINE_CHANNEL_SECRET が設定されていなければ、デフォルトのダミー値を使用する
SECRET=${LINE_CHANNEL_SECRET:-"dummy_secret_for_local_dev"}

send_webhook() {
  local payload_file=$1
  local step_name=$2
  
  echo "----------------------------------------"
  echo "⚔️ 進軍: ${step_name}"
  
  if [ ! -f "${payload_file}" ]; then
    echo "❌ 弾薬（${payload_file}）が見つかりません！"
    exit 1
  fi

  local body=$(cat "${payload_file}")
  
  # 完璧な偽造印（X-Line-Signature）を生成
  local signature=$(echo -n "${body}" | openssl dgst -sha256 -hmac "${SECRET}" -binary | base64)
  
  # 城門への直接攻撃！
  curl -s -X POST "${TARGET_URL}" \
       -H "Content-Type: application/json" \
       -H "X-Line-Signature: ${signature}" \
       -d "${body}"
  
  echo -e "\n✅ 攻撃完了。1秒待機..."
  sleep 1
}

# 実行ディレクトリを test-scripts に固定するため、移動
cd "$(dirname "$0")"

echo "🔥 E2E猛訓練を開始いたします！シナリオ：【困った】→【暴言や暴力】→【母】→【自分へ】"

send_webhook "payloads/01_start.json" "フェーズ1: テキスト『開始』"
send_webhook "payloads/02_category.json" "フェーズ2: カテゴリ『困った(30)』"
send_webhook "payloads/03_detail.json" "フェーズ3: 詳細『暴言や暴力(3011)』"
send_webhook "payloads/04_who.json" "フェーズ4: 対象『母(202)』"
send_webhook "payloads/05_towho.json" "フェーズ5: 誰へ『自分(301)』"
send_webhook "payloads/06_date.json" "フェーズ6: 日付『2026-06-29』"
send_webhook "payloads/07_timezone.json" "フェーズ7: 時間帯『朝(morning)』"
send_webhook "payloads/08_timespend.json" "フェーズ8: 所要時間『1(15分未満等)』"
send_webhook "payloads/09_memo_yn.json" "フェーズ9: メモ『いいえ(save_no_memo)』"

echo "🎉 全9フェーズの攻撃完了！DB(care_records)に美しい履歴が刻まれたか確認せよ！"
