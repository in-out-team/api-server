package com.inout.apiserver.infrastructure.db.word

import com.inout.apiserver.base.enums.LanguageType
import org.springframework.data.jpa.repository.JpaRepository

interface WordJpaRepository : JpaRepository<WordEntity, Long> {
    fun findByNameAndFromLanguageAndToLanguage(
        name: String,
        fromLanguage: LanguageType,
        toLanguage: LanguageType
    ): WordEntity?
}