package com.inout.apiserver.base.client

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "openai")
data class OpenAIProperties(
    var token: String,
    var organization: String,
)