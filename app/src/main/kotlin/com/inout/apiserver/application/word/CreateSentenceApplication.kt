package com.inout.apiserver.application.word

import com.inout.apiserver.base.service.openai.OpenAIService
import com.inout.apiserver.domain.word.SentenceCreateObject
import com.inout.apiserver.domain.word.WordDefinition
import com.inout.apiserver.domain.word.WordService
import com.inout.apiserver.error.NotFoundException
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
@Transactional
class CreateSentenceApplication(
    private val wordService: WordService,
    private val openAIService: OpenAIService,
) {
    fun run(wordId: Long) {
        val word =
            wordService.getWordById(wordId) ?: throw NotFoundException(message = "Word not found", code = "WORD_4")
        // TODO: need to bulk create sentences
        word.definitions.forEach { wordDefinition: WordDefinition ->
            wordService.getSentencesByWordDefinitionId(wordDefinition.id).let {
                if (it.isNotEmpty()) return@forEach
            }

            val openAIWordDefinitionSentenceResponse =
                openAIService.fetchWordDefinitionSentence(
                    word = word.name,
                    meaning = wordDefinition.meaning,
                    fromLanguage =
                        word.fromLanguage.name
                            .lowercase()
                            .replaceFirstChar { it.uppercase() },
                    toLanguage =
                        word.toLanguage.name
                            .lowercase()
                            .replaceFirstChar { it.uppercase() },
                )

            openAIWordDefinitionSentenceResponse.sentences.forEach {
                val sentenceCreateObject =
                    SentenceCreateObject(
                        wordDefinitionId = wordDefinition.id,
                        content = it.content,
                        translation = it.translation,
                    )
                wordService.createSentence(sentenceCreateObject)
            }
        }
    }
}
