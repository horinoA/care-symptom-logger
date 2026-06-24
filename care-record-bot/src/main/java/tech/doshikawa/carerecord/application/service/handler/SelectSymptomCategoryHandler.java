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
public class SelectSymptomCategoryHandler implements PostbackActionHandler {

    private final UserSessionRepository sessionRepository;
    private final LineMessageService lineMessageService;

    @Override
    public boolean supports(String action) {
        return "select_symptom_category".equals(action);
    }

    @Override
    public Set<InputPhase> getAllowedPhases() {
        return Set.of(
            InputPhase.WAITING_FOR_SYMPTOM_CATEGORY,
            InputPhase.WAITING_FOR_SYMPTOM_DETAIL
        );
    }

    @Override
    public void handle(String replyToken, UserSession session, Map<String, String> params) {
        String categoryId = params.get("categoryId");
        log.info("Symptoms category selected. CategoryId: {}", categoryId);

        // 次のフェーズへ進行
        session.setCurrentPhase(InputPhase.WAITING_FOR_SYMPTOM_DETAIL);
        sessionRepository.save(session);

        // セッションのtempData(CareRecordDraft)の更新は、具体的なsymptomIdが選ばれた際に行うためここではスキップ

        String jsonFileName = switch (categoryId) {
            case "30" -> "Sympton_Trouble.json";
            case "40" -> "Symptom_MemoryWord.json";
            case "50" -> "Sympton_Behavior.json";
            case "60" -> "Sympton_Mistake.json";
            default -> "Sympton_Trouble.json";
        };

        lineMessageService.replyFlexMessage(replyToken, "症状の詳細を選択してください", jsonFileName);
    }
}
