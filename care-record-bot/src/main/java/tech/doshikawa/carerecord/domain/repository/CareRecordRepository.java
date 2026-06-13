package tech.doshikawa.carerecord.domain.repository;

import org.springframework.data.repository.CrudRepository;
import tech.doshikawa.carerecord.domain.entity.CareRecord;

/**
 * 介護記録(event.care_records)の永続化を担当するRepository
 */
public interface CareRecordRepository extends CrudRepository<CareRecord, Long> {
}
