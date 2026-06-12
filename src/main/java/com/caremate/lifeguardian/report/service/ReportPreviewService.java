package com.caremate.lifeguardian.report.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ReportPreviewService {

    private final ReportTemplateService reportTemplateService;
    private final ReportPdfServiceImpl reportPdfService;
    private final ReportStorageServiceImpl reportStorageService;

    public String createAndUploadSample() {
        Map<String, Object> variables = Map.of(
                "reportYear", LocalDate.now().getYear(),
                "generatedAt", LocalDate.now(),
                "customerName", "김민준",
                "summary", "현재 연령대의 주요 질병 통계와 웹폼 응답을 바탕으로 생성한 샘플 분석입니다."
        );

        String html = reportTemplateService.render("growth-report", variables);
        byte[] pdfBytes = reportPdfService.convertToPdf(html);
        return reportStorageService.uploadPdf(pdfBytes, 1000001L);
    }
}
