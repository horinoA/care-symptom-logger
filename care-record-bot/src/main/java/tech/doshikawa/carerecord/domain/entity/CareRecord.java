package tech.doshikawa.carerecord.domain.entity;

import lombok.Builder;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.domain.Persistable;
import org.springframework.data.relational.core.mapping.MappedCollection;
import org.springframework.data.relational.core.mapping.Table;
import tech.doshikawa.carerecord.domain.type.TimePeriod;
import tech.doshikawa.carerecord.domain.type.TimeSpend;

import java.time.OffsetDateTime;
import java.util.Set;

/**
 * 介護記録の親テーブル(event.care_records)に対応するEntity
 */
@Data
@Builder
@Table("event.care_records")
public class CareRecord implements Persistable<Long> {
    
    @Id
    private Long id;
    
    private String userId;
    private Integer targetId;
    private Integer toWhoId;
    private OffsetDateTime onsetAt;
    private TimePeriod timezone;
    private TimeSpend duration;
    private OffsetDateTime remittedAt;
    private String memo;
    
    @Builder.Default
    private Boolean isDelete = false;
    
    private OffsetDateTime createdAt;

    // 子テーブル（care_record_details）への一対多マッピング
    // 親が保存されると子も連動して保存（Insert/Update/Delete）されます
    @MappedCollection(idColumn = "care_records_id")
    private Set<CareRecordDetail> details;

    // --- Persistableの実装 ---
    // Spring Data JDBCは、@Idがnull以外の場合に「Update」として振る舞おうとします。
    // SnowflakeID等で手動発番して「Insert」させるため、isNewフラグで新規であることを明示します。
    @Transient
    @Builder.Default
    private boolean isNew = true;

    @Override
    public boolean isNew() {
        return isNew;
    }
}
