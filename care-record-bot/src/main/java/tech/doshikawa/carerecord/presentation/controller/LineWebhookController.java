package tech.doshikawa.carerecord.presentation.controller;

import com.linecorp.bot.webhook.model.MessageEvent;
import com.linecorp.bot.webhook.model.PostbackEvent;
import com.linecorp.bot.webhook.model.TextMessageContent;
import com.linecorp.bot.spring.boot.handler.annotation.EventMapping;
import com.linecorp.bot.spring.boot.handler.annotation.LineMessageHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import tech.doshikawa.carerecord.application.service.LineWebhookDispatcherService;

/**
 * LINEプラットフォームからのWebhookイベントを受信するController層。
 * ビジネスロジックは一切持たず、Application層（Service）へ処理を委譲します。
 */
@Slf4j
@LineMessageHandler
@RequiredArgsConstructor
public class LineWebhookController {

    private final LineWebhookDispatcherService dispatcherService;

    /**
     * メッセージを受け取るイベントハンドラー
     */
    @EventMapping
    public void handleMessageEvent(MessageEvent event) {
        // v8系ではジェネリクスではなく、ここで型判定を行います
        if (event.message() instanceof TextMessageContent textMessage) {
            log.info("Controller: テキストメッセージイベントを受信しました。");
            
            // v8系からは getXXX() ではなく xxx() メソッドで値を取得します
            String replyToken = event.replyToken();
            String userId = event.source() != null ? event.source().userId() : null;
            String text = textMessage.text();

            dispatcherService.handleTextMessage(replyToken, userId, text);
        }
    }

    /**
     * Postback（カルーセルのボタン押下等）を受け取るイベントハンドラー
     */
    @EventMapping
    public void handlePostbackEvent(PostbackEvent event) {
        log.info("Controller: Postbackイベントを受信しました。");
        dispatcherService.handlePostback(event);
    }
}
