package tech.doshikawa.carerecord.application.service.handler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import tech.doshikawa.carerecord.domain.entity.UserSession;
import tech.doshikawa.carerecord.domain.type.InputPhase;
import tech.doshikawa.carerecord.application.service.LineMessageService;

import java.time.LocalDate;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

@Slf4j
@Component
@RequiredArgsConstructor
public class RequestCsvHandler implements PostbackActionHandler {

    private final LineMessageService lineMessageService;

    @Value("${app.api.base-url:https://care-record-bot.fly.dev}")
    private String baseUrl;

    @Override
    public boolean supports(String action) {
        return "request_csv".equals(action);
    }

    @Override
    public Set<InputPhase> getAllowedPhases() {
        // リッチメニューからはどの状態（フェーズ）でも叩かれる可能性があるため全許可
        return EnumSet.allOf(InputPhase.class);
    }

    @Override
    public void handle(String replyToken, UserSession session, Map<String, String> params) {
        log.info("Executing RequestCsvHandler");
        
        String userId = session.getUserId();
        
        // 直近3ヶ月の期間を算出
        String startDate = LocalDate.now().minusMonths(3).toString();
        String endDate = LocalDate.now().toString();
        
        // ダウンロードURLの組み立て
        String downloadUrl = String.format("%s/api/v1/records/export?userId=%s&startDate=%s&endDate=%s", 
                baseUrl, userId, startDate, endDate);
                
        String message = "直近3ヶ月分の記録データが準備できました。\n以下のURLからダウンロードしてください。\n※PCでのダウンロードを推奨します。\n\n" + downloadUrl;
        
        lineMessageService.replyText(replyToken, message);
    }
}
