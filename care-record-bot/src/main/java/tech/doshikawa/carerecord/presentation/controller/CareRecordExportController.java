package tech.doshikawa.carerecord.presentation.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import tech.doshikawa.carerecord.application.service.CareRecordExportService;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;

/**
 * 介護記録データの外部エクスポートを処理するコントローラー (最前線：城門の守備隊)
 */
@RestController
@RequestMapping("/api/v1/records/export")
@RequiredArgsConstructor
@Slf4j
public class CareRecordExportController {

    private final CareRecordExportService careRecordExportService;

    /**
     * 指定期間の介護記録をCSV形式でダウンロードする
     *
     * @param userId 対象ユーザーのLINEユーザーID（将来的な認証対応も考慮）
     * @param startDate 抽出開始日 (yyyy-MM-dd)
     * @param endDate 抽出終了日 (yyyy-MM-dd)
     * @return CSVファイルデータ
     */
    @GetMapping
    public ResponseEntity<byte[]> exportCsv(
            @RequestParam("userId") String userId,
            @RequestParam("startDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam("endDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        
        log.info("CSVエクスポート要求を受信しました。userId: {}, startDate: {}, endDate: {}", userId, startDate, endDate);

        try {
            // アプリケーション層へCSV文字列の生成を委譲
            String csvData = careRecordExportService.exportCareRecordsAsCsv(userId, startDate, endDate);

            // BOM付きUTF-8にして、Excel等で文字化けしないようにする
            byte[] bom = new byte[] { (byte) 0xEF, (byte) 0xBB, (byte) 0xBF };
            byte[] csvBytes = csvData.getBytes(StandardCharsets.UTF_8);
            byte[] outputBytes = new byte[bom.length + csvBytes.length];
            System.arraycopy(bom, 0, outputBytes, 0, bom.length);
            System.arraycopy(csvBytes, 0, outputBytes, bom.length, csvBytes.length);

            // ダウンロードさせるためのHTTPヘッダの構築 (魔法の勅命)
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(new MediaType("text", "csv", StandardCharsets.UTF_8));
            headers.setContentDispositionFormData("attachment", "care_records_" + startDate + "_to_" + endDate + ".csv");

            log.info("CSVエクスポート応答を返却します。");
            return ResponseEntity.ok()
                    .headers(headers)
                    .body(outputBytes);

        } catch (Exception e) {
            log.error("CSVエクスポート中に予期せぬエラーが発生しました。", e);
            // 実際は適切なエラーレスポンス(JSON等)を返すか、例外ハンドラに任せるべきですが、
            // シンプルに500エラーを返却します。
            return ResponseEntity.internalServerError().build();
        }
    }
}
