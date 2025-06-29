package app.accommodationbookingservice.config;

import io.swagger.v3.oas.models.info.Info;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {
    @Bean
    public GroupedOpenApi publicApi() {
        return GroupedOpenApi.builder()
                .group("openapi")
                .pathsToMatch("/**")
                .build();
    }

    @Bean
    public Info apiInfo() {
        return new Info().title("Accommodation Booking Service API")
                .description("API documentation for booking system")
                .version("v1");
    }
}
