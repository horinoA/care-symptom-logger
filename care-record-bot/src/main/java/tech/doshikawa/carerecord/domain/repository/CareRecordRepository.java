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
              , CASE r.timezone 
                WHEN 'MORNING' THEN '朝'
                WHEN 'NOON' THEN '昼'
                WHEN 'EVENING' THEN '夕方'
                WHEN 'NIGHT' THEN '夜・深夜'
              ELSE '時間帯未入力'				   
              END
              AS onset_timezone
              , ROUND(EXTRACT(EPOCH FROM (r.remitted_at - r.onset_at))/60) AS duration_minutes
              , CASE r.duration 
                WHEN 'UP_TO_30_MIN' THEN '30分まで'
                WHEN 'AROUND_2_HOURS' THEN '２時間程度'
                WHEN 'HALF_DAY' THEN '半日'
                WHEN 'OVER_1_DAY' THEN '１日以上'
              ELSE '経過時間未入力'				   
              END
              AS duration
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

    @Query("""
            SELECT
              CONCAT(TO_CHAR(r.onset_at AT TIME ZONE 'Asia/Tokyo', 'YYYY-MM-DD') ,
                '_'
              , CASE r.timezone
                WHEN 'MORNING' THEN '朝'
                WHEN 'NOON' THEN '昼'
                WHEN 'EVENING' THEN '夕方'
                WHEN 'NIGHT' THEN '夜・深夜'
              ELSE '発症時間帯未入力'				   
                END
              ) AS onset_timezone
              , CASE r.duration 
                WHEN 'UP_TO_30_MIN' THEN '30分まで'
                WHEN 'AROUND_2_HOURS' THEN '２時間程度'
                WHEN 'HALF_DAY' THEN '半日'
                WHEN 'OVER_1_DAY' THEN '１日以上'
              ELSE '持続時間未入力'				   
                END			  
              AS duration
              , p1.relationship AS target_relationship
              , STRING_AGG(sc.symptom_category_name,',') AS symptom_category_name
              , STRING_AGG(sy.symptom_name,',') AS symptom_name
              , r.memo AS memo
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
              AND r.onset_at <= :endDate
            GROUP BY r.onset_at,r.timezone,r.duration ,p1.relationship,r.memo
            ORDER BY r.onset_at DESC
            """)
    List<tech.doshikawa.carerecord.domain.dto.RecentSymptomDto> findRecentSymptomsByDateRange(
            @Param("userId") String userId,
            @Param("startDate") OffsetDateTime startDate,
            @Param("endDate") OffsetDateTime endDate
    );
}
