package com.caremate.lifeguardian.report.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.util.Locale;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ReportTemplateService {

    private final TemplateEngine templateEngine;

    public String render(String templateName, Map<String, Object> variables) {
        Context context = new Context(Locale.KOREAN);
        context.setVariables(variables);
        return templateEngine.process("reports/" + templateName, context);
    }
}
