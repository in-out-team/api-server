package com.inout.apiserver.infrastructure.mongo.word

import com.inout.apiserver.base.enums.SentenceType
import org.bson.types.ObjectId
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.stereotype.Repository

@Repository
interface MongoUserSentenceRepository : MongoRepository<MongoUserSentence, ObjectId> {
    fun findByUserIdAndSentenceId(
        userId: ObjectId,
        sentenceId: ObjectId,
    ): MongoUserSentence?

    fun findAllByUserIdAndWordDefinitionIdAndType(
        userId: ObjectId,
        wordDefinitionId: ObjectId,
        type: SentenceType,
    ): List<MongoUserSentence>
}
