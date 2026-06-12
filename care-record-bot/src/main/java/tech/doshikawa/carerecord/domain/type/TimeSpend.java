package tech.doshikawa.carerecord.domain.type;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 介護対応にかかった時間（所要時間）を定義するEnum。
 * LINEのPostbackデータ (timespendId) とのマッピングを担う。
 */
@Getter
@AllArgsConstructor
public enum TimeSpend {

    UP_TO_30_MIN("1", "🕒 30分まで"),
    AROUND_2_HOURS("2", "🕒 ２時間程度"),
    HALF_DAY("3", "🕒 半日"),
    OVER_1_DAY("4", "🕒 １日以上");

    // LINEのpostback data (timespendId=xxx) で送られてくるID文字列
    private final String id;
    
    // 画面表示やログ出力用のラベル
    private final String label;

    /**
     * postbackのID文字列からEnumを取得する
     * * @param id LINEから送信されたID (例: "1")
     * @return 該当するTimeSpend
     * @throws IllegalArgumentException 未知のIDが渡された場合
     */
    public static TimeSpend fromId(String id) {
        for (TimeSpend timeSpend : values()) {
            if (timeSpend.getId().equals(id)) {
                return timeSpend;
            }
        }
        throw new IllegalArgumentException("問答無用でエラー: 未知の所要時間IDです -> " + id);
    }
}