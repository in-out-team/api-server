package com.inout.apiserver.infrastructure.db.word

import org.springframework.data.jpa.repository.JpaRepository

interface WordJpaRepository : JpaRepository<WordEntity, Long> {
    fun findByNameAndLanguage(name: String, language: LanguageTypes): WordEntity?
}