package com.inout.apiserver.application.word

import com.inout.apiserver.domain.user.User
import com.inout.apiserver.domain.word.UserSentence
import com.inout.apiserver.domain.word.UserSentenceCreateObject
import com.inout.apiserver.domain.word.WordService
import com.inout.apiserver.error.NotFoundException
import org.springframework.stereotype.Component

@Component
class SelectSentenceApplication(
    private val wordService: WordService,
) {
    fun run(
        sentenceId: Long,
        user: User,
    ): UserSentence {
        val sentence =
            wordService.getSentenceById(sentenceId) ?: throw NotFoundException(
                message = "Sentence Not Found",
                code = "WORD_5",
            )

        return wordService.createUserSentence(
            UserSentenceCreateObject(
                userId = user.id,
                wordDefinitionId = sentence.wordDefinitionId,
                sentenceId = sentence.id,
            ),
        )
    }
}
