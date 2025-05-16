package com.inout.apiserver.infrastructure.mongo.word

import com.inout.apiserver.base.enums.SentenceType
import org.bson.types.ObjectId
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.stereotype.Repository

@Repository
interface SentenceRepository : MongoRepository<Sentence, ObjectId> {
    fun findAllByWordDefinitionId(wordDefinitionId: ObjectId): List<Sentence>

    fun findAllByWordDefinitionIdAndType(
        wordDefinitionId: ObjectId,
        type: SentenceType,
    ): List<Sentence>

    fun findByIdAndType(
        id: ObjectId,
        type: SentenceType,
    ): Sentence?
}
