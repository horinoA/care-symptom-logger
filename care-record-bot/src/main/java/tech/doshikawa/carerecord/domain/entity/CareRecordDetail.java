package tech.doshikawa.carerecord.domain.entity;

import lombok.Builder;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.domain.Persistable;
import org.springframework.data.relational.core.mapping.Table;

/**
 * 介護記録の子テーブル(event.care_record_details)に対応するEntity
 */
@Data
@Builder
@Table("event.care_record_details")
public class CareRecordDetail implements Persistable<Long> {
    
    @Id
    private Long id;
    
    // Spring Data JDBCが自動で外部キー(care_records_id)を管理するため
    // このクラスには親のIDを持たせる必要がありません（持たせてもOKですが自動処理に任せる方が安全です）
    
    private Integer symptomId;

    // 親Entityと同様、手動発番のIDを用いてInsertさせるためのフラグ
    @Transient
    @Builder.Default
    private boolean isNew = true;

    @Override
    public boolean isNew() {
        return isNew;
    }
}
