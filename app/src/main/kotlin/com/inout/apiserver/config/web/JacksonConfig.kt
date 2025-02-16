package com.inout.apiserver.config.web

import com.inout.apiserver.base.serializer.PreSignedUrlSerializer
import com.inout.apiserver.infrastructure.s3.S3Service
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class JacksonConfig(
    private val s3Service: S3Service,
) {
    @Bean
    fun preSignedUrlSerializer() = PreSignedUrlSerializer(s3Service)
}
