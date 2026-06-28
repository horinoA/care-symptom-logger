package tech.doshikawa.carerecord.application.service.handler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import tech.doshikawa.carerecord.domain.entity.UserSession;
import tech.doshikawa.carerecord.domain.repository.UserSessionRepository;
import tech.doshikawa.carerecord.domain.type.InputPhase;
import tech.doshikawa.carerecord.application.service.LineMessageService;
import tech.doshikawa.carerecord.application.service.UserSessionHelper;
import tech.doshikawa.carerecord.application.service.CareRecordApplicationService;
import tech.doshikawa.carerecord.application.dto.CareRecordDraft;
import tech.doshikawa.carerecord.application.dto.CareRecordCreateCommand;

import java.util.Map;
import java.util.Set;

@Slf4j
@Component
@RequiredArgsConstructor
public class SaveNoMemoHandler implements PostbackActionHandler {

    private final UserSessionRepository sessionRepository;
    private final LineMessageService lineMessageService;
    private final UserSessionHelper sessionHelper;
    private final CareRecordApplicationService careRecordService;

    @Override
    public boolean supports(String action) {
        return "save_no_memo".equals(action);
    }

    @Override
    public Set<InputPhase> getAllowedPhases() {
        return Set.of(InputPhase.WAITING_FOR_SAVE_OR_MEMO);
    }

    @Override
    public void handle(String replyToken, UserSession session, Map<String, String> params) {
        log.info("Executing SaveNoMemoHandler");
        
        CareRecordDraft draft = sessionHelper.getDraft(session);
        CareRecordCreateCommand command = draft.toCommand(session.getUserId());

        careRecordService.createCareRecord(command);
        
        sessionRepository.deleteById(session.getUserId());
        lineMessageService.replyTextByCode(replyToken, "reply.record.saved.nomemo");
    }
}
