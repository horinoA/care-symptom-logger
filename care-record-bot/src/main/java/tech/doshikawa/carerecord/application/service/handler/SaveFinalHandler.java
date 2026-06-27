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
public class SaveFinalHandler implements PostbackActionHandler {

    private final UserSessionRepository sessionRepository;
    private final LineMessageService lineMessageService;
    private final tech.doshikawa.carerecord.application.service.UserSessionHelper sessionHelper;

    @Override
    public boolean supports(String action) {
        return "save_final".equals(action);
    }

    @Override
    public Set<InputPhase> getAllowedPhases() {
        return Set.of(InputPhase.WAITING_FOR_FINAL_CONFIRM);
    }

    @Override
    public void handle(String replyToken, UserSession session, Map<String, String> params) {
        log.info("Executing SaveFinalHandler");
        
        // TODO: DBへの保存処理 (CareRecordApplicationService.createCareRecord() の呼び出し)
        
        // 実際の業務処理としては、ここで care_records 等への最終永続化処理を呼び出す
        
        sessionRepository.deleteById(session.getUserId());
        lineMessageService.replyTextByCode(replyToken, "reply.record.saved");
    }
}
