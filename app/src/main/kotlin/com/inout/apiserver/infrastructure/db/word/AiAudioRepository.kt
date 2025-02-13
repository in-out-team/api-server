package com.inout.apiserver.infrastructure.db.word

import com.inout.apiserver.base.alias.AiAudioId
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface AiAudioRepository : JpaRepository<AiAudio, AiAudioId>
