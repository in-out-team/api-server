package com.inout.apiserver.infrastructure.db.word

import com.inout.apiserver.base.alias.AiAudioId
import com.inout.apiserver.base.enums.LanguageType
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface AiAudioRepository : JpaRepository<AiAudio, AiAudioId> {
    // FIXME: potential bug when multiple records are found
    fun findByLanguageAndContent(
        language: LanguageType,
        content: String,
    ): AiAudio?
}
