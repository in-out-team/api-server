package com.inout.apiserver.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component

@Component
@ConfigurationProperties(prefix = "app")
data class AppConfig(
    val jobrunr: Jobrunr = Jobrunr(),
) {
    data class Jobrunr(
        var uri: String = "",
    )
}
