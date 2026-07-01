package tech.doshikawa.carerecord.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 過去1ヶ月の症状履歴表示用のフラットなデータを保持するDTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RecentSymptomDto {
    private String onsetTimezone;       // 発症日時と時間帯
    private String duration;            // 持続時間帯
    private String targetRelationship;  // 対象者関係
    private String symptomCategoryName; // 症状カテゴリ
    private String symptomName;         // 症状
    private String memo;                // メモ
}
