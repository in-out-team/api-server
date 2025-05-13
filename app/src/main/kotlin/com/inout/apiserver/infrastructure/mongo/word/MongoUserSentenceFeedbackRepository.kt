package com.inout.apiserver.infrastructure.mongo.word

import org.bson.types.ObjectId
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.stereotype.Repository

@Repository
interface MongoUserSentenceFeedbackRepository : MongoRepository<MongoUserSentenceFeedback, ObjectId> {
    fun findAllByUserSentenceId(userSentenceId: ObjectId): List<MongoUserSentenceFeedback>
}
