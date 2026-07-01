package tech.doshikawa.carerecord.application.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.linecorp.bot.messaging.client.MessagingApiClient;
import com.linecorp.bot.messaging.model.TextMessage;
import com.linecorp.bot.messaging.model.ReplyMessageRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tech.doshikawa.carerecord.application.dto.CareRecordDraft;
import tech.doshikawa.carerecord.domain.entity.UserSession;
import tech.doshikawa.carerecord.domain.repository.UserSessionRepository;
import tech.doshikawa.carerecord.domain.type.InputPhase;
import tech.doshikawa.carerecord.domain.type.JsonData;
import tech.doshikawa.carerecord.domain.type.UserValidationConstraints;

import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import tech.doshikawa.carerecord.domain.entity.LineWebhookEvent;
import tech.doshikawa.carerecord.domain.repository.LineWebhookEventRepository;
import tech.doshikawa.carerecord.application.service.handler.PostbackActionHandler;
import com.linecorp.bot.webhook.model.PostbackEvent;

@Slf4j
@Service
@RequiredArgsConstructor
public class LineWebhookDispatcherService {

    private final UserSessionRepository userSessionRepository;
    private final MessagingApiClient messagingApiClient;
    private final LineWebhookEventRepository eventRepository;
    private final List<PostbackActionHandler> actionHandlers;
    private final LineMessageService lineMessageService;
    private final UserSessionRepository sessionRepository;
    private final UserSessionHelper sessionHelper;
    private final MessageSource messageSource;
    
    // SpringのDIに依存せず、独自にObjectMapperを定義
    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

    @Transactional
    public void handleTextMessage(String replyToken, String userId, String text) {
        log.info("LINEからテキストメッセージを受信しました。ユーザーID: {}, 内容: {}", userId, text);

        if ("リセット".equals(text)) {
            userSessionRepository.deleteById(userId);
            lineMessageService.replyTextByCode(replyToken, "reply.session.reset");
            return;
        }

        // --- テキスト「ダウンロード」迎撃の陣 ---
        if ("ダウンロード".equals(text)) {
            UserSession currentSession = userSessionRepository.findById(userId)
                    .orElseGet(() -> {
                        log.info("CSV出力要求のため、ダミーセッションを錬成します");
                        return createDummySession(userId);
                    });
            
            // "request_csv" をサポートするハンドラーへ委譲
            executeActionHandler("request_csv", replyToken, currentSession);
            return;
        }

        // --- テキスト「表示」迎撃の陣 ---
        if ("表示".equals(text)) {
            UserSession currentSession = userSessionRepository.findById(userId)
                    .orElseGet(() -> {
                        log.info("履歴表示要求のため、ダミーセッションを錬成します");
                        return createDummySession(userId);
                    });
            
            // "show_history" をサポートするハンドラーへ委譲
            executeActionHandler("show_history", replyToken, currentSession);
            return;
        }

        UserSession session = userSessionRepository.findById(userId).orElse(null);

        // 「開始」または「記録」が入力された場合は、現在のフェーズを問わず強制的にセッションをリセットして初期化する
        if ("記録".equals(text) || "開始".equals(text)) {
            startNewSession(userId, session);
            
            //開始時メッセージ
            // 例外による反乱を防ぐため、第三引数に「デフォルトのメッセージ」を設定して強固な防壁を築く
            String fallbackMessage = "日々の介護、誠にお疲れ様です。\n記録したい症状のカテゴリを選択してください。";
            String introText = messageSource.getMessage("reply.intro.category", null, fallbackMessage, Locale.JAPAN);
            log.info(introText);
            lineMessageService.replyTextAndFlexMessage(replyToken, introText, "症状カテゴリ選択", "SymptomCategoryName.json");
            return;
        }

        // セッションがない、または初期フェーズの場合はテキスト入力でデモ進行（開発用）
        if (session == null || session.getCurrentPhase().isInitialPhase()) {
            lineMessageService.replyTextByCode(replyToken, "reply.prompt.start");
            return;
        }

        //現在どのフェーズ（InputPhase）か取得
        InputPhase phase = session.getCurrentPhase();

        //メモ入力状態からテキストが入力された
        if (phase == InputPhase.WAITING_FOR_MEMO_TEXT) {
            log.info("Executing SelecthandleTextMessage_WAITING_FOR_MEMO_TEXT: {}", text);
            
            if (text != null) {
                //メモが入力済みで、文字数制限を超える場合は再度入力
                if (text.length() > UserValidationConstraints.MAX_COMMENT_LENGTH){
                    lineMessageService.replyTextByCode(replyToken, "validation.carerecord.memo.size", UserValidationConstraints.MAX_COMMENT_LENGTH_STR);
                    return;
                }
                sessionHelper.updateDraft(session, draft -> {
                    draft.setMemo(text);
                });
            }
            
            session.setCurrentPhase(InputPhase.WAITING_FOR_FINAL_CONFIRM);
            sessionRepository.save(session);
            lineMessageService.replyFlexMessage(replyToken, "メモ付きで保存するか", "MemoAddYN.json");
            return;
        }

        lineMessageService.replyTextByCode(replyToken, "reply.error.invalid.phase", phase.toString());
    }

