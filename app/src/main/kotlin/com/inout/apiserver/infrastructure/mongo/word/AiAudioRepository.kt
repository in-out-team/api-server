package com.inout.apiserver.infrastructure.mongo.word

import com.inout.apiserver.base.enums.AiVoiceType
import com.inout.apiserver.base.enums.LanguageType
import org.bson.types.ObjectId
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.stereotype.Repository

@Repository
interface AiAudioRepository : MongoRepository<AiAudio, ObjectId> {
    fun findByLanguageAndVoiceTypeAndContent(
        language: LanguageType,
        voiceType: AiVoiceType,
        content: String,
    ): AiAudio?
}
