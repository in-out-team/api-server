package com.inout.apiserver.infrastructure.db.word

import com.inout.apiserver.base.alias.UserSentenceFeedbackId
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface UserSentenceFeedbackRepository : JpaRepository<UserSentenceFeedback, UserSentenceFeedbackId> {
    fun findAllByUserSentenceId(userSentenceId: Long): List<UserSentenceFeedback>
}
