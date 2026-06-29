package tech.doshikawa.carerecord.domain.repository;

import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import tech.doshikawa.carerecord.domain.dto.CareRecordExportDto;
import tech.doshikawa.carerecord.domain.entity.CareRecord;

import java.time.OffsetDateTime;
import java.util.List;

/**
 * 介護記録(event.care_records)の永続化を担当するRepository
 */
public interface CareRecordRepository extends CrudRepository<CareRecord, Long> {

    @Query("""
            SELECT
              TO_CHAR(r.onset_at AT TIME ZONE 'Asia/Tokyo', 'YYYY-MM-DD') AS onset_date
              , r.timezone AS onset_timezone
              , ROUND(EXTRACT(EPOCH FROM (r.remitted_at - r.onset_at))/60) AS duration_minutes
              , r.duration AS duration
              , p1.relationship AS target_relationship
              , p2.relationship AS to_who_relationship
              , sc.symptom_category_name AS symptom_category_name
              , sy.symptom_name AS symptom_name
              , r.memo AS memo
              , TO_CHAR(r.created_at AT TIME ZONE 'Asia/Tokyo', 'YYYY-MM-DD HH24:MI:SS') AS recorded_at
            FROM event.care_records r
            INNER JOIN event.care_record_details d 
              ON r.id = d.care_records_id
            INNER JOIN core.symptom_name sy 
              ON sy.id = d.symptom_id
            INNER JOIN core.symptom_category_name sc 
              ON sc.id = sy.symptom_category_id
            INNER JOIN core.people_care p1 
              ON p1.id = r.target_id
            LEFT JOIN core.people_care p2 
              ON p2.id = r.to_who_id
            WHERE
              r.user_id = :userId
              AND r.is_delete = FALSE
              AND r.onset_at >= :startDate
              AND r.onset_at < :endDate
            ORDER BY r.onset_at ASC, r.id ASC
            """)
    List<CareRecordExportDto> findExportDataByDateRange(
            @Param("userId") String userId,
            @Param("startDate") OffsetDateTime startDate,
            @Param("endDate") OffsetDateTime endDate
    );
}
