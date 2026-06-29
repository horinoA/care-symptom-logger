package tech.doshikawa.carerecord.application.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import tech.doshikawa.carerecord.domain.dto.CareRecordExportDto;
import tech.doshikawa.carerecord.domain.repository.CareRecordRepository;
import tech.doshikawa.carerecord.infrastructure.csv.CsvGenerator;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.List;

/**
 * 介護記録のCSVエクスポートに関するユースケースを担うアプリケーションサービス
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CareRecordExportService {

    private final CareRecordRepository careRecordRepository;
    private final CsvGenerator csvGenerator;

    /**
     * 指定期間の介護記録を抽出し、CSV形式の文字列を返す
     *
     * @param userId 対象ユーザーID
     * @param startDate 開始日 (指定日の 00:00:00 を起点とする)
     * @param endDate 終了日 (指定日の翌日の 00:00:00 を終点とする)
     * @return CSV形式の文字列
     */
    public String exportCareRecordsAsCsv(String userId, LocalDate startDate, LocalDate endDate) {
        log.info("CSVエクスポートを開始します。対象期間: {} 〜 {}", startDate, endDate);

        // 期間の妥当性チェック
        if (endDate.isBefore(startDate)) {
            throw new IllegalArgumentException("終了日は開始日以降を指定してください。");
        }
        
        // 業務要件：「直近3ヶ月分」のみ出力可能とする
        LocalDate threeMonthsAgo = LocalDate.now().minusMonths(3);
        if (startDate.isBefore(threeMonthsAgo)) {
            log.warn("直近3ヶ月より古いデータが要求されました。 startDate: {}, 制限: {}", startDate, threeMonthsAgo);
            throw new IllegalArgumentException("エクスポート可能なデータは直近3ヶ月以内のみです。");
        }
        
        // （安全のため、期間自体が3ヶ月を超えていないかもチェック）
        if (startDate.plusMonths(3).isBefore(endDate)) {
            log.warn("指定された期間が3ヶ月を超えています。 startDate: {}, endDate: {}", startDate, endDate);
            throw new IllegalArgumentException("一度に指定できる期間は最大3ヶ月間です。");
        }

        // LocalDate を OffsetDateTime (日本時間) に変換
        // 終了日は「指定日の翌日の0時0分」にして < で比較することで、指定日の23:59:59までをカバーする
        ZoneId zone = ZoneId.of("Asia/Tokyo");
        OffsetDateTime startDateTime = startDate.atStartOfDay(zone).toOffsetDateTime();
        OffsetDateTime endDateTime = endDate.plusDays(1).atStartOfDay(zone).toOffsetDateTime();

        // ドメイン（Repository）へデータの抽出を指示
        List<CareRecordExportDto> data = careRecordRepository.findExportDataByDateRange(userId, startDateTime, endDateTime);
        log.info("エクスポート対象の記録を抽出しました。件数: {}件", data.size());

        // インフラストラクチャ（CsvGenerator）へCSV文字列の生成を指示
        String csvString = csvGenerator.generate(data);
        log.info("CSVデータの生成が完了しました。");

        return csvString;
    }
}
