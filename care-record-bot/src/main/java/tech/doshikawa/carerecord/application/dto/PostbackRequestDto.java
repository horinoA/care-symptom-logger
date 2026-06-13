package tech.doshikawa.carerecord.application.dto;

import lombok.Builder;
import lombok.Data;
import tech.doshikawa.carerecord.domain.type.TimePeriod;
import tech.doshikawa.carerecord.domain.type.TimeSpend;

import java.util.HashMap;
import java.util.Map;

/**
 * LINEのPostbackデータ（例: action=select_timezone&timezone=morning）
 * を安全に扱うためのDTO。
 */
@Data
@Builder
public class PostbackRequestDto {

    // どのアクションか（例: "select_timezone"）
    private String action;

    // パラメータのキーと値のマップ
    @Builder.Default
    private Map<String, String> parameters = new HashMap<>();

    /**
     * data文字列からDTOを生成するファクトリメソッド
     */
    public static PostbackRequestDto parse(String data) {
        if (data == null || data.isBlank()) {
            return PostbackRequestDto.builder().build();
        }

        Map<String, String> params = new HashMap<>();
        String action = "";

        // "&" で分割し、さらに "=" でキーと値に分ける
        String[] pairs = data.split("&");
        for (String pair : pairs) {
            String[] keyValue = pair.split("=");
            if (keyValue.length == 2) {
                if ("action".equals(keyValue[0])) {
                    action = keyValue[1];
                } else {
                    params.put(keyValue[0], keyValue[1]);
                }
            }
        }

        return PostbackRequestDto.builder()
                .action(action)
                .parameters(params)
                .build();
    }

    /**
     * パラメータからTimePeriod（時間帯）のEnumを安全に取得する
     */
    public TimePeriod getTimePeriod() {
        String code = parameters.get("timezone");
        return code != null ? TimePeriod.fromCode(code) : null;
    }

    /**
     * パラメータからTimeSpend（所要時間）のEnumを安全に取得する
     */
    public TimeSpend getTimeSpend() {
        String id = parameters.get("timespendId");
        return id != null ? TimeSpend.fromId(id) : null;
    }
}