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
public class CancelRecordHandler implements PostbackActionHandler {

    private final UserSessionRepository sessionRepository;
    private final LineMessageService lineMessageService;

    @Override
    public boolean supports(String action) {
        return "cancel_record".equals(action);
    }

    @Override
    public Set<InputPhase> getAllowedPhases() {
        return Set.of(InputPhase.WAITING_FOR_SAVE_OR_MEMO, InputPhase.WAITING_FOR_FINAL_CONFIRM);
    }

    @Override
    public void handle(String replyToken, UserSession session, Map<String, String> params) {
        log.info("Executing CancelRecordHandler");
        
        sessionRepository.deleteById(session.getUserId());
        lineMessageService.replyText(replyToken, "記録をキャンセルしました。「開始」と送ると最初からやり直せます。");
    }
}
