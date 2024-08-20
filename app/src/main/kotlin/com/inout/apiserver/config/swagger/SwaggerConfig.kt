package com.inout.apiserver.config.swagger

import io.swagger.v3.oas.models.Components
import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.info.Info
import io.swagger.v3.oas.models.security.SecurityRequirement
import io.swagger.v3.oas.models.security.SecurityScheme
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class SwaggerConfig {
    @Bean
    fun openAPI(): OpenAPI {
        return OpenAPI()
            .info(configurationInfo())
            .components(configurationComponents())
            .addSecurityItem(configurationSecurityItems())
    }

    private fun configurationInfo(): Info {
        return Info()
            .title("In-Out API")
            .description("Inout API")
            .version("1.0.0")
    }

    private fun configurationComponents(): Components {
        return Components()
            .addSecuritySchemes(
                "bearer-jwt",
                SecurityScheme()
                    .type(SecurityScheme.Type.HTTP)
                    .scheme("bearer")
                    .bearerFormat("JWT")
            )
    }

    private fun configurationSecurityItems(): SecurityRequirement {
        return SecurityRequirement()
            .addList("bearer-jwt")
    }
}