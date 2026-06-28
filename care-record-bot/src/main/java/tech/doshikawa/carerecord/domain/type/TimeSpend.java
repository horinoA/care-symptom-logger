package tech.doshikawa.carerecord.domain.type;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.Duration;

/**
 * 介護対応にかかった時間（所要時間）を定義するEnum。
 * LINEのPostbackデータ (timespendId) とのマッピングを担う。
 */
@Getter
@AllArgsConstructor
public enum TimeSpend {

    UP_TO_30_MIN("1", "🕒 30分まで", Duration.ofMinutes(30)),
    AROUND_2_HOURS("2", "🕒 ２時間程度", Duration.ofHours(2)),
    HALF_DAY("3", "🕒 半日", Duration.ofHours(6)),
    OVER_1_DAY("4", "🕒 １日以上", Duration.ofHours(24));

    // LINEのpostback data (timespendId=xxx) で送られてくるID文字列
    private final String id;
    
    // 画面表示やログ出力用のラベル
    private final String label;

    // 計算用の具体的な所要時間
    private final Duration durationAmount;

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
        throw new IllegalArgumentException("error.enum.time_spend.unknown");
    }
}