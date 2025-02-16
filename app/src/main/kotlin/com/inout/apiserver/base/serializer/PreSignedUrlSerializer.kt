package com.inout.apiserver.base.serializer

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.JsonSerializer
import com.fasterxml.jackson.databind.SerializerProvider
import com.inout.apiserver.infrastructure.s3.S3Service

class PreSignedUrlSerializer(
    private val s3Service: S3Service,
) : JsonSerializer<String?>() {
    override fun serialize(
        value: String?,
        gen: JsonGenerator,
        serializers: SerializerProvider,
    ) {
        val url = value?.let { s3Service.getAudioUrl(it) }
        gen.writeString(url)
    }
}
