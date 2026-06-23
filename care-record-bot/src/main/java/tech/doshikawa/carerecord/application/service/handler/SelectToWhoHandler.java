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
public class SelectToWhoHandler implements PostbackActionHandler {

    private final UserSessionRepository sessionRepository;
    private final LineMessageService lineMessageService;
    private final tech.doshikawa.carerecord.application.service.UserSessionHelper sessionHelper;

    @Override
    public boolean supports(String action) {
        return "select_to_who".equals(action);
    }

    @Override
    public Set<InputPhase> getAllowedPhases() {
        return Set.of(InputPhase.WAITING_FOR_TO_WHO);
    }

    @Override
    public void handle(String replyToken, UserSession session, Map<String, String> params) {
        log.info("Executing SelectToWhoHandler with params: {}", params);
        
        String toWhoIdStr = params.get("toWhoId");
        if (toWhoIdStr != null) {
            sessionHelper.updateDraft(session, draft -> {
                draft.setToWhoId(Integer.parseInt(toWhoIdStr));
            });
        }
        
        session.setCurrentPhase(InputPhase.WAITING_FOR_SAVE_OR_MEMO);
        sessionRepository.save(session);
        
        lineMessageService.replyFlexMessage(replyToken, "メモ有無選択", "MemoYN.json");
    }
}
