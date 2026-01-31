package com.moviebooking.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * OpenAPI/Swagger configuration
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Movie Booking Platform API")
                        .version("1.0.0")
                        .description("Online Movie Ticket Booking Platform - B2B and B2C APIs\n\n" +
                                "## Features\n" +
                                "- Browse theatres running shows for a movie\n" +
                                "- View show timings and seat availability\n" +
                                "- Book tickets with seat selection\n" +
                                "- Automatic offer application:\n" +
                                "  - 50% off on 3rd ticket (when booking 3+ tickets)\n" +
                                "  - 20% off for afternoon shows (12 PM - 5 PM)")
                        .contact(new Contact()
                                .name("Movie Booking Support")
                                .email("support@moviebooking.com"))
                        .license(new License()
                                .name("Apache 2.0")
                                .url("https://www.apache.org/licenses/LICENSE-2.0")))
                .servers(List.of(
                        new Server()
                                .url("http://localhost:8080")
                                .description("Development Server")
                ));
    }
}
