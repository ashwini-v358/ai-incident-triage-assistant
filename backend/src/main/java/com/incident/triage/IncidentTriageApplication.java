package com.incident.triage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.core.env.Environment;

import java.util.Arrays;

@SpringBootApplication
public class IncidentTriageApplication {

    private static final Logger log = LoggerFactory.getLogger(IncidentTriageApplication.class);

    public static void main(String[] args) {
        SpringApplication app = new SpringApplication(IncidentTriageApplication.class);

        // Validate required database configuration on startup before DataSource initialization
        app.addInitializers(context -> {
            Environment env = context.getEnvironment();

            // Skip check when running under test profile (e.g. H2 in-memory DB)
            boolean isTestProfile = Arrays.asList(env.getActiveProfiles()).contains("test");
            if (isTestProfile) {
                return;
            }

            String url = env.getProperty("spring.datasource.url");
            String username = env.getProperty("spring.datasource.username");
            String password = env.getProperty("spring.datasource.password");

            // If connecting to PostgreSQL, ensure credentials are provided
            if (url != null && url.contains("postgresql")) {
                if (username == null || username.trim().isEmpty()) {
                    throw new IllegalStateException(
                            "\n========================================================================================\n" +
                            "CONFIGURATION ERROR: Missing PostgreSQL database username!\n" +
                            "Please set the 'SPRING_DATASOURCE_USERNAME' environment variable.\n" +
                            "========================================================================================"
                    );
                }

                if (password == null || password.trim().isEmpty()) {
                    throw new IllegalStateException(
                            "\n========================================================================================\n" +
                            "CONFIGURATION ERROR: Missing PostgreSQL database password!\n" +
                            "The 'SPRING_DATASOURCE_PASSWORD' environment variable is required to connect to PostgreSQL.\n" +
                            "Please set the 'SPRING_DATASOURCE_PASSWORD' environment variable before starting the application.\n" +
                            "Refer to .env.example for configuration details.\n" +
                            "========================================================================================"
                    );
                }
            }
        });

        app.run(args);
    }
}
