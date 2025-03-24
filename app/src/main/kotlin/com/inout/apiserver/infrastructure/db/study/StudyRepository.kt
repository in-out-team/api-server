package com.inout.apiserver.infrastructure.db.study

import com.inout.apiserver.base.alias.StudyId
import com.inout.apiserver.base.alias.UserId
import com.inout.apiserver.base.alias.WordDefinitionId
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import java.time.Instant

interface StudyJpaRepository : JpaRepository<Study, StudyId> {
    fun findByUserIdAndWordDefinitionId(
        userId: UserId,
        wordDefinitionId: WordDefinitionId,
    ): Study?

    fun findAllByUserId(
        userId: UserId,
        pageable: Pageable,
    ): Page<Study>

    fun findAllByUserIdAndDueLessThanOrderByDue(
        userId: UserId,
        due: Instant,
        pageable: Pageable,
    ): Page<Study>

    fun findAllByUserIdAndDueLessThanAndIdNotInOrderByDue(
        userId: UserId,
        due: Instant,
        excludeIds: List<Long>,
        pageable: Pageable,
    ): Page<Study>

    fun findAllByUserIdAndWordDefinitionIdIn(
        userId: UserId,
        wordDefinitionIds: List<WordDefinitionId>,
    ): List<Study>

    @Query(
        """
            SELECT s FROM Study s
            JOIN WordDefinition wd ON s.wordDefinitionId = wd.id
            JOIN Word w ON wd.wordId = w.id
            WHERE s.userId = :userId
              AND LOWER(w.name) LIKE LOWER(CONCAT(:wordNamePrefix, '%'))
            ORDER BY w.name
        """,
    )
    fun findAllByUserIdAndWordNamePrefix(
        userId: UserId,
        wordNamePrefix: String,
        pageable: Pageable,
    ): Page<Study>
}

@Repository
class StudyRepository(
    private val studyJpaRepository: StudyJpaRepository,
) : StudyJpaRepository by studyJpaRepository {
    fun findAllPastDueStudiesBy(
        userId: UserId,
        due: Instant,
        excludeIds: List<StudyId>,
        pageable: Pageable,
    ): Page<Study> =
        if (excludeIds.isEmpty()) {
            findAllByUserIdAndDueLessThanOrderByDue(userId, due, pageable)
        } else {
            findAllByUserIdAndDueLessThanAndIdNotInOrderByDue(userId, due, excludeIds, pageable)
        }
}
