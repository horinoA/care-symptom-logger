package tech.doshikawa.carerecord.application.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.linecorp.bot.messaging.client.MessagingApiClient;
import com.linecorp.bot.messaging.model.FlexContainer;
import com.linecorp.bot.messaging.model.FlexMessage;
import com.linecorp.bot.messaging.model.Message;
import com.linecorp.bot.messaging.model.ReplyMessageRequest;
import com.linecorp.bot.messaging.model.TextMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.context.MessageSource;

import java.io.InputStream;
import java.util.List;
import java.util.Locale;

@Slf4j
@Service
@RequiredArgsConstructor
public class LineMessageService {

    private final MessagingApiClient messagingApiClient;
    private final MessageSource messageSource;

    // SpringのDIに依存せず、独自にObjectMapperを定義
    private final ObjectMapper objectMapper = new ObjectMapper()
            .configure(com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    public void replyText(String replyToken, String text) {
        reply(replyToken, List.of(new TextMessage(text)));
    }

    public void replyMessages(String replyToken, List<Message> messages) {
        reply(replyToken, messages);
    }

    public void replyTextByCode(String replyToken, String code, Object... args) {
        String text = messageSource.getMessage(code, args, code, Locale.JAPAN);
        replyText(replyToken, text);
    }

    public void replyFlexMessage(String replyToken, String altText, String jsonFileName) {
        try {
            ClassPathResource resource = new ClassPathResource("flex-messages/" + jsonFileName);
            try (InputStream is = resource.getInputStream()) {
                FlexContainer container = objectMapper.readValue(is, FlexContainer.class);
                FlexMessage flexMessage = new FlexMessage(altText, container);
                reply(replyToken, List.of(flexMessage));
            }
        } catch (Exception e) {
            log.error("Flex Messageの読み込み・送信に失敗しました: " + jsonFileName, e);
            replyText(replyToken, "システムエラーが発生しました。");
        }
    }

    public void replyTextAndFlexMessage(String replyToken, String text, String altText, String jsonFileName) {
        try {
            ClassPathResource resource = new ClassPathResource("flex-messages/" + jsonFileName);
            try (InputStream is = resource.getInputStream()) {
                FlexContainer container = objectMapper.readValue(is, FlexContainer.class);
                FlexMessage flexMessage = new FlexMessage(altText, container);
                TextMessage textMessage = new TextMessage(text);
                reply(replyToken, List.of(textMessage, flexMessage));
            }
        } catch (Exception e) {
            log.error("テキストとFlex Messageの読み込み・送信に失敗しました: " + jsonFileName, e);
            replyText(replyToken, "システムエラーが発生しました。");
        }
    }

    public void replyTextAndFlexMessageByCode(String replyToken, String textCode, Object[] args, String altText, String jsonFileName) {
        String text = messageSource.getMessage(textCode, args, textCode, Locale.JAPAN);
        replyTextAndFlexMessage(replyToken, text, altText, jsonFileName);
    }

    private void reply(String replyToken, List<Message> messages) {
        if ("dummy_token".equals(replyToken)) {
            log.info("テスト用トークンのため、LINE APIへの送信をスキップします。送信予定のメッセージ数: {}", messages.size());
            return;
        }
        try {
            messagingApiClient.replyMessage(new ReplyMessageRequest(replyToken, messages, false)).get();
        } catch (Exception e) {
            log.warn("Replyの送信に失敗しました: {}", e.getMessage());
        }
    }
}
