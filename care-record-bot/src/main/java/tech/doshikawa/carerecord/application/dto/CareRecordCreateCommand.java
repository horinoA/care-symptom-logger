package tech.doshikawa.carerecord.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Data;
import tech.doshikawa.carerecord.domain.type.TimePeriod;
import tech.doshikawa.carerecord.domain.type.TimeSpend;

import java.time.OffsetDateTime;
import java.util.List;

/**
 * 介護記録の新規登録を行うためのCommand(DTO)
 * ユーザー入力の形式バリデーションを担当します。
 * （実在チェックなどのビジネスルールはService層で実施します）
 */
@Data
@Builder
public class CareRecordCreateCommand {

    @NotBlank(message = "{validation.carerecord.userId.notnull}")
    private String userId;

    @NotNull(message = "{validation.carerecord.targetId.notnull}")
    private Integer targetId;

    // 「困った」等、対象がいない場合はNULLを許容する
    private Integer toWhoId;

    @NotNull(message = "{validation.carerecord.occurredAt.notnull}")
    private OffsetDateTime onsetAt;

    @NotNull(message = "{validation.carerecord.timezone.notnull}")
    private TimePeriod timezone;

    @NotNull(message = "{validation.carerecord.duration.notnull}")
    private TimeSpend duration;

    @NotNull(message = "{validation.carerecord.remittedAt.notnull}")
    private OffsetDateTime remittedAt;

    @Size(max = 1000, message = "{validation.carerecord.memo.size}")
    private String memo;

    // 症状のリスト（最低1つは必要）
    @NotEmpty(message = "{validation.carerecord.symptoms.notempty}")
    private List<Integer> symptomIds;
}
