# 1. 実行環境には軽量なJREを指定（Java 21）
FROM eclipse-temurin:21-jre-alpine

# 2. 作業ディレクトリの指定
WORKDIR /app

# 3. 事前にビルドしたjarファイルをコピー
# ファイル名を直接指定する（一番確実、実際のファイル名は要確認）
COPY build/libs/care-record-bot-0.0.1-SNAPSHOT.jar app.jar

# 4. コンテナ起動時のコマンド
ENTRYPOINT ["java", "-jar", "app.jar"]

# Fly.io にポートを知らせる
EXPOSE 8080