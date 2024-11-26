package com.inout.apiserver.interfaces.web.v1.response

import com.inout.apiserver.domain.word.UserSentence

data class UserSentenceResponse(
    val id: Long,
    val userId: Long,
    val wordDefinitionId: Long,
    val sentenceId: Long,
) {
    companion object {
        fun of(userSentence: UserSentence): UserSentenceResponse =
            UserSentenceResponse(
                id = userSentence.id,
                userId = userSentence.userId,
                wordDefinitionId = userSentence.wordDefinitionId,
                sentenceId = userSentence.sentenceId,
            )
    }
}
