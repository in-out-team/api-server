package com.inout.apiserver.infrastructure.mongo.study

import org.bson.types.ObjectId
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.stereotype.Repository
import java.time.Instant

interface MongoStudyRepositoryInternal : MongoRepository<MongoStudy, ObjectId> {
    fun findByUserIdAndWordDefinitionId(
        userId: ObjectId,
        wordDefinitionId: ObjectId,
    ): MongoStudy?

    fun findAllByUserId(
        userId: ObjectId,
        pageable: Pageable,
    ): Page<MongoStudy>

    fun findAllByUserIdAndDueLessThanOrderByDue(
        userId: ObjectId,
        due: Instant,
        pageable: Pageable,
    ): Page<MongoStudy>

    fun findAllByUserIdAndDueLessThanAndIdNotInOrderByDue(
        userId: ObjectId,
        due: Instant,
        excludeIds: List<ObjectId>,
        pageable: Pageable,
    ): Page<MongoStudy>

    fun findAllByUserIdAndWordDefinitionIdIn(
        userId: ObjectId,
        wordDefinitionIds: List<ObjectId>,
    ): List<MongoStudy>

    fun existsByWordDefinitionId(wordDefinitionId: ObjectId): Boolean
}

@Repository
class MongoStudyRepository(
    private val mongoStudyRepositoryInternal: MongoStudyRepositoryInternal,
) : MongoStudyRepositoryInternal by mongoStudyRepositoryInternal {
    fun findAllPastDueStudiesBy(
        userId: ObjectId,
        due: Instant,
        excludeIds: List<ObjectId>,
        pageable: Pageable,
    ): Page<MongoStudy> =
        if (excludeIds.isEmpty()) {
            mongoStudyRepositoryInternal.findAllByUserIdAndDueLessThanOrderByDue(
                userId = userId,
                due = due,
                pageable = pageable,
            )
        } else {
            mongoStudyRepositoryInternal.findAllByUserIdAndDueLessThanAndIdNotInOrderByDue(
                userId = userId,
                due = due,
                excludeIds = excludeIds,
                pageable = pageable,
            )
        }

    fun findAllByUserIdAndWordNamePrefix(
        userId: ObjectId,
        wordNamePrefix: String,
        pageable: Pageable,
    ): Page<MongoStudy> {
        TODO("Implement this later with MongoTemplate")
    }
}
