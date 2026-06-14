package tech.doshikawa.carerecord.infrastructure.config;

import org.postgresql.util.PGobject;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.ReadingConverter;
import org.springframework.data.convert.WritingConverter;
import org.springframework.data.jdbc.core.convert.JdbcCustomConversions;
import org.springframework.data.jdbc.repository.config.AbstractJdbcConfiguration;
import tech.doshikawa.carerecord.domain.type.JsonData;

import java.sql.SQLException;
import java.util.Arrays;

/**
 * Spring Data JDBCのカスタム設定
 * PostgreSQLのJSONBカラムとのマッピング等を定義します。
 */
@Configuration
public class JdbcConfig extends AbstractJdbcConfiguration {

    @Override
    public JdbcCustomConversions jdbcCustomConversions() {
        return new JdbcCustomConversions(Arrays.asList(
            new JsonDataToPGobjectConverter(),
            new PGobjectToJsonDataConverter(),
            new TimestampToOffsetDateTimeConverter()
        ));
    }

    @WritingConverter
    static class JsonDataToPGobjectConverter implements Converter<JsonData, PGobject> {
        @Override
        public PGobject convert(JsonData source) {
            PGobject pgObject = new PGobject();
            pgObject.setType("jsonb");
            try {
                pgObject.setValue(source.getValue());
            } catch (SQLException e) {
                throw new RuntimeException("Failed to convert JsonData to PGobject", e);
            }
            return pgObject;
        }
    }

    @ReadingConverter
    static class PGobjectToJsonDataConverter implements Converter<PGobject, JsonData> {
        @Override
        public JsonData convert(PGobject source) {
            return JsonData.of(source.getValue());
        }
    }

    @ReadingConverter
    static class TimestampToOffsetDateTimeConverter implements Converter<java.sql.Timestamp, java.time.OffsetDateTime> {
        @Override
        public java.time.OffsetDateTime convert(java.sql.Timestamp source) {
            return source.toInstant().atOffset(java.time.ZoneOffset.ofHours(9));
        }
    }
}
