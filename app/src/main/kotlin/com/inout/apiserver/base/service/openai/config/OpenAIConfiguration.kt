package com.inout.apiserver.base.service.openai.config

import com.aallam.openai.client.OpenAI
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
@EnableConfigurationProperties(OpenAIProperties::class)
class OpenAIConfiguration {
    @Bean
    fun openAI(
        openAIProperties: OpenAIProperties
    ): OpenAI {
        // https://github.com/aallam/openai-kotlin/blob/main/guides/GettingStarted.md
        return OpenAI(
            token = openAIProperties.token,
            organization = openAIProperties.organization
        )
    }
}
