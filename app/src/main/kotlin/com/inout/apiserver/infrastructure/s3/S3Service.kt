package com.inout.apiserver.infrastructure.s3

import com.amazonaws.HttpMethod
import com.amazonaws.services.s3.AmazonS3
import com.amazonaws.services.s3.model.CannedAccessControlList
import com.amazonaws.services.s3.model.GeneratePresignedUrlRequest
import com.amazonaws.services.s3.model.PutObjectRequest
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import java.util.Date

@Service
class S3Service(
    private val amazonS3: AmazonS3,
    @Value("\${cloud.aws.s3.bucket}")
    private val bucket: String,
) {
    companion object {
        /**
         * audio directory rule:
         * - static/audio/{{language}}/{{rule}}.{{audio speaker}}.mp3
         * - rule?:
         *   - table name + id + md5 hash of the audio text
         *   - ex: ai_speeches_1_{{md5 hash of the audio text}}.alloy.mp3
         */
        private const val AUDIO_PREFIX = "static/audio/"
        private const val PRE_SIGNED_URL_EXPIRATION = 3600 * 1000 // 1 hour
    }

    /**
     * @param file audio file
     * @return audio file url
     */
    fun saveAudioFile(file: MultipartFile): String {
        val directory = AUDIO_PREFIX + "english/test.alloy.mp3"
        amazonS3.putObject(
            PutObjectRequest(
                bucket,
                directory,
                file.inputStream,
                null,
            ).withCannedAcl(CannedAccessControlList.PublicRead),
        )
        /**
         * TODO:
         *  - consider what to do with re-writes
         */
        val expiration = Date(System.currentTimeMillis() + PRE_SIGNED_URL_EXPIRATION)
        val generatePreSignedUrlRequest =
            GeneratePresignedUrlRequest(bucket, directory)
                .withMethod(HttpMethod.GET)
                .withExpiration(expiration)
        return amazonS3.generatePresignedUrl(generatePreSignedUrlRequest).toString()
    }
}
