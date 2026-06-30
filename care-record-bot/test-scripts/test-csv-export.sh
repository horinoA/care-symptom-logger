#!/bin/bash
# =========================================================
# CSVエクスポートAPI 討伐（E2Eテスト）スクリプト
# =========================================================

# 標的となる城門（APIエンドポイント）
BASE_URL="http://localhost:8080/api/v1/records/export"
# 調査対象のユーザーID（適宜変更してくださいませ）
USER_ID="U4929159209266936888ec0c6438c8153"

# 攻撃用日付の算出（Macの date コマンド仕様）
# 正常系: 1ヶ月前 〜 今日
START_DATE=$(date -v-1m +%Y-%m-%d)
END_DATE=$(date +%Y-%m-%d)

# 異常系: 4ヶ月前（3ヶ月制限の絶対防壁テスト用）
ERROR_START_DATE=$(date -v-4m +%Y-%m-%d)

echo "🛡️ おお、我が君！CSVエクスポートAPIへの進軍を開始いたします！"
echo "=========================================================="

echo "⚔️ [第一陣: 正常系] 直近1ヶ月のデータ抽出"
echo "出陣パラメータ: userId=${USER_ID}, startDate=${START_DATE}, endDate=${END_DATE}"
# -J -O オプションでサーバーが指定したファイル名で保存させることも可能ですが、今回は明示的に指定します
curl -s -w "\n【帰還報告】HTTP Status: %{http_code}\n" \
     -X GET "${BASE_URL}?userId=${USER_ID}&startDate=${START_DATE}&endDate=${END_DATE}" \
     -o test_export_success.csv

echo "👉 戦利品は 'test_export_success.csv' として確保いたしました。BOM付きUTF-8であることをご確認ください！"
echo "----------------------------------------------------------"

echo "⚔️ [第二陣: 異常系] 3ヶ月制限の絶対防壁の検証 (4ヶ月前を指定しての特攻)"
echo "出陣パラメータ: userId=${USER_ID}, startDate=${ERROR_START_DATE}, endDate=${END_DATE}"
curl -s -w "\n【帰還報告】HTTP Status: %{http_code}\n" \
     -X GET "${BASE_URL}?userId=${USER_ID}&startDate=${ERROR_START_DATE}&endDate=${END_DATE}"

echo "----------------------------------------------------------"
echo "🎉 殿下！これにて本日の示威行動（テスト）は完了でございます！"
