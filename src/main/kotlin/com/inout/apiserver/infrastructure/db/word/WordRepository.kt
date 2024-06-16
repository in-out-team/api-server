package com.inout.apiserver.infrastructure.db.word

import com.inout.apiserver.base.enums.LanguageType
import com.inout.apiserver.domain.word.Word
import org.springframework.stereotype.Repository

@Repository
class WordRepository(
    private val wordJpaRepository: WordJpaRepository
) {
    fun save(word: WordEntity): Word {
        return wordJpaRepository.save(word).toDomain()
    }

    fun findByNameAndLanguage(name: String, language: LanguageType): Word? {
        return wordJpaRepository.findByNameAndLanguage(name, language)?.toDomain()
    }

    fun findById(id: Long): Word? {
        return wordJpaRepository.findById(id).orElse(null)?.toDomain()
    }
}