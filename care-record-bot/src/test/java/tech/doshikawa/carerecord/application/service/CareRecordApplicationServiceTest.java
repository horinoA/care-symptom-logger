package tech.doshikawa.carerecord.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import tech.doshikawa.carerecord.application.dto.CareRecordCreateCommand;
import tech.doshikawa.carerecord.common.exception.NotFoundMasterExceprion;
import tech.doshikawa.carerecord.common.util.SnowflakeIdGenerator;
import tech.doshikawa.carerecord.domain.entity.CareRecord;
import tech.doshikawa.carerecord.domain.repository.CareRecordRepository;
import tech.doshikawa.carerecord.domain.repository.PeopleCareRepository;
import tech.doshikawa.carerecord.domain.repository.SymptomNameRepository;
import tech.doshikawa.carerecord.domain.type.TimePeriod;
import tech.doshikawa.carerecord.domain.type.TimeSpend;

@ExtendWith(MockitoExtension.class)
class CareRecordApplicationServiceTest {

    @Mock
    private CareRecordRepository careRecordRepository;

    @Mock
    private PeopleCareRepository peopleCareRepository;

    @Mock
    private SymptomNameRepository symptomNameRepository;

    @Mock
    private SnowflakeIdGenerator idGenerator;

    @InjectMocks
    private CareRecordApplicationService target;

    @Test
    @DisplayName("正常系: 全てのマスタが存在する場合、親子データが正しく構築されRepositoryのsaveが呼ばれること")
    void createCareRecord_Success() {
        // Arrange
        CareRecordCreateCommand command = CareRecordCreateCommand.builder()
            .userId("user_123")
            .targetId(1)
            .toWhoId(2)
            .onsetAt(OffsetDateTime.now())
            .timezone(TimePeriod.MORNING)
            .duration(TimeSpend.UP_TO_30_MIN)
            .remittedAt(OffsetDateTime.now().plusHours(1))
            .memo("テストメモ")
            .symptomIds(List.of(10, 20))
            .build();

        // マスタ存在チェックのモック
        when(peopleCareRepository.existsById(1)).thenReturn(true);
        when(peopleCareRepository.existsById(2)).thenReturn(true);
        when(symptomNameRepository.existsById(10)).thenReturn(true);
        when(symptomNameRepository.existsById(20)).thenReturn(true);

        // ID採番のモック (1回目: 親レコードID, 2回目・3回目: 子レコードID)
        when(idGenerator.nextId()).thenReturn(100L, 101L, 102L);

        // Act
        target.createCareRecord(command);

        // Assert
        // Repositoryのsaveメソッドに渡された引数をキャプチャして検証
        ArgumentCaptor<CareRecord> captor = ArgumentCaptor.forClass(CareRecord.class);
        verify(careRecordRepository).save(captor.capture());

        CareRecord savedRecord = captor.getValue();
        
        // 1. 親データ（CareRecord）の検証
        assertThat(savedRecord.getId()).isEqualTo(100L);
        assertThat(savedRecord.getUserId()).isEqualTo("user_123");
        assertThat(savedRecord.getTargetId()).isEqualTo(1);
        assertThat(savedRecord.getToWhoId()).isEqualTo(2);
        assertThat(savedRecord.getMemo()).isEqualTo("テストメモ");
        assertThat(savedRecord.getIsDelete()).isFalse();
        assertThat(savedRecord.isNew()).isTrue();

        // 2. 子データ（CareRecordDetail）の検証
        assertThat(savedRecord.getDetails()).hasSize(2);
        assertThat(savedRecord.getDetails())
            .extracting("symptomId")
            .containsExactlyInAnyOrder(10, 20);
        
        // 子EntityのisNewがtrueであることも確認
        savedRecord.getDetails().forEach(detail -> 
            assertThat(detail.isNew()).isTrue()
        );
    }

    @Test
    @DisplayName("異常系: 対象者(targetId)のマスタが存在しない場合、例外がスローされること")
    void createCareRecord_TargetNotFound() {
        // Arrange
        CareRecordCreateCommand command = CareRecordCreateCommand.builder()
            .userId("user_123")
            .targetId(999) // 存在しないID
            .build();

        when(peopleCareRepository.existsById(999)).thenReturn(false);

        // Act & Assert
        assertThatThrownBy(() -> target.createCareRecord(command))
            .isInstanceOf(NotFoundMasterExceprion.class)
            .hasMessage("error.carerecord.target.notfound");
    }

    @Test
    @DisplayName("異常系: 症状(symptomId)のマスタが存在しない場合、例外がスローされること")
    void createCareRecord_SymptomNotFound() {
        // Arrange
        CareRecordCreateCommand command = CareRecordCreateCommand.builder()
            .userId("user_123")
            .targetId(1)
            .symptomIds(List.of(10, 999)) // 999が存在しない
            .build();

        when(peopleCareRepository.existsById(1)).thenReturn(true);
        when(symptomNameRepository.existsById(10)).thenReturn(true);
        when(symptomNameRepository.existsById(999)).thenReturn(false);

        // Act & Assert
        assertThatThrownBy(() -> target.createCareRecord(command))
            .isInstanceOf(NotFoundMasterExceprion.class)
            .hasMessage("error.carerecord.symptom.notfound");
    }
}
