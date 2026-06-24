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
public class ShowToWhoSubHandler implements PostbackActionHandler {

    private final UserSessionRepository sessionRepository;
    private final LineMessageService lineMessageService;

    @Override
    public boolean supports(String action) {
        return "show_to_who_sub".equals(action);
    }

    @Override
    public Set<InputPhase> getAllowedPhases() {
        return Set.of(InputPhase.WAITING_FOR_TO_WHO);
    }

    @Override
    public void handle(String replyToken, UserSession session, Map<String, String> params) {
        log.info("Executing ShowToWhoSubHandler");
        
        session.setCurrentPhase(InputPhase.WAITING_FOR_TO_WHO_SUB);
        sessionRepository.save(session);
        
        lineMessageService.replyFlexMessage(replyToken, "誰に対しての詳細選択", "ToWhoOther.json");
    }
}
