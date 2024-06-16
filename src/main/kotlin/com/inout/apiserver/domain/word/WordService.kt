package com.inout.apiserver.domain.word

import com.inout.apiserver.base.enums.LanguageType
import com.inout.apiserver.infrastructure.db.word.WordEntity
import com.inout.apiserver.infrastructure.db.word.WordRepository
import org.springframework.stereotype.Service

@Service
class WordService(
    private val wordRepository: WordRepository
) {
    fun getWordByNameAndLanguage(name: String, language: LanguageType): Word? {
        return wordRepository.findByNameAndLanguage(name, language)
    }

    fun getWordById(id: Long): Word? {
        return wordRepository.findById(id)
    }

    fun createWord(wordCreateObject: WordCreateObject): Word {
        // TODO: add duplicate check
        return wordRepository.save(WordEntity.fromCreateObject(wordCreateObject))
    }
}