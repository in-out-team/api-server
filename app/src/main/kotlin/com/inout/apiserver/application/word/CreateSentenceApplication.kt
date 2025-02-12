package com.inout.apiserver.application.word

import com.inout.apiserver.base.alias.WordId
import com.inout.apiserver.base.enums.LexicalCategoryType
import com.inout.apiserver.base.enums.SentenceType
import com.inout.apiserver.base.service.openai.OpenAIService
import com.inout.apiserver.domain.word.SentenceCreateObject
import com.inout.apiserver.domain.word.WordService
import com.inout.apiserver.error.NotFoundException
import com.inout.apiserver.infrastructure.db.word.Sentence
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
@Transactional
class CreateSentenceApplication(
    private val wordService: WordService,
    private val openAIService: OpenAIService,
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
        // TODO: need to bulk create sentences
        word.definitions.forEach { wordDefinition ->
            wordService.getSentencesByWordDefinitionId(wordDefinition.id!!).let {
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

            openAIWordDefinitionSentenceResponse.sentences.forEachIndexed { index, sentence ->
                val sentenceCreateObject =
                    SentenceCreateObject(
                        wordDefinitionId = wordDefinition.id,
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
}
