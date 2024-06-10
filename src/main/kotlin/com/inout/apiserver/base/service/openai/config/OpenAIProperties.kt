package com.inout.apiserver.base.service.openai.config

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "openai")
data class OpenAIProperties(
    var token: String,
    var organization: String,
)