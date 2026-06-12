package com.caremate.lifeguardian.report.service;

import com.caremate.lifeguardian.common.exception.BaseException;
import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;

@Service
public class ReportPdfServiceImpl {

    private final ResourceLoader resourceLoader;
    private final String fontLocation;

    public ReportPdfServiceImpl(
            ResourceLoader resourceLoader,
            @Value("${app.report.pdf.font-location:}") String fontLocation
    ) {
        this.resourceLoader = resourceLoader;
        this.fontLocation = fontLocation;
    }

    public byte[] convertToPdf(String html) {
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            PdfRendererBuilder builder = new PdfRendererBuilder();
            builder.useFastMode();
            builder.withHtmlContent(html, null);

            if (StringUtils.hasText(fontLocation)) {
                Resource font = resourceLoader.getResource(fontLocation);
                if (!font.exists()) {
                    throw new BaseException(500, "PDF 폰트 파일을 찾을 수 없습니다.");
                }
                builder.useFont(() -> {
                    try {
                        return font.getInputStream();
                    } catch (IOException e) {
                        throw new UncheckedIOException(e);
                    }
                }, "ReportFont");
            }

            builder.toStream(outputStream);
            builder.run();
            return outputStream.toByteArray();
        } catch (BaseException e) {
            throw e;
        } catch (Exception e) {
            throw new BaseException(500, "리포트 PDF 변환에 실패했습니다.");
        }
    }
}
