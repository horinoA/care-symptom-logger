package tech.doshikawa.carerecord.domain.type;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalTime;

/**
 * 介護記録の時間帯を定義するEnum。
 * LINEのPostbackデータとのマッピングも担う。
 */
@Getter
@AllArgsConstructor
public enum TimePeriod {

    MORNING("morning", "🌅 朝", LocalTime.of(8, 0)),
    NOON("noon", "☀️ 昼", LocalTime.of(12, 0)),
    EVENING("evening", "🌆 夕方", LocalTime.of(17, 0)),
    NIGHT("night", "🌙 夜・深夜", LocalTime.of(22, 0));

    // LINEのpostback data (timezone=xxx) で送られてくる値
    private final String code;
    
    // 画面表示やログ出力用のラベル
    private final String label;

    // 時間帯の代表的な開始時刻
    private final LocalTime startTime;

    /**
     * postbackのコード文字列からEnumを取得する
     * * @param code LINEから送信されたコード (例: "morning")
     * @return 該当するTimePeriod
     * @throws IllegalArgumentException 未知のコードが渡された場合
     */
    public static TimePeriod fromCode(String code) {
        for (TimePeriod period : values()) {
            if (period.getCode().equals(code)) {
                return period;
            }
        }
        throw new IllegalArgumentException("error.enum.time_period.unknown");
    }
}