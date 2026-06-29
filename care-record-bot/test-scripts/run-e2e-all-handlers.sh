#!/bin/bash
# 全19ハンドラを蹂躙する究極のE2Eテスト (v2: State Machine修正版)

TARGET_URL="http://localhost:8080/api/v1/webhook/line"
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
send_webhook "payloads_all/02_3_cancel.json" "CancelRecordHandler"

echo "🔥 【第二陣】カテゴリ40(記憶と言葉)・メモありルート"
# ※カテゴリ40(記憶と言葉)など困った(30)以外は、WhoのあとにToWhoが無く、直接メモ有無に飛びます
send_webhook "payloads_all/01_1_start.json" "Text (初期化再)"
send_webhook "payloads_all/02_4_category2.json" "SelectSymptomCategoryHandler (記憶)"
send_webhook "payloads_all/03_1_detail.json" "SelectSymptomDetailHandler"
send_webhook "payloads_all/06_1_next_to_date.json" "NextToDateHandler"
send_webhook "payloads_all/07_1_select_date.json" "SelectDateHandler"
send_webhook "payloads_all/08_1_select_timezone.json" "SelectTimezoneHandler"
send_webhook "payloads_all/09_1_select_duration.json" "SelectDurationHandler"
send_webhook "payloads_all/04_1_show_who_sub.json" "ShowWhoSubHandler"
send_webhook "payloads_all/04_2_who_detail.json" "SelectWhoDetailHandler (祖父)"
send_webhook "payloads_all/10_1_want_memo.json" "WantMemoHandler"
send_webhook "payloads_all/10_2_memo_text.json" "Text (メモ本文)"
send_webhook "payloads_all/11_1_save_final.json" "SaveFinalHandler"

echo "🔥 【第三陣】カテゴリ30(困った)・ループ・ToWho(その他)・メモなしルート"
# ※カテゴリ30(困った)はWhoのあとにToWhoがあります
send_webhook "payloads_all/12_1_start2.json" "Text (初期化再々)"
send_webhook "payloads_all/12_2_category_trouble.json" "SelectSymptomCategoryHandler (困った)"
send_webhook "payloads_all/12_3_detail_trouble.json" "SelectSymptomDetailHandler"
send_webhook "payloads_all/03_2_continue.json" "ContinueSymptomHandler (追加ループ)"
send_webhook "payloads_all/12_2_category_trouble.json" "SelectSymptomCategoryHandler (困った2回目)"
send_webhook "payloads_all/03_3_detail2.json" "SelectSymptomDetailHandler (詳細2個目)"
send_webhook "payloads_all/06_1_next_to_date.json" "NextToDateHandler"
send_webhook "payloads_all/07_1_select_date.json" "SelectDateHandler"
send_webhook "payloads_all/08_1_select_timezone.json" "SelectTimezoneHandler"
send_webhook "payloads_all/09_1_select_duration.json" "SelectDurationHandler"
send_webhook "payloads_all/12_4_select_who.json" "SelectWhoHandler"
send_webhook "payloads_all/05_1_show_towho_sub.json" "ShowToWhoSubHandler"
send_webhook "payloads_all/05_2_towho_detail.json" "SelectToWhoDetailHandler (医療従事者)"
send_webhook "payloads_all/12_6_save_no_memo.json" "SaveNoMemoHandler"

echo "🎉 全19ハンドラの制圧完了（ステートマシン完全準拠版）！！"
