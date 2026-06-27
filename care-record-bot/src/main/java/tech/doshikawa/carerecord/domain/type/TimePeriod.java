package tech.doshikawa.carerecord.domain.type;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 介護記録の時間帯を定義するEnum。
 * LINEのPostbackデータとのマッピングも担う。
 */
@Getter
@AllArgsConstructor
public enum TimePeriod {

    MORNING("morning", "🌅 朝"),
    NOON("noon", "☀️ 昼"),
    EVENING("evening", "🌆 夕方"),
    NIGHT("night", "🌙 夜・深夜");

    // LINEのpostback data (timezone=xxx) で送られてくる値
    private final String code;
    
    // 画面表示やログ出力用のラベル
    private final String label;

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