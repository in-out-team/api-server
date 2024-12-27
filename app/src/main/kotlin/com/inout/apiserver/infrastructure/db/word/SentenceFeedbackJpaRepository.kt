package com.inout.apiserver.infrastructure.db.word

import org.springframework.data.jpa.repository.JpaRepository

interface SentenceFeedbackJpaRepository : JpaRepository<SentenceFeedbackEntity, Long>
