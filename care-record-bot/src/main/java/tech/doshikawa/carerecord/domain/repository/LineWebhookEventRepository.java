package tech.doshikawa.carerecord.domain.repository;

import org.springframework.data.repository.CrudRepository;
import tech.doshikawa.carerecord.domain.entity.LineWebhookEvent;

public interface LineWebhookEventRepository extends CrudRepository<LineWebhookEvent, String> {
}
