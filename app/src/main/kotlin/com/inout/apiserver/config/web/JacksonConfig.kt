package com.inout.apiserver.config.web

import com.inout.apiserver.base.serializer.PreSignedUrlSerializer
import com.inout.apiserver.infrastructure.blobstore.BlobStoreService
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class JacksonConfig(
    private val blobStoreService: BlobStoreService,
) {
    @Bean
    fun preSignedUrlSerializer() = PreSignedUrlSerializer(blobStoreService)
}
