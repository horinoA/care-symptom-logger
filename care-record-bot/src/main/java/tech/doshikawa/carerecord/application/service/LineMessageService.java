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

import java.io.InputStream;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class LineMessageService {

    private final MessagingApiClient messagingApiClient;
    private final ObjectMapper objectMapper;

    public void replyText(String replyToken, String text) {
        reply(replyToken, List.of(new TextMessage(text)));
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

    private void reply(String replyToken, List<Message> messages) {
        try {
            messagingApiClient.replyMessage(new ReplyMessageRequest(replyToken, messages, false)).get();
        } catch (Exception e) {
            log.warn("Replyの送信に失敗しました: {}", e.getMessage());
        }
    }
}
