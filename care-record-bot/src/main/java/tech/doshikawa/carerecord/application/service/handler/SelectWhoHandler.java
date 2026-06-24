package tech.doshikawa.carerecord.application.service.handler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import tech.doshikawa.carerecord.domain.entity.UserSession;
import tech.doshikawa.carerecord.domain.repository.UserSessionRepository;
import tech.doshikawa.carerecord.domain.type.InputPhase;
import tech.doshikawa.carerecord.application.service.LineMessageService;
import tech.doshikawa.carerecord.application.dto.CareRecordDraft;
import tech.doshikawa.carerecord.application.service.UserSessionHelper;

import java.util.Map;
import java.util.Set;
import java.util.List;    

@Slf4j
@Component
@RequiredArgsConstructor
public class SelectWhoHandler implements PostbackActionHandler {

    private final UserSessionRepository sessionRepository;
    private final LineMessageService lineMessageService;
    private final UserSessionHelper sessionHelper;

    @Override
    public boolean supports(String action) {
        return "select_who".equals(action);
    }

    @Override
    public Set<InputPhase> getAllowedPhases() {
        return Set.of(InputPhase.WAITING_FOR_WHO);
    }

    @Override
    public void handle(String replyToken, UserSession session, Map<String, String> params) {
        log.info("Executing SelectWhoHandler with params: {}", params);
        String jsonFileName;
        String altText;
        
        String targetIdStr = params.get("targetId");
        if (targetIdStr != null) {
            sessionHelper.updateDraft(session, draft -> {
                draft.setTargetId(Integer.parseInt(targetIdStr));
            });
        }
        
        // session内の症状カテゴリー配列を取得し、共通判定メソッドを呼ぶ
        CareRecordDraft draft = sessionHelper.getDraft(session);
        boolean hasTarget = draft.hasTroubleSymptom();
        
        // 症状カテゴリーが「困った」なら「誰が」選択へ、それ以外ならメモ入力選択へ
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
