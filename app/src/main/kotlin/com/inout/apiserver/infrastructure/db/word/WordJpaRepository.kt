package com.inout.apiserver.infrastructure.db.word

import com.inout.apiserver.base.enums.LanguageType
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.JpaSpecificationExecutor

interface WordJpaRepository : JpaRepository<WordEntity, Long>, JpaSpecificationExecutor<WordEntity> {
    fun findByNameAndFromLanguageAndToLanguage(
        name: String,
        fromLanguage: LanguageType,
        toLanguage: LanguageType
    ): WordEntity?
}
