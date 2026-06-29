package tech.doshikawa.carerecord.infrastructure.csv;

import org.springframework.stereotype.Component;
import tech.doshikawa.carerecord.domain.dto.CareRecordExportDto;

import java.util.List;
import java.util.StringJoiner;

/**
 * 介護記録データをCSVフォーマットの文字列に変換するインフラストラクチャ・コンポーネント
 */
@Component
public class CsvGenerator {

    private static final String[] HEADER = {
        "発症日", "発症時間帯", "持続時間_分", "持続時間帯", 
        "対象者", "対応者", "症状カテゴリ", "症状", "メモ", "記録日時"
    };

    public String generate(List<CareRecordExportDto> data) {
        StringJoiner csv = new StringJoiner("\r\n");
        
        // ヘッダー行
        StringJoiner headerRow = new StringJoiner(",");
        for (String col : HEADER) {
            headerRow.add(escape(col));
        }
        csv.add(headerRow.toString());

        // データ行
        for (CareRecordExportDto row : data) {
            StringJoiner dataRow = new StringJoiner(",");
            dataRow.add(escape(row.getOnsetDate()));
            dataRow.add(escape(row.getOnsetTimezone()));
            dataRow.add(escape(row.getDurationMinutes() != null ? String.valueOf(row.getDurationMinutes()) : ""));
            dataRow.add(escape(row.getDuration()));
            dataRow.add(escape(row.getTargetRelationship()));
            dataRow.add(escape(row.getToWhoRelationship()));
            dataRow.add(escape(row.getSymptomCategoryName()));
            dataRow.add(escape(row.getSymptomName()));
            dataRow.add(escape(row.getMemo()));
            dataRow.add(escape(row.getRecordedAt()));
            csv.add(dataRow.toString());
        }

        return csv.toString() + "\r\n"; // 最後に改行をつけるのが一般的
    }

    private String escape(String value) {
        if (value == null) {
            return "";
        }
        // ダブルクォーテーションを2つ重ねてエスケープ
        String escaped = value.replace("\"", "\"\"");
        // カンマ、改行、ダブルクォーテーションが含まれる場合は全体をダブルクォーテーションで囲む
        if (escaped.contains(",") || escaped.contains("\n") || escaped.contains("\r") || escaped.contains("\"")) {
            return "\"" + escaped + "\"";
        }
        return escaped;
    }
}
