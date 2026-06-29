package tech.doshikawa.carerecord.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * CSVエクスポート用のフラットなデータを保持するDTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CareRecordExportDto {
    private String onsetDate;           // 発症日
    private String onsetTimezone;       // 発症時間帯
    private Integer durationMinutes;    // 持続時間_分
    private String duration;            // 持続時間帯
    private String targetRelationship;  // 対象者
    private String toWhoRelationship;   // 対応者
    private String symptomCategoryName; // 症状カテゴリ
    private String symptomName;         // 症状
    private String memo;                // メモ
    private String recordedAt;          // 記録日時
}
