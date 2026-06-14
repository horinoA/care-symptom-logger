package tech.doshikawa.carerecord.domain.type;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * PostgreSQLのJSONBカラムへのマッピングを安全に行うためのラッパークラス
 */
@Getter
@AllArgsConstructor
public class JsonData {
    private final String value;
    
    public static JsonData of(String value) {
        return new JsonData(value);
    }
}
