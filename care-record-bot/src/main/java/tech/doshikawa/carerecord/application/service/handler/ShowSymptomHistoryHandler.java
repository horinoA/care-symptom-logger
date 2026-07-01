package tech.doshikawa.carerecord.application.service.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.linecorp.bot.messaging.model.FlexContainer;
import com.linecorp.bot.messaging.model.FlexMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import tech.doshikawa.carerecord.application.service.LineMessageService;
import tech.doshikawa.carerecord.domain.dto.RecentSymptomDto;
import tech.doshikawa.carerecord.domain.entity.UserSession;
import tech.doshikawa.carerecord.domain.repository.CareRecordRepository;
import tech.doshikawa.carerecord.domain.type.InputPhase;

import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class ShowSymptomHistoryHandler implements PostbackActionHandler {

    private final CareRecordRepository careRecordRepository;
    private final LineMessageService lineMessageService;
    
    // SpringのDIに依存せず、独自にObjectMapperを定義
    private final ObjectMapper objectMapper = new ObjectMapper()
            .configure(com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    @Override
    public boolean supports(String action) {
        return "show_history".equals(action);
    }

    @Override
    public Set<InputPhase> getAllowedPhases() {
        // 全フェーズで許可
        return Arrays.stream(InputPhase.values()).collect(Collectors.toSet());
    }

    @Override
    public void handle(String replyToken, UserSession session, Map<String, String> params) {
        OffsetDateTime now = OffsetDateTime.now(ZoneId.of("Asia/Tokyo"));
        OffsetDateTime startDate = now.minusDays(30).withHour(0).withMinute(0).withSecond(0).withNano(0);
        OffsetDateTime endDate = now.withHour(23).withMinute(59).withSecond(59).withNano(999999999);

        List<RecentSymptomDto> records = careRecordRepository.findRecentSymptomsByDateRange(
                session.getUserId(), startDate, endDate);

        if (records.isEmpty()) {
            lineMessageService.replyText(replyToken, "直近1ヶ月の記録は見つかりませんでした。");
            return;
        }

        String startDateStr = startDate.format(DateTimeFormatter.ofPattern("M/d"));
        String endDateStr = endDate.format(DateTimeFormatter.ofPattern("M/d"));

        // JSONを文字列として構築する
        StringBuilder jsonBuilder = new StringBuilder();
        jsonBuilder.append(String.format("""
            {
              "type": "bubble",
              "size": "giga",
              "header": {
                "type": "box",
                "layout": "vertical",
                "backgroundColor": "#17c950",
                "contents": [
                  {
                    "type": "text",
                    "text": "過去1ヶ月の症状履歴",
                    "weight": "bold",
                    "color": "#ffffff",
                    "size": "sm"
                  },
                  {
                    "type": "text",
                    "text": "期間: %s - %s",
                    "weight": "bold",
                    "size": "xl",
                    "color": "#ffffff",
                    "margin": "md"
                  }
                ]
              },
              "body": {
                "type": "box",
                "layout": "vertical",
                "spacing": "md",
                "contents": [
            """, startDateStr, endDateStr));

        for (int i = 0; i < records.size(); i++) {
            RecentSymptomDto record = records.get(i);
            
            String text = String.format("%s %s [%s] %s。%s",
                    record.getOnsetTimezone(),
                    record.getDuration(),
                    record.getSymptomCategoryName() != null ? record.getSymptomCategoryName() : "カテゴリ不明",
                    record.getSymptomName() != null ? record.getSymptomName() : "症状不明",
                    record.getMemo() != null ? record.getMemo() : "");
            
            // JSONエスケープ
            String escapedText = text.replace("\"", "\\\"").replace("\n", "\\n");

            jsonBuilder.append(String.format("""
                  {
                    "type": "box",
                    "layout": "vertical",
                    "spacing": "sm",
                    "contents": [
                      {
                        "type": "text",
                        "text": "%s",
                        "wrap": true,
                        "size": "sm",
                        "color": "#333333"
                      }
                    ]
                  }
            """, escapedText));

            if (i < records.size() - 1) {
                jsonBuilder.append(",\n      {\n        \"type\": \"separator\"\n      },\n");
            }
        }

        jsonBuilder.append("""
                ]
              }
            }
            """);

        try {
            FlexContainer container = objectMapper.readValue(jsonBuilder.toString(), FlexContainer.class);
            FlexMessage flexMessage = new FlexMessage("直近1ヶ月の症状履歴", container);
            lineMessageService.replyMessages(replyToken, List.of(flexMessage));
        } catch (Exception e) {
            log.error("履歴表示用のFlex Message構築に失敗しました", e);
            lineMessageService.replyText(replyToken, "履歴の表示に失敗しました。");
        }
    }
}
