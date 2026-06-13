package tech.doshikawa.carerecord.domain.repository;

import org.springframework.data.repository.CrudRepository;
import tech.doshikawa.carerecord.domain.entity.SymptomName;

/**
 * 症状名マスタへのアクセスを提供するRepository
 */
public interface SymptomNameRepository extends CrudRepository<SymptomName, Integer> {
}
