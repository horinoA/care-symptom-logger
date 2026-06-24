package tech.doshikawa.carerecord.application.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import tech.doshikawa.carerecord.application.dto.CareRecordDraft;
import tech.doshikawa.carerecord.domain.entity.UserSession;
import tech.doshikawa.carerecord.domain.type.JsonData;

import java.util.function.Consumer;

@Slf4j
@Component
public class UserSessionHelper {

    private final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule());

    /**
     * セッションのJSONデータからCareRecordDraftを復元する
     */
    public CareRecordDraft getDraft(UserSession session) {
        try {
            if (session.getTempData() == null || session.getTempData().getValue() == null) {
                return new CareRecordDraft();
            }
            return objectMapper.readValue(session.getTempData().getValue(), CareRecordDraft.class);
        } catch (JsonProcessingException e) {
            log.error("JSONパースエラー", e);
            throw new RuntimeException("セッションデータの復元に失敗しました", e);
        }
    }

    /**
     * CareRecordDraftをJSON化してセッションに保存する
     */
    public void saveDraft(UserSession session, CareRecordDraft draft) {
        try {
            session.setTempData(JsonData.of(objectMapper.writeValueAsString(draft)));
        } catch (JsonProcessingException e) {
            log.error("JSONシリアライズエラー", e);
            throw new RuntimeException("セッションデータの保存に失敗しました", e);
        }
    }

    /**
     * ドラフトを取得して更新処理を行い、再度セッションに保存する便利メソッド
     */
    public void updateDraft(UserSession session, Consumer<CareRecordDraft> updater) {
        CareRecordDraft draft = getDraft(session);
        updater.accept(draft);
        saveDraft(session, draft);
    }
}
