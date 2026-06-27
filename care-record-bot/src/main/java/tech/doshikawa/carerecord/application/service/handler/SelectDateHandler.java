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
public class SelectDateHandler implements PostbackActionHandler {

    private final UserSessionRepository sessionRepository;
    private final LineMessageService lineMessageService;
    private final tech.doshikawa.carerecord.application.service.UserSessionHelper sessionHelper;

    @Override
    public boolean supports(String action) {
        return "select_date".equals(action);
    }

    @Override
    public Set<InputPhase> getAllowedPhases() {
        return Set.of(InputPhase.WAITING_FOR_DATE);
    }

    @Override
    public void handle(String replyToken, UserSession session, Map<String, String> params) {
        log.info("Executing SelectDateHandler with params: {}", params);
        
        String dateStr = params.get("date");
        if (dateStr != null) {
            sessionHelper.updateDraft(session, draft -> {
                draft.setOnsetAt(java.time.OffsetDateTime.parse(dateStr + "T00:00:00+09:00"));
            });
            session.setCurrentPhase(InputPhase.WAITING_FOR_TIMEZONE);
            sessionRepository.save(session);
            lineMessageService.replyTextAndFlexMessageByCode(replyToken, "reply.date.selected", new Object[]{dateStr}, "時間帯選択", "Timezone.json");
        } else {
            session.setCurrentPhase(InputPhase.WAITING_FOR_TIMEZONE);
            sessionRepository.save(session);
            lineMessageService.replyFlexMessage(replyToken, "時間帯選択", "Timezone.json");
        }
    }
}
