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
        return CareRecordCreateCommand.builder()
                .userId(userId)
                .targetId(this.targetId)
                .toWhoId(this.toWhoId)
                .onsetAt(this.onsetAt)
                .timezone(this.timezone)
                .duration(this.duration)
                .remittedAt(OffsetDateTime.now()) // 完了時点を落ち着いた日時とする
                .memo(this.memo)
                .symptomIds(this.symptomIds)
                .build();
    }
}
