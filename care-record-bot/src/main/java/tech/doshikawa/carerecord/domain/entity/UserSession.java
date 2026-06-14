package tech.doshikawa.carerecord.domain.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.domain.Persistable;
import org.springframework.data.relational.core.mapping.Table;
import tech.doshikawa.carerecord.domain.type.InputPhase;
import tech.doshikawa.carerecord.domain.type.JsonData;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * session.user_sessions テーブルに対応するEntity
 * ユーザーごとの現在の入力フェーズと一時データを保持する
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "user_sessions", schema = "session")
public class UserSession implements Persistable<String> {

    @Id
    private String userId;

    private UUID sessionId;

    private InputPhase currentPhase;

    // JSONBカラムに保存されるデータ（Jackson等でCareRecordDraftから文字列化してセットする）
    private JsonData tempData;

    @Builder.Default
    private OffsetDateTime createdAt = OffsetDateTime.now();

    @Builder.Default
    private OffsetDateTime updatedAt = OffsetDateTime.now();

    // --- Persistableの実装 ---
    // UserSessionのIDはユーザーID（String）なので、Spring Data JDBCは自動採番できず、
    // save()を呼ぶとデフォルトで「Update」文を発行しようとします。
    // そのため、新規保存(Insert)の場合は isNew フラグを true にして明示する必要があります。
    @Transient
    @Builder.Default
    private boolean isNew = false;

    @Override
    public String getId() {
        return this.userId;
    }

    @Override
    public boolean isNew() {
        return this.isNew;
    }
}
