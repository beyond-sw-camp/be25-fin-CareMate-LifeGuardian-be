package com.caremate.lifeguardian.report.service;

import com.caremate.lifeguardian.common.exception.BaseException;
import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Locale;
import java.util.Map;

/**
 * 리포트 템플릿을 렌더링하고 PDF 바이트 배열로 변환한다.
 */
@Service
public class ReportDocumentService {

    private final TemplateEngine templateEngine;
    private final ResourceLoader resourceLoader;
    private final String fontLocation;

    public ReportDocumentService(
            TemplateEngine templateEngine,
            ResourceLoader resourceLoader,
            @Value("${app.report.pdf.font-location:}") String fontLocation
    ) {
        this.templateEngine = templateEngine;
        this.resourceLoader = resourceLoader;
        this.fontLocation = fontLocation;
    }

    public byte[] renderPdf(String templateName, Map<String, Object> variables) {
        String html = renderHtml(templateName, variables);
        return convertToPdf(html);
    }

    private String renderHtml(String templateName, Map<String, Object> variables) {
        Context context = new Context(Locale.KOREAN);
        context.setVariables(variables);
        return templateEngine.process("reports/" + templateName, context);
    }

    private byte[] convertToPdf(String html) {
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
