package com.inout.apiserver.application.word

import com.inout.apiserver.base.alias.WordDefinitionId
import com.inout.apiserver.base.alias.WordId
import com.inout.apiserver.base.enums.LexicalCategoryType
import com.inout.apiserver.base.enums.SentenceType
import com.inout.apiserver.base.service.openai.OpenAIService
import com.inout.apiserver.domain.word.SentenceCreateObject
import com.inout.apiserver.domain.word.WordService
import com.inout.apiserver.error.NotFoundException
import com.inout.apiserver.infrastructure.db.word.Sentence
import org.jobrunr.scheduling.JobScheduler
import org.springframework.stereotype.Component

@Component
class CreateSentenceApplication(
    private val wordService: WordService,
    private val openAIService: OpenAIService,
    private val jobScheduler: JobScheduler,
) {
    data class Request(
        val wordId: WordId,
    )

    fun run(request: Request) {
        val word =
            wordService.getWordById(request.wordId) ?: throw NotFoundException(
                message = "Word not found",
                code = "WORD_4",
            )

        word.definitions.forEach { wordDefinition ->
            jobScheduler.enqueue { createSentencesFor(word.id!!, wordDefinition.id!!) }
        }
    }

    fun createSentencesFor(
        wordId: WordId,
        wordDefinitionId: WordDefinitionId,
    ) {
        wordService.getSentencesByWordDefinitionId(wordDefinitionId).let {
            if (it.isNotEmpty()) return
        }

        val word =
            wordService.getWordById(wordId) ?: throw NotFoundException(
                message = "Word not found",
                code = "WORD_4",
            )
        val wordDefinition =
            word.definitions.find { it.id == wordDefinitionId } ?: throw NotFoundException(
                message = "Word definition not found",
                code = "WORD_4",
            )

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

        openAIWordDefinitionSentenceResponse.sentences.forEachIndexed { index, sentence ->
            val sentenceCreateObject =
                SentenceCreateObject(
                    wordDefinitionId = wordDefinitionId,
                    type = if (index % 2 == 0) SentenceType.READING else SentenceType.WRITING,
                    content = sentence.content,
                    translation = sentence.translation,
                    lexicalCategories =
                        sentence.lexicalCategories.map { lexicalCategoryMap ->
                            Sentence.LexicalCategoryInfo(
                                word = lexicalCategoryMap.word,
                                lexicalCategory = LexicalCategoryType.of(lexicalCategoryMap.lexicalCategory),
                            )
                        },
                )
            wordService.createSentence(sentenceCreateObject)
        }
    }
}
