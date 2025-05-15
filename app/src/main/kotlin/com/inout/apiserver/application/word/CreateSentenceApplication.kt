package com.inout.apiserver.application.word

import com.inout.apiserver.base.enums.LexicalCategoryType
import com.inout.apiserver.base.enums.SentenceType
import com.inout.apiserver.base.service.DictionaryService
import com.inout.apiserver.domain.word.MongoWordService
import com.inout.apiserver.error.BadRequestException
import com.inout.apiserver.error.NotFoundException
import com.inout.apiserver.infrastructure.mongo.word.MongoSentence
import org.bson.types.ObjectId
import org.jobrunr.scheduling.JobScheduler
import org.springframework.stereotype.Component

@Component
class CreateSentenceApplication(
    private val wordService: MongoWordService,
    private val dictionaryService: DictionaryService,
    private val jobScheduler: JobScheduler,
) {
    data class Request(
        val wordId: ObjectId,
    )

    fun run(request: Request) {
        val word =
            wordService.getWordWithDefinitionsBy(request.wordId) ?: throw NotFoundException(
                message = "Word not found",
                code = "WORD_4",
            )

        word.definitions.forEach { wordDefinition ->
            jobScheduler.enqueue { createSentencesFor(word.id!!, wordDefinition.id!!) }
        }
    }

    fun createSentencesFor(
        wordId: ObjectId,
        wordDefinitionId: ObjectId,
    ) {
        wordService.getSentencesByWordDefinitionId(wordDefinitionId).let {
            if (it.isNotEmpty()) return
        }

        val word =
            wordService.getWordWithDefinitionsBy(wordId) ?: throw NotFoundException(
                message = "Word not found",
                code = "WORD_4",
            )
        val wordDefinition =
            word.definitions.find { it.id == wordDefinitionId } ?: throw NotFoundException(
                message = "Word definition not found",
                code = "WORD_4",
            )

        val sentences =
            dictionaryService.fetchWordDefinitionSentences(
                word = word.name,
                meaning = wordDefinition.meaning,
                fromLanguage = word.fromLanguage,
                toLanguage = word.toLanguage,
            )
        if (sentences.isEmpty()) {
            throw BadRequestException(
                message = "Valid sentences not found for wordDefinitionId: $wordDefinitionId",
                code = "SENTENCE_9",
            )
        }

        sentences.forEachIndexed { index, sentence ->
            wordService.createSentence(
                wordDefinitionId = wordDefinitionId,
                type = if (index % 2 == 0) SentenceType.READING else SentenceType.WRITING,
                sentence = sentence.content,
                translation = sentence.translation,
                lexicalCategories =
                    sentence.lexicalCategories.map { lexicalCategoryMap ->
                        MongoSentence.LexicalCategoryInfo(
                            word = lexicalCategoryMap.word,
                            lexicalCategory = LexicalCategoryType.of(lexicalCategoryMap.lexicalCategory),
                        )
                    },
            )
        }
    }
}
