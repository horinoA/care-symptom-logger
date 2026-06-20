package tech.doshikawa.carerecord.domain.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.domain.Persistable;
import org.springframework.data.relational.core.mapping.Table;

import java.time.OffsetDateTime;
import java.util.UUID;

@Table(name = "line_webhook_events", schema = "session")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LineWebhookEvent implements Persistable<String> {

    @Id
    private String eventId;
    private String userId;
    private UUID sessionId;
    private String eventType;
    private OffsetDateTime lineTimestamp;
    private String status;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;

    @Transient
    @Builder.Default
    private boolean isNew = true;

    @Override
    public String getId() {
        return eventId;
    }

    @Override
    public boolean isNew() {
        return isNew;
    }
}
