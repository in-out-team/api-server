package com.inout.apiserver.infrastructure.db.word

import com.inout.apiserver.base.alias.WordDefinitionId
import com.inout.apiserver.base.alias.WordId
import com.inout.apiserver.base.enums.LanguageType
import com.inout.apiserver.base.enums.LexicalCategoryType
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository

@Repository
interface WordRepository : JpaRepository<Word, WordId> {
    fun findByNameAndFromLanguageAndToLanguage(
        name: String,
        fromLanguage: LanguageType,
        toLanguage: LanguageType,
    ): Word?

    /**
     * TODO:
     * WARN message when JOIN FETCH is used with pagination for OneToMany:
     * HHH90003004: firstResult/maxResults specified with collection fetch; applying in memory
     */
    @Query(
        """
            SELECT DISTINCT w FROM Word w
            JOIN FETCH w.definitions wd
            WHERE w.fromLanguage = :fromLanguage
              AND w.toLanguage = :toLanguage
              AND LOWER(w.name) LIKE LOWER(CONCAT(:prefix, '%'))
              AND (:lexicalCategory IS NULL OR wd.lexicalCategory = :lexicalCategory)
              AND wd.status = 'LIVE'
        """,
    )
    fun findAllWithLiveDefinitionsBy(
        fromLanguage: LanguageType,
        toLanguage: LanguageType,
        prefix: String,
        lexicalCategory: LexicalCategoryType?,
        pageable: Pageable,
    ): Page<Word>

    @Query(
        """
            SELECT w FROM Word w
            JOIN FETCH w.definitions wd
            WHERE wd.id = :wordDefinitionId
              AND wd.status = 'LIVE'
        """,
    )
    fun findByLiveDefinitionsId(wordDefinitionId: WordDefinitionId): Word?

    @Query(
        """
            SELECT w FROM Word w
            JOIN FETCH w.definitions wd
            WHERE wd.id IN :wordDefinitionIds
              AND wd.status = 'LIVE'
        """,
    )
    fun findAllByLiveDefinitionsIds(wordDefinitionIds: List<WordDefinitionId>): List<Word>
}
