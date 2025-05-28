package com.inout.apiserver.infrastructure.s3

import com.amazonaws.HttpMethod
import com.amazonaws.services.s3.AmazonS3
import com.amazonaws.services.s3.model.CannedAccessControlList
import com.amazonaws.services.s3.model.GeneratePresignedUrlRequest
import com.amazonaws.services.s3.model.PutObjectRequest
import com.inout.apiserver.base.enums.AiVoiceType
import com.inout.apiserver.base.enums.LanguageType
import com.inout.apiserver.infrastructure.blobstore.BlobStoreService
import com.inout.apiserver.infrastructure.blobstore.BlobStoreService.Companion.AUDIO_PREFIX
import com.inout.apiserver.infrastructure.blobstore.BlobStoreService.Companion.PRE_SIGNED_URL_EXPIRATION
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.io.InputStream
import java.util.Date
import java.util.UUID

@Service
class S3Service(
    private val amazonS3: AmazonS3,
    @Value("\${cloud.aws.s3.bucket}")
    private val bucket: String,
) : BlobStoreService {
    override fun uploadAudio(
        inputStream: InputStream,
        language: LanguageType,
        voiceType: AiVoiceType,
        tableName: String,
        content: String,
    ): String {
        val directory = getAudioDirectory(language, tableName, content, voiceType)
        /**
         * TODO:
         *  - consider what to do with re-writes
         */
        amazonS3.putObject(
            PutObjectRequest(
                bucket,
                directory,
                inputStream,
                null,
            ).withCannedAcl(CannedAccessControlList.PublicRead),
        )

        return directory
    }

    override fun getAudioUrl(directory: String): String {
        val expiration = Date(System.currentTimeMillis() + PRE_SIGNED_URL_EXPIRATION)
        val generatePreSignedUrlRequest =
            GeneratePresignedUrlRequest(bucket, directory)
                .withMethod(HttpMethod.GET)
                .withExpiration(expiration)
        return amazonS3.generatePresignedUrl(generatePreSignedUrlRequest).toString()
    }

    override fun getAudioDirectory(
        language: LanguageType,
        tableName: String,
        content: String,
        voiceType: AiVoiceType,
    ) = AUDIO_PREFIX +
        "${language.name.lowercase()}/" +
        "${tableName}_${UUID.randomUUID()}.${voiceType.name.lowercase()}.mp3"
}
