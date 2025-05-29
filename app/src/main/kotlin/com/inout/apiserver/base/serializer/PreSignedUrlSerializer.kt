package com.inout.apiserver.base.serializer

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.JsonSerializer
import com.fasterxml.jackson.databind.SerializerProvider
import com.inout.apiserver.infrastructure.blobstore.BlobStoreService

class PreSignedUrlSerializer(
    private val blobStoreService: BlobStoreService,
) : JsonSerializer<String?>() {
    override fun serialize(
        value: String?,
        gen: JsonGenerator,
        serializers: SerializerProvider,
    ) {
        val url = value?.let { blobStoreService.getAudioUrl(it) }
        gen.writeString(url)
    }
}
