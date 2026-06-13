package tech.doshikawa.carerecord.application.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tech.doshikawa.carerecord.application.dto.CareRecordCreateCommand;
import tech.doshikawa.carerecord.common.exception.NotFoundMasterExceprion;
import tech.doshikawa.carerecord.common.util.SnowflakeIdGenerator;
import tech.doshikawa.carerecord.domain.entity.CareRecord;
import tech.doshikawa.carerecord.domain.entity.CareRecordDetail;
import tech.doshikawa.carerecord.domain.repository.CareRecordRepository;
import tech.doshikawa.carerecord.domain.repository.PeopleCareRepository;
import tech.doshikawa.carerecord.domain.repository.SymptomNameRepository;

import java.time.OffsetDateTime;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 介護記録のユースケース（登録・更新・検索など）を担当するApplication Service
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CareRecordApplicationService {

    private final CareRecordRepository careRecordRepository;
    private final PeopleCareRepository peopleCareRepository;
    private final SymptomNameRepository symptomNameRepository;
    private final SnowflakeIdGenerator idGenerator;

    /**
     * 介護記録を新規登録します。
     * 内部でマスタの存在チェックを行い、問題なければ親・子テーブルへ一括保存します。
     */
    @Transactional
    public void createCareRecord(CareRecordCreateCommand command) {
        log.info("介護記録の登録処理を開始します。ユーザーID: {}", command.getUserId());

        // 1. マスタの存在チェック（対象者）
        if (!peopleCareRepository.existsById(command.getTargetId())) {
            log.warn("指定された対象者マスタが存在しません: {}", command.getTargetId());
            throw new NotFoundMasterExceprion("指定された対象者が見つかりません。");
        }

        // 誰に対して（toWhoId）が指定されている場合もチェック
        if (command.getToWhoId() != null && !peopleCareRepository.existsById(command.getToWhoId())) {
            log.warn("指定されたToWhoマスタが存在しません: {}", command.getToWhoId());
            throw new NotFoundMasterExceprion("指定された関連者が見つかりません。");
        }

        // 2. マスタの存在チェック（症状）
        for (Integer symptomId : command.getSymptomIds()) {
            if (!symptomNameRepository.existsById(symptomId)) {
                log.warn("指定された症状マスタが存在しません: {}", symptomId);
                throw new NotFoundMasterExceprion("指定された症状が見つかりません。");
            }
        }

        // 3. Entityの生成とID採番（SnowflakeId）
        Long recordId = idGenerator.nextId();
        
        // 子Entity（症状明細）のセットを生成
        Set<CareRecordDetail> details = command.getSymptomIds().stream()
                .map(symptomId -> CareRecordDetail.builder()
                        .id(idGenerator.nextId())
                        .symptomId(symptomId)
                        .isNew(true)
                        .build())
                .collect(Collectors.toSet());

        // 親Entityの生成
        CareRecord careRecord = CareRecord.builder()
                .id(recordId)
                .userId(command.getUserId())
                .targetId(command.getTargetId())
                .toWhoId(command.getToWhoId())
                .onsetAt(command.getOnsetAt())
                .timezone(command.getTimezone())
                .duration(command.getDuration())
                .remittedAt(command.getRemittedAt())
                .memo(command.getMemo())
                .isDelete(false)
                .createdAt(OffsetDateTime.now())
                .details(details)
                .isNew(true)
                .build();

        // 4. DBへ一括保存
        // Spring Data JDBCの @MappedCollection により、親(care_records)と子(care_record_details)へ
        // 自動的に2つのINSERT文が発行されます。
        careRecordRepository.save(careRecord);
        
        log.info("介護記録の保存が完了しました。レコードID: {}", recordId);
    }
}
