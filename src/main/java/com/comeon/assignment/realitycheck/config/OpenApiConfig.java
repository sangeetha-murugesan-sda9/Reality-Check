package com.comeon.assignment.realitycheck.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

        @Bean
    public OpenAPI realityCheckOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("Reality Check API")
                        .version("1.0.0")
                        .description("""
                                Responsible-gaming reality-check sessions.

                                While a player is in a gaming session, this service tracks how long they've
                                been playing and their net win/loss, and periodically prompts them to check
                                in on their own wellbeing. All timestamps in responses are formatted in the
                                player's own timezone (the timezone stored on their player record)."""));
    }
}
