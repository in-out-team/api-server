package com.inout.apiserver.base.service.openai.config

import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Configuration

@Configuration
@EnableConfigurationProperties(OpenAIProperties::class)
class OpenAIConfiguration
