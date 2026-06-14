#!/bin/bash

# LINE Webhookへテキストメッセージを送信するテスト用スクリプト
# 使い方: ./test-webhook.sh "送りたいテキスト"

TEXT=$1
if [ -z "$TEXT" ]; then
    echo "使用方法: ./test-webhook.sh <text>"
    exit 1
fi

# .envファイルからLINE_CHANNEL_SECRETを読み込む
if [ -f ".env" ]; then
    export $(grep -v '^#' .env | xargs)
fi

SECRET=$LINE_CHANNEL_SECRET

if [ -z "$SECRET" ]; then
    echo "エラー: .env ファイルに LINE_CHANNEL_SECRET が設定されていません。"
    exit 1
fi

BODY=$(cat <<EOF
{
  "destination": "xxxxxxxxxx",
  "events": [
    {
      "replyToken": "00000000000000000000000000000000",
      "type": "message",
      "timestamp": 1462629479859,
      "source": {
        "type": "user",
        "userId": "test_user_123"
      },
      "message": {
        "id": "325708",
        "type": "text",
        "text": "${TEXT}"
      }
    }
  ]
}
EOF
)

# LINEの仕様に基づく署名（X-Line-Signature）を生成
SIGNATURE=$(echo -n "$BODY" | openssl dgst -sha256 -hmac "$SECRET" -binary | base64)

echo "--- 送信データ ---"
echo "テキスト: ${TEXT}"
echo "署名: ${SIGNATURE}"
echo "------------------"

curl -X POST http://localhost:8080/api/v1/webhook/line \
-H "Content-Type: application/json" \
-H "X-Line-Signature: $SIGNATURE" \
-d "$BODY"

echo ""
