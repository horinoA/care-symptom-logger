package tech.doshikawa.carerecord.application.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.linecorp.bot.messaging.client.MessagingApiClient;
import com.linecorp.bot.messaging.model.TextMessage;
import com.linecorp.bot.messaging.model.ReplyMessageRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tech.doshikawa.carerecord.application.dto.CareRecordCreateCommand;
import tech.doshikawa.carerecord.application.dto.CareRecordDraft;
import tech.doshikawa.carerecord.domain.entity.UserSession;
import tech.doshikawa.carerecord.domain.repository.UserSessionRepository;
import tech.doshikawa.carerecord.domain.type.InputPhase;
import tech.doshikawa.carerecord.domain.type.JsonData;
import tech.doshikawa.carerecord.domain.type.TimePeriod;
import tech.doshikawa.carerecord.domain.type.TimeSpend;

import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class LineWebhookDispatcherService {

    private final UserSessionRepository userSessionRepository;
    private final CareRecordApplicationService careRecordService;
    private final MessagingApiClient messagingApiClient;
    
    // SpringのDIに依存せず、独自にObjectMapperを定義
    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

    @Transactional
    public void handleTextMessage(String replyToken, String userId, String text) {
        log.info("LINEからテキストメッセージを受信しました。ユーザーID: {}, 内容: {}", userId, text);

        if ("リセット".equals(text)) {
            userSessionRepository.deleteById(userId);
            reply(replyToken, "セッションをリセットしました。最初からやり直してください。（「開始」と送る等）");
            return;
        }

        UserSession session = userSessionRepository.findById(userId).orElse(null);

        // セッションがない場合は新規作成して開始
        if (session == null || session.getCurrentPhase().isInitialPhase()) {
            startNewSession(userId);
            session = userSessionRepository.findById(userId).get();
            CareRecordDraft draft = getDraft(session);
            
            // とりあえず1つ目の症状IDを3011番（暴言・暴力など）で仮決めして次へ進むデモ
            draft.setSymptomIds(List.of(3011));
            saveDraft(session, draft);
            
            session.setCurrentPhase(InputPhase.WAITING_FOR_WHO);
            userSessionRepository.save(session);
            
            reply(replyToken, "症状を受け付けました！\n次に対象者（Who）を「who=対象者ID」の形式で送ってください。\n(例: who=201)");
            return;
        }

        InputPhase phase = session.getCurrentPhase();
        CareRecordDraft draft = getDraft(session);

        if (phase == InputPhase.WAITING_FOR_WHO && text.startsWith("who=")) {
            draft.setTargetId(Integer.parseInt(text.replace("who=", "")));
            saveDraft(session, draft);
            
            session.setCurrentPhase(InputPhase.WAITING_FOR_DATE);
            userSessionRepository.save(session);
            
            reply(replyToken, "対象者を記録しました。\n次に発生日時を「date=2026-06-14T10:00:00+09:00」の形式で送ってください。");
            return;
        }

        if (phase == InputPhase.WAITING_FOR_DATE && text.startsWith("date=")) {
            draft.setOnsetAt(OffsetDateTime.parse(text.replace("date=", "")));
            saveDraft(session, draft);
            
            session.setCurrentPhase(InputPhase.WAITING_FOR_TIMEZONE);
            userSessionRepository.save(session);
            
            reply(replyToken, "日時を記録しました。\n次に時間帯を「time=morning」の形式で送ってください。(morning, noon, evening, night)");
            return;
        }

        if (phase == InputPhase.WAITING_FOR_TIMEZONE && text.startsWith("time=")) {
            draft.setTimezone(TimePeriod.fromCode(text.replace("time=", "")));
            saveDraft(session, draft);
            
            session.setCurrentPhase(InputPhase.WAITING_FOR_DURATION);
            userSessionRepository.save(session);
            
            reply(replyToken, "時間帯を記録しました。\n次に経過時間を「duration=1」の形式で送ってください。(1: 30分, 2: 2時間, 3: 半日, 4: 1日以上)");
            return;
        }

        if (phase == InputPhase.WAITING_FOR_DURATION && text.startsWith("duration=")) {
            draft.setDuration(TimeSpend.fromId(text.replace("duration=", "")));
            saveDraft(session, draft);
            
            session.setCurrentPhase(InputPhase.WAITING_FOR_MEMO_TEXT);
            userSessionRepository.save(session);
            
            reply(replyToken, "経過時間を記録しました。\n最後にメモを入力してください。");
            return;
        }

        if (phase == InputPhase.WAITING_FOR_MEMO_TEXT) {
            draft.setMemo(text);
            
            // 完了！DBに保存
            CareRecordCreateCommand command = draft.toCommand(userId);
            careRecordService.createCareRecord(command);
            
            userSessionRepository.deleteById(userId);
            reply(replyToken, "介護記録の保存が完了しました！お疲れ様でした。");
            return;
        }

        reply(replyToken, "現在のフェーズ（" + phase + "）に合致しない入力です。「リセット」と送ると最初からやり直せます。");
    }

    @Transactional
    public void handlePostback(String replyToken, String userId, String data) {
        log.info("LINEからPostbackイベントを受信しました。ユーザーID: {}, データ: {}", userId, data);
        reply(replyToken, "Postbackを受け付けました: " + data);
    }

    private void startNewSession(String userId) {
        CareRecordDraft draft = new CareRecordDraft();
        UserSession session = UserSession.builder()
                .userId(userId)
                .sessionId(UUID.randomUUID())
                .currentPhase(InputPhase.WAITING_FOR_SYMPTOM_CATEGORY)
                .tempData(JsonData.of(toJson(draft)))
                .isNew(true)
                .build();
        userSessionRepository.save(session);
    }

    private CareRecordDraft getDraft(UserSession session) {
        try {
            return objectMapper.readValue(session.getTempData().getValue(), CareRecordDraft.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("JSONパースエラー", e);
        }
    }

    private void saveDraft(UserSession session, CareRecordDraft draft) {
        session.setTempData(JsonData.of(toJson(draft)));
    }

    private String toJson(Object obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("JSONシリアライズエラー", e);
        }
    }

    private void reply(String replyToken, String text) {
        try {
            messagingApiClient.replyMessage(new ReplyMessageRequest(
                replyToken, 
                List.of(new TextMessage(text)), 
                false
            )).get(); // 同期呼び出し
        } catch (Exception e) {
            log.warn("Replyの送信に失敗しました（テスト環境でLINE接続がない場合は無視してOKです）: {}", e.getMessage());
        }
    }
}
