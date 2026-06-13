package tech.doshikawa.carerecord.domain.repository;

import org.springframework.data.repository.CrudRepository;
import tech.doshikawa.carerecord.domain.entity.PeopleCare;

/**
 * 登場人物マスタへのアクセスを提供するRepository
 */
public interface PeopleCareRepository extends CrudRepository<PeopleCare, Integer> {
}
