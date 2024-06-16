package com.inout.apiserver.domain.word

import com.inout.apiserver.base.enums.LanguageType
import com.inout.apiserver.infrastructure.db.word.WordEntity
import com.inout.apiserver.infrastructure.db.word.WordRepository
import org.springframework.stereotype.Service

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

    fun createWord(wordCreateObject: WordCreateObject): Word {
        // TODO: add duplicate check
        return wordRepository.save(WordEntity.fromCreateObject(wordCreateObject))
    }
}