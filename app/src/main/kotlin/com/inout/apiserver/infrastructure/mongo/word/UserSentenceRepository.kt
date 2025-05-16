package com.inout.apiserver.infrastructure.mongo.word

import com.inout.apiserver.base.enums.SentenceType
import org.bson.types.ObjectId
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.stereotype.Repository

@Repository
interface UserSentenceRepository : MongoRepository<UserSentence, ObjectId> {
    fun findByUserIdAndSentenceId(
        userId: ObjectId,
        sentenceId: ObjectId,
    ): UserSentence?

    fun findAllByUserIdAndWordDefinitionIdAndType(
        userId: ObjectId,
        wordDefinitionId: ObjectId,
        type: SentenceType,
    ): List<UserSentence>
}
