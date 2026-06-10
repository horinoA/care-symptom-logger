package tech.doshikawa.carerecord.application.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * LINEプラットフォームから受信したWebhookイベントを適切に処理・振り分ける
 * アプリケーションサービスのモック実装。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class LineWebhookDispatcherService {

    // 💡 今後、各種Repository（セッション管理やファクトの保存用）などを
    // ここで宣言し、@RequiredArgsConstructor によってDI（依存性の注入）を行います。
    // 例: private final UserSessionRepository userSessionRepository;

    /**
     * テキストメッセージイベントの処理（モック）
     * 
     * @param replyToken 返信用トークン
     * @param userId     ユーザーID
     * @param text       受信したテキスト内容
     */
    public void handleTextMessage(String replyToken, String userId, String text) {
        log.info("LINEからテキストメッセージを受信しました。ユーザーID: {}, 内容: {}", userId, text);
        
        // TODO: セッション状態の確認と、入力フェーズに応じた処理の分岐を実装する
        // TODO: LINE API経由での返信（Reply）処理を実装する

        log.info("テキストメッセージイベントの処理が完了しました。");
    }

    /**
     * Postbackイベントの処理（モック）
     * 💡 主にカルーセルやボタンの押下時に発火します。
     * 
     * @param replyToken 返信用トークン
     * @param userId     ユーザーID
     * @param data       Postbackされたデータ（session_id 等を含む想定）
     */
    public void handlePostback(String replyToken, String userId, String data) {
        log.info("LINEからPostbackイベントを受信しました。ユーザーID: {}, データ: {}", userId, data);
        
        // TODO: 送られてきた session_id (UUID) による冪等性・順序チェックを実装する
        // TODO: ステートマシンを次のフェーズ（症状選択 → 対象者選択など）へ進める処理を実装する
        
        log.info("Postbackイベントの処理が完了しました。");
    }
}
