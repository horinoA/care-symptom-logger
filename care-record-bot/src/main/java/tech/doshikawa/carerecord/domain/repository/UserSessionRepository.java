package tech.doshikawa.carerecord.domain.repository;

import org.springframework.data.repository.CrudRepository;
import tech.doshikawa.carerecord.domain.entity.UserSession;

public interface UserSessionRepository extends CrudRepository<UserSession, String> {
}
