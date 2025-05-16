package com.inout.apiserver.infrastructure.mongo.word

import org.bson.types.ObjectId
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.stereotype.Repository

@Repository
interface UserSentenceFeedbackRepository : MongoRepository<UserSentenceFeedback, ObjectId> {
    fun findAllByUserSentenceId(userSentenceId: ObjectId): List<UserSentenceFeedback>
}
