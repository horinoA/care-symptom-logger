package tech.doshikawa.carerecord.application.service.handler;

import tech.doshikawa.carerecord.domain.entity.UserSession;
import tech.doshikawa.carerecord.domain.type.InputPhase;

import java.util.Map;
import java.util.Set;

public interface PostbackActionHandler {
    
    boolean supports(String action);

    Set<InputPhase> getAllowedPhases();

    void handle(String replyToken, UserSession session, Map<String, String> params);
}
