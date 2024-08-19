package com.inout.apiserver.domain.word

import com.inout.apiserver.base.enums.LanguageType
import com.inout.apiserver.base.enums.LexicalCategoryType
import com.inout.apiserver.error.ConflictException
import com.inout.apiserver.infrastructure.db.word.WordEntity
import com.inout.apiserver.infrastructure.db.word.WordRepository
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class WordService(
    private val wordRepository: WordRepository
) {
    fun getWordByNameAndFromLanguageAndToLanguage(
        name: String,
        fromLanguage: LanguageType,
        toLanguage: LanguageType
    ): Word? {
        return wordRepository.findByNameAndFromLanguageAndToLanguage(name, fromLanguage, toLanguage)
    }

    fun getWordById(id: Long): Word? {
        return wordRepository.findById(id)
    }

    @Transactional
    fun createWord(wordCreateObject: WordCreateObject): Word {
        getWordByNameAndFromLanguageAndToLanguage(
            name = wordCreateObject.name,
            fromLanguage = wordCreateObject.fromLanguage,
            toLanguage = wordCreateObject.toLanguage
        )?.let {
            throw ConflictException(message = "Word already exists", code = "WORD_1")
        }

        return wordRepository.save(WordEntity.fromCreateObject(wordCreateObject))
    }

    fun getWordsWithDefinitions(
        fromLanguage: LanguageType,
        toLanguage: LanguageType,
        prefix: String,
        lexicalCategory: LexicalCategoryType?,
        pageable: Pageable
    ): Page<Word> {
        return wordRepository.findWordsWithDefinitions(fromLanguage, toLanguage, prefix, lexicalCategory, pageable)
    }
}