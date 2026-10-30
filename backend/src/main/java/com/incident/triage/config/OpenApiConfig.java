package com.incident.triage.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("AI-Powered Incident Triage & Root-Cause Assistant API")
                        .version("1.0.0")
                        .description("Automated log parsing, Java exception extraction, rule-based classification, and Gemini-powered probable root-cause analysis.")
                        .contact(new Contact()
                                .name("DevOps & SRE Engineering Team")
                                .email("sre-team@incident-triage.local"))
                        .license(new License().name("Apache 2.0")));
    }
}

