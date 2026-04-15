package dev.artisra.simplecrud.config

import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.info.Info
import io.swagger.v3.oas.models.info.License
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class OpenApiConfig {

    @Bean
    fun customOpenAPI(): OpenAPI {
        return OpenAPI()
            .info(
                Info()
                    .title("Simple CRUD API")
                    .version("1.0.0")
                    .description("Inventory and Reservation management system API")
                    .license(License().name("Apache 2.0").url("http://springdoc.org"))
            )
    }
}