    @Transactional
    public void handlePostback(PostbackEvent event) {
        String eventId = event.webhookEventId();
        String userId = event.source() != null ? event.source().userId() : null;
        
        log.info("LINEからPostbackイベントを受信しました。ユーザーID: {}, eventId: {}", userId, eventId);
        if (userId == null) return;

        UserSession session = userSessionRepository.findById(userId).orElse(null);

        // 【第一の盾】冪等性のチェック
        if (eventRepository.existsById(eventId)) {
            log.info("既に処理済みのイベントです。無視して200を返します。eventId: {}", eventId);
            return;
        }
        
        eventRepository.save(LineWebhookEvent.builder()
            .eventId(eventId)
            .userId(userId)
            .sessionId(session != null ? session.getSessionId() : null)
            .eventType("postback")
            .lineTimestamp(OffsetDateTime.ofInstant(java.time.Instant.ofEpochMilli(event.timestamp()), ZoneId.systemDefault()))
            .status("PROCESSING")
            .isNew(true)
            .build());

        String data = event.postback().data();
        Map<String, String> params = parsePostbackData(data);
        if (event.postback().params() != null) {
            params.putAll(event.postback().params());
        }
        String action = params.get("action");

        if (session == null) {
            if ("request_csv".equals(action)) {
                log.info("セッションが存在しませんが、{} のためダミーセッションを錬成します", action);
                session = createDummySession(userId);
            } else {
                throw new IllegalStateException("error.session.notfound");
            }
        }

        if (action == null) {
            log.warn("actionが指定されていません: {}", data);
            markEventStatus(eventId, "ERROR", session.getSessionId());
            return;
        }

        PostbackActionHandler handler = actionHandlers.stream()
            .filter(h -> h.supports(action))
            .findFirst()
            .orElse(null);

        if (handler == null) {
            log.warn("未知のアクションです: {}", action);
            markEventStatus(eventId, "ERROR", session.getSessionId());
            return;
        }

        // 【第二の盾】フェーズチェック
        if (!handler.getAllowedPhases().contains(session.getCurrentPhase())) {
            log.warn("現在のフェーズ {} では許可されないアクションです: {}", session.getCurrentPhase(), action);
            lineMessageService.replyTextByCode(event.replyToken(), "reply.error.invalid.operation");
            markEventStatus(eventId, "ERROR", session.getSessionId());
            return;
        }

        // 将軍に処理を委譲
        handler.handle(event.replyToken(), session, params);
        //Usersessionに保存
        CareRecordDraft draft = getDraft(session);
        saveDraft(session, draft);
        // 戦果の記録
        markEventStatus(eventId, "COMPLETED", session.getSessionId());
    }

    private void markEventStatus(String eventId, String status, UUID sessionId) {
        eventRepository.findById(eventId).ifPresent(e -> {
            e.setStatus(status);
            if (sessionId != null) {
                e.setSessionId(sessionId);
            }
            eventRepository.save(e);
        });
    }

    private Map<String, String> parsePostbackData(String data) {
        Map<String, String> params = new HashMap<>();
        if (data == null || data.isEmpty()) return params;
        String[] pairs = data.split("&");
        for (String pair : pairs) {
            int idx = pair.indexOf("=");
            if (idx > 0) {
                String key = URLDecoder.decode(pair.substring(0, idx), StandardCharsets.UTF_8);
                String value = URLDecoder.decode(pair.substring(idx + 1), StandardCharsets.UTF_8);
                params.put(key, value);
            }
        }
        return params;
    }

    private void startNewSession(String userId, UserSession existingSession) {
        CareRecordDraft draft = new CareRecordDraft();
        if (existingSession != null) {
            existingSession.setSessionId(UUID.randomUUID());
            existingSession.setCurrentPhase(InputPhase.WAITING_FOR_SYMPTOM_CATEGORY);
            existingSession.setTempData(JsonData.of(toJson(draft)));
            userSessionRepository.save(existingSession);
        } else {
            UserSession session = UserSession.builder()
                    .userId(userId)
                    .sessionId(UUID.randomUUID())
                    .currentPhase(InputPhase.WAITING_FOR_SYMPTOM_CATEGORY)
                    .tempData(JsonData.of(toJson(draft)))
                    .isNew(true)
                    .build();
            userSessionRepository.save(session);
        }
    }

    private CareRecordDraft getDraft(UserSession session) {
        try {
            return objectMapper.readValue(session.getTempData().getValue(), CareRecordDraft.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("error.json.parse", e);
        }
    }

    private void saveDraft(UserSession session, CareRecordDraft draft) {
        session.setTempData(JsonData.of(toJson(draft)));
    }

    private String toJson(Object obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("error.json.serialize", e);
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

    /**
     * セッション未構築のユーザー向けの、一時的なダミーセッションを錬成する
     */
    private UserSession createDummySession(String userId) {
        tech.doshikawa.carerecord.application.dto.CareRecordDraft draft = new tech.doshikawa.carerecord.application.dto.CareRecordDraft();
        return UserSession.builder()
                .userId(userId)
                .sessionId(UUID.randomUUID())
                .currentPhase(tech.doshikawa.carerecord.domain.type.InputPhase.WAITING_FOR_SYMPTOM_CATEGORY)
                .tempData(tech.doshikawa.carerecord.domain.type.JsonData.of(toJson(draft)))
                .isNew(true)
                .build();
    }

    /**
     * 指定されたアクションに対応する将軍（ハンドラー）を呼び出し、処理を委譲する
     */
    private void executeActionHandler(String action, String replyToken, UserSession session) {
        actionHandlers.stream()
                .filter(h -> h.supports(action))
                .findFirst()
                .ifPresentOrElse(
                        handler -> {
                            log.info("アクション '{}' のハンドラー {} を発動します", action, handler.getClass().getSimpleName());
                            handler.handle(replyToken, session, java.util.Collections.emptyMap());
                        },
                        () -> log.warn("アクション '{}' に対応するハンドラーが見つかりません", action)
                );
    }
}
