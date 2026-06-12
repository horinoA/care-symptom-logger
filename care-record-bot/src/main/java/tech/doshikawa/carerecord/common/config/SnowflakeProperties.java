package tech.doshikawa.carerecord.common.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import lombok.Data;

@Data
@Component
@ConfigurationProperties(prefix = "snowflake")
public class SnowflakeProperties {
    private Long nodeId = 1L; // デフォルト値
    private Long epoch = 1704067200000L; // 2024年01月01日 00:00:00 (UTC)
    private Long nodeIdBits = 10L;
    private Long sequenceBits = 12L;
}
