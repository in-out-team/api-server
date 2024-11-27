package com.inout.apiserver.application.word

import com.inout.apiserver.domain.user.User
import com.inout.apiserver.domain.word.WordService
import com.inout.apiserver.error.NotFoundException
import org.springframework.stereotype.Component

@Component
class UnselectSentenceApplication(
    private val wordService: WordService,
) {
    fun run(
        sentenceId: Long,
        user: User,
    ) {
        wordService.getUserSentenceBy(user.id, sentenceId)?.let {
            wordService.deleteUserSentence(it)
        } ?: throw NotFoundException(message = "UserSentence not found", code = "SENTENCE_3")
    }
}
