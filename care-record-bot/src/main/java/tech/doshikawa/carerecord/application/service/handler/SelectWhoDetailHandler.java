package tech.doshikawa.carerecord.application.service.handler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import tech.doshikawa.carerecord.domain.entity.UserSession;
import tech.doshikawa.carerecord.domain.repository.UserSessionRepository;
import tech.doshikawa.carerecord.domain.type.InputPhase;
import tech.doshikawa.carerecord.application.service.LineMessageService;
import tech.doshikawa.carerecord.application.dto.CareRecordDraft;

import java.util.Map;
import java.util.Set;

@Slf4j
@Component
@RequiredArgsConstructor
public class SelectWhoDetailHandler implements PostbackActionHandler {

    private final UserSessionRepository sessionRepository;
    private final LineMessageService lineMessageService;
    private final tech.doshikawa.carerecord.application.service.UserSessionHelper sessionHelper;

    @Override
    public boolean supports(String action) {
        return "select_who_detail".equals(action);
    }

    @Override
    public Set<InputPhase> getAllowedPhases() {
        return Set.of(InputPhase.WAITING_FOR_WHO_SUB);
    }

    @Override
    public void handle(String replyToken, UserSession session, Map<String, String> params) {
        log.info("Executing SelectWhoDetailHandler with params: {}", params);
        
        String targetIdStr = params.get("targetId");
        if (targetIdStr != null) {
            sessionHelper.updateDraft(session, draft -> {
                draft.setTargetId(Integer.parseInt(targetIdStr));
            });
        }
        
        CareRecordDraft draft = sessionHelper.getDraft(session);
        boolean hasTarget = draft.hasTroubleSymptom();
        
        String jsonFileName;
        String altText;
        if (hasTarget) {
            session.setCurrentPhase(InputPhase.WAITING_FOR_TO_WHO);
            jsonFileName = "ToWho.json";
            altText = "誰に対しての行動か選択";
        } else {
            session.setCurrentPhase(InputPhase.WAITING_FOR_SAVE_OR_MEMO);
            jsonFileName = "MemoYN.json";
            altText = "メモ有無選択";
        }
        
        sessionRepository.save(session);
        
        lineMessageService.replyFlexMessage(replyToken, altText, jsonFileName);
    }
}
