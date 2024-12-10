package com.inout.apiserver.application.word

import com.inout.apiserver.base.enums.SentenceType
import com.inout.apiserver.domain.user.User
import com.inout.apiserver.domain.word.UserSentence
import com.inout.apiserver.domain.word.UserSentenceCreateObject
import com.inout.apiserver.domain.word.WordService
import com.inout.apiserver.error.BadRequestException
import com.inout.apiserver.error.NotFoundException
import org.springframework.stereotype.Component

@Component
class SelectReadingSentenceApplication(
    private val wordService: WordService,
) {
    companion object {
        private const val MAX_SENTENCES_COUNT = 3
    }

    fun run(
        sentenceId: Long,
        user: User,
    ): UserSentence {
        val sentence =
            wordService.getSentenceByIdAndType(sentenceId, SentenceType.READING) ?: throw NotFoundException(
                message = "Sentence Not Found",
                code = "SENTENCE_1",
            )
        wordService.getUserSentencesBy(user.id, sentence.wordDefinitionId).let { userSentences ->
            if (userSentences.size >= MAX_SENTENCES_COUNT) {
                throw BadRequestException(
                    message = "Maximum of $MAX_SENTENCES_COUNT sentences can be selected",
                    code = "SENTENCE_5",
                )
            }
        }

        return wordService.createUserSentence(
            UserSentenceCreateObject(
                userId = user.id,
                wordDefinitionId = sentence.wordDefinitionId,
                sentenceId = sentence.id,
            ),
        )
    }
}
