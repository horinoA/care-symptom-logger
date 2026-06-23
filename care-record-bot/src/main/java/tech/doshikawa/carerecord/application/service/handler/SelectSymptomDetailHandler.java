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
public class SelectSymptomDetailHandler implements PostbackActionHandler {

    private final UserSessionRepository sessionRepository;
    private final LineMessageService lineMessageService;
    private final tech.doshikawa.carerecord.application.service.UserSessionHelper sessionHelper;

    @Override
    public boolean supports(String action) {
        return "select_symptom_detail".equals(action);
    }

    @Override
    public Set<InputPhase> getAllowedPhases() {
        return Set.of(InputPhase.WAITING_FOR_SYMPTOM_DETAIL);
    }

    @Override
    public void handle(String replyToken, UserSession session, Map<String, String> params) {
        log.info("Executing SelectSymptomDetailHandler with params: {}", params);
        
        String symptomIdStr = params.get("symptomId");
        if (symptomIdStr != null) {
            Integer symptomId = Integer.parseInt(symptomIdStr);
            sessionHelper.updateDraft(session, draft -> {
                if (draft.getSymptomIds() == null) {
                    draft.setSymptomIds(new java.util.ArrayList<>());
                }
                draft.getSymptomIds().add(symptomId);
            });
        }
        
        session.setCurrentPhase(InputPhase.WAITING_FOR_LOOP_CHOICE);
        sessionRepository.save(session);
        
        lineMessageService.replyFlexMessage(replyToken, "症状の追加入力確認", "SymptomYN.json");
    }
}
