package com.inout.apiserver.application.word

import com.inout.apiserver.domain.user.User
import com.inout.apiserver.domain.word.UserSentenceCreateObject
import com.inout.apiserver.domain.word.WordService
import com.inout.apiserver.error.NotFoundException
import org.springframework.stereotype.Component

@Component
class SelectSentenceApplication(
    private val wordService: WordService,
) {
    fun run(
        wordId: Long,
        wordDefinitionId: Long,
        sentenceId: Long,
        user: User,
    ) {
        val sentence =
            wordService
                .getWordById(wordId)
                ?.let { word ->
                    word.definitions.firstOrNull { it.id == wordDefinitionId }
                }?.let { wordDefinition ->
                    wordService.getSentencesByWordDefinitionId(wordDefinition.id).firstOrNull { it.id == sentenceId }
                } ?: throw NotFoundException(message = "Sentence Not Found", code = "WORD_5")

        val createdUserSentence =
            wordService.createUserSentence(
                UserSentenceCreateObject(
                    userId = user.id,
                    wordDefinitionId = wordDefinitionId,
                    sentenceId = sentence.id,
                ),
            )
    }
}
