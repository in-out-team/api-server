package com.inout.apiserver.infrastructure.db.word

import com.inout.apiserver.base.enums.LanguageType
import org.springframework.data.jpa.repository.JpaRepository

interface WordJpaRepository : JpaRepository<WordEntity, Long> {
    fun findByNameAndLanguage(name: String, language: LanguageType): WordEntity?
}