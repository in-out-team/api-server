package com.inout.apiserver.infrastructure.db.word

import com.inout.apiserver.domain.word.AiAudio
import org.springframework.stereotype.Repository

@Repository
class AiAudioRepository(
    private val aiAudioJpaRepository: AiAudioJpaRepository,
) {
    fun save(aiAudio: AiAudioEntity) = aiAudioJpaRepository.save(aiAudio).toDomain()

    fun findById(id: Long): AiAudio? = aiAudioJpaRepository.findById(id).map { it.toDomain() }.orElse(null)
}
