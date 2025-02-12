package com.inout.apiserver.application.word

import com.inout.apiserver.base.alias.SentenceId
import com.inout.apiserver.base.enums.SentenceType
import com.inout.apiserver.domain.word.WordService
import com.inout.apiserver.error.NotFoundException
import com.inout.apiserver.infrastructure.db.user.User
import org.springframework.stereotype.Component

@Component
class UnselectReadingSentenceApplication(
    private val wordService: WordService,
) {
    data class Request(
        val sentenceId: SentenceId,
        val user: User,
    )

    fun run(request: Request) {
        wordService
            .getUserSentenceBy(request.user.id!!, request.sentenceId)
            ?.takeIf { userSentence ->
                wordService.getSentenceByIdAndType(
                    userSentence.sentenceId,
                    SentenceType.READING,
                ) != null
            }?.let { userSentence -> wordService.deleteUserSentence(userSentence) }
            ?: throw NotFoundException(message = "UserSentence not found", code = "SENTENCE_3")
    }
}
