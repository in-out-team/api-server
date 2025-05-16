package com.inout.apiserver.infrastructure.mongo.study

import org.bson.types.ObjectId
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.aggregation.Aggregation.limit
import org.springframework.data.mongodb.core.aggregation.Aggregation.lookup
import org.springframework.data.mongodb.core.aggregation.Aggregation.match
import org.springframework.data.mongodb.core.aggregation.Aggregation.newAggregation
import org.springframework.data.mongodb.core.aggregation.Aggregation.skip
import org.springframework.data.mongodb.core.aggregation.Aggregation.sort
import org.springframework.data.mongodb.core.aggregation.Aggregation.unwind
import org.springframework.data.mongodb.core.query.Criteria
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
    private val mongoTemplate: MongoTemplate,
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

    // TODO: consider de-normalizing
    fun findAllByUserIdAndWordNamePrefix(
        userId: ObjectId,
        wordNamePrefix: String,
        pageable: Pageable,
    ): Page<MongoStudy> {
        val matchStage = match(Criteria.where("userId").`is`(userId))
        val lookupWordDefinitionStage = lookup("word_definitions", "wordDefinitionId", "_id", "wordDefinitions")
        val unwindWordDefinitionsStage = unwind("wordDefinitions")
        val lookupWordStage = lookup("words", "wordDefinitions.wordId", "_id", "word")
        val unwindWordStage = unwind("word")
        val matchWordNameStage =
            match(
                Criteria
                    .where("word.name")
                    .regex("^$wordNamePrefix", "i"),
            )
        val sortStage = sort(Sort.by(Sort.Order.asc("word.name")))
        val skipStage = skip(pageable.offset)
        val limitStage = limit(pageable.pageSize.toLong())

        val aggregation =
            newAggregation(
                matchStage,
                lookupWordDefinitionStage,
                unwindWordDefinitionsStage,
                lookupWordStage,
                unwindWordStage,
                matchWordNameStage,
                sortStage,
                skipStage,
                limitStage,
            )
        val countAggregation =
            newAggregation(
                matchStage,
                lookupWordDefinitionStage,
                unwindWordDefinitionsStage,
                lookupWordStage,
                unwindWordStage,
                matchWordNameStage,
                org.springframework.data.mongodb.core.aggregation.Aggregation
                    .count()
                    .`as`("total"),
            )

        val countResult =
            mongoTemplate
                .aggregate(countAggregation, "studies", Map::class.java)
                .uniqueMappedResult
                ?.get("total")
                ?.let { it as? Int }
                ?.toLong()
                ?: 0L
        val results =
            mongoTemplate
                .aggregate(aggregation, "studies", MongoStudy::class.java)
                .mappedResults

        return PageImpl(
            results,
            pageable,
            countResult,
        )
    }
}
