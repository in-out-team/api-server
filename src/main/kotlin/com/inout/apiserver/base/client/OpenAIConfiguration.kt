package com.inout.apiserver.base.client

import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Configuration

@Configuration
@EnableConfigurationProperties(OpenAIProperties::class)
class OpenAIConfiguration
