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
public class NextToDateHandler implements PostbackActionHandler {

    private final UserSessionRepository sessionRepository;
    private final LineMessageService lineMessageService;

    @Override
    public boolean supports(String action) {
        return "next_to_date".equals(action);
    }

    @Override
    public Set<InputPhase> getAllowedPhases() {
        return Set.of(InputPhase.WAITING_FOR_LOOP_CHOICE);
    }

    @Override
    public void handle(String replyToken, UserSession session, Map<String, String> params) {
        log.info("Executing NextToDateHandler");
        
        session.setCurrentPhase(InputPhase.WAITING_FOR_DATE);
        sessionRepository.save(session);
        
        lineMessageService.replyFlexMessage(replyToken, "日時選択", "Calender.json");
    }
}
