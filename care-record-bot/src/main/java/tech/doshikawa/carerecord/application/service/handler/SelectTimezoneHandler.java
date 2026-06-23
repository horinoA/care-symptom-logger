package tech.doshikawa.carerecord.application.service.handler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import tech.doshikawa.carerecord.domain.entity.UserSession;
import tech.doshikawa.carerecord.domain.repository.UserSessionRepository;
import tech.doshikawa.carerecord.domain.type.InputPhase;
import tech.doshikawa.carerecord.application.service.LineMessageService;

import java.util.Map;
import java.util.Set;

@Slf4j
@Component
@RequiredArgsConstructor
public class SelectTimezoneHandler implements PostbackActionHandler {

    private final UserSessionRepository sessionRepository;
    private final LineMessageService lineMessageService;
    private final tech.doshikawa.carerecord.application.service.UserSessionHelper sessionHelper;

    @Override
    public boolean supports(String action) {
        return "select_timezone".equals(action);
    }

    @Override
    public Set<InputPhase> getAllowedPhases() {
        return Set.of(InputPhase.WAITING_FOR_TIMEZONE);
    }

    @Override
    public void handle(String replyToken, UserSession session, Map<String, String> params) {
        log.info("Executing SelectTimezoneHandler with params: {}", params);
        
        String timezoneCode = params.get("code");
        if (timezoneCode != null) {
            sessionHelper.updateDraft(session, draft -> {
                draft.setTimezone(tech.doshikawa.carerecord.domain.type.TimePeriod.fromCode(timezoneCode));
            });
        }
        
        session.setCurrentPhase(InputPhase.WAITING_FOR_DURATION);
        sessionRepository.save(session);
        
        lineMessageService.replyFlexMessage(replyToken, "所要時間選択", "TimeSpend.json");
    }
}
