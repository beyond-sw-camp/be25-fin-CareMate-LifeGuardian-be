package com.caremate.lifeguardian.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

	private static final String SECURITY_SCHEME_NAME = "Bearer Authentication";

	@Bean
	public OpenAPI openAPI() {
		// 1. API 기본 정보 설정
		Info info = new Info()
				.title("LifeGuardian API")
				.description("보험 영업 CRM 및 보험 추천 시스템 API 문서")
				.version("v1.0.0");

		// 2. JWT 보안 요구사항 및 스키마 정의
		SecurityRequirement securityRequirement = new SecurityRequirement().addList(SECURITY_SCHEME_NAME);

		Components components = new Components()
				.addSecuritySchemes(SECURITY_SCHEME_NAME, new SecurityScheme()
						.name("Authorization")
						.type(SecurityScheme.Type.HTTP)
						.scheme("bearer")
						.bearerFormat("JWT"));

		// 3. 하나로 조립하여 반환
		return new OpenAPI()
				.info(info)
				.addSecurityItem(securityRequirement)
				.components(components);
	}
}