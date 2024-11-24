package com.inout.apiserver.domain.word

import com.inout.apiserver.base.enums.LanguageType
import com.inout.apiserver.base.enums.LexicalCategoryType
import com.inout.apiserver.error.ConflictException
import com.inout.apiserver.error.NotFoundException
import com.inout.apiserver.infrastructure.db.word.SentenceEntity
import com.inout.apiserver.infrastructure.db.word.SentenceRepository
import com.inout.apiserver.infrastructure.db.word.WordEntity
import com.inout.apiserver.infrastructure.db.word.WordRepository
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class WordService(
    private val wordRepository: WordRepository,
    private val sentenceRepository: SentenceRepository,
) {
    fun getWordByNameAndFromLanguageAndToLanguage(
        name: String,
        fromLanguage: LanguageType,
        toLanguage: LanguageType,
    ): Word? = wordRepository.findByNameAndFromLanguageAndToLanguage(name, fromLanguage, toLanguage)

    fun getWordById(id: Long): Word? = wordRepository.findById(id)

    @Transactional
    fun createWord(wordCreateObject: WordCreateObject): Word {
        getWordByNameAndFromLanguageAndToLanguage(
            name = wordCreateObject.name,
            fromLanguage = wordCreateObject.fromLanguage,
            toLanguage = wordCreateObject.toLanguage,
        )?.let {
            throw ConflictException(message = "Word already exists", code = "WORD_1")
        }

        return wordRepository.save(WordEntity.fromCreateObject(wordCreateObject))
    }

    @Transactional
    fun createSentence(sentenceCreateObject: SentenceCreateObject): Sentence =
        sentenceRepository.save(SentenceEntity.fromCreateObject(sentenceCreateObject))

    fun getWordsWithDefinitions(
        fromLanguage: LanguageType,
        toLanguage: LanguageType,
        prefix: String,
        lexicalCategory: LexicalCategoryType?,
        pageable: Pageable,
    ): Page<Word> = wordRepository.findWordsWithDefinitions(fromLanguage, toLanguage, prefix, lexicalCategory, pageable)

    fun getWordByWordDefinitionId(wordDefinitionId: Long): Word =
        wordRepository
            .findByWordDefinitionId(wordDefinitionId)
            ?.let { word -> word.copy(definitions = word.definitions.filter { it.id == wordDefinitionId }) }
            ?: throw NotFoundException(message = "Word Definition not found", code = "WORD_2")

    fun getWordsByWordDefinitionIds(wordDefinitionIds: List<Long>): List<Word> {
        val ids = wordDefinitionIds.toSet()
        return wordRepository
            .findAllByWordDefinitionIds(wordDefinitionIds)
            .map { word -> word.copy(definitions = word.definitions.filter { it.id in ids }) }
    }

    fun getSentencesByWordDefinitionId(wordDefinitionId: Long): List<Sentence> =
        sentenceRepository.findAllByWordDefinitionId(wordDefinitionId)
}
