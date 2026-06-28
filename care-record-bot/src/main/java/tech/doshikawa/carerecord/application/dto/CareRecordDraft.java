package tech.doshikawa.carerecord.application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import tech.doshikawa.carerecord.domain.type.TimePeriod;
import tech.doshikawa.carerecord.domain.type.TimeSpend;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * ユーザーがLINEで入力中の介護記録の途中状態（ドラフト）を保持するDTO。
 * このオブジェクトがJSONにシリアライズされ、UserSessionのtempDataとしてDBに保存されます。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CareRecordDraft {

    // 1枚目
    @Builder.Default
    private List<Integer> symptomIds = new ArrayList<>();
    
    // 2枚目
    private OffsetDateTime onsetAt;
    private TimePeriod timezone;
    private TimeSpend duration;
    
    // 3枚目
    private Integer targetId;
    private Integer toWhoId;
    
    // 4枚目
    private String memo;
    
    // 完了時に登録用のCommandに変換するメソッド
    public CareRecordCreateCommand toCommand(String userId) {
        OffsetDateTime actualOnsetAt = this.onsetAt;
        OffsetDateTime calculatedRemittedAt = OffsetDateTime.now();

        if (this.onsetAt != null && this.timezone != null && this.duration != null) {
            actualOnsetAt = this.onsetAt.withOffsetSameInstant(java.time.ZoneOffset.ofHours(9))
                                        .with(this.timezone.getStartTime());
            calculatedRemittedAt = actualOnsetAt.plus(this.duration.getDurationAmount());
        }

        return CareRecordCreateCommand.builder()
                .userId(userId)
                .targetId(this.targetId)
                .toWhoId(this.toWhoId)
                .onsetAt(actualOnsetAt)
                .timezone(this.timezone)
                .duration(this.duration)
                .remittedAt(calculatedRemittedAt)
                .memo(this.memo)
                .symptomIds(this.symptomIds)
                .build();
    }

    /**
     * 「困った」カテゴリー（3000番台）の症状が選択されているかを判定する
     */
    @com.fasterxml.jackson.annotation.JsonIgnore
    public boolean hasTroubleSymptom() {
        if (this.symptomIds == null || this.symptomIds.isEmpty()) {
            throw new IllegalStateException("error.draft.nosymptom");
        }
        return this.symptomIds.stream()
                .anyMatch(id -> id >= 3000 && id <= 3999);
    }
}
