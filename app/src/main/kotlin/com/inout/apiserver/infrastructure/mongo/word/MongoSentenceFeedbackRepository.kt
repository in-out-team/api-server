package com.inout.apiserver.infrastructure.mongo.word

import org.bson.types.ObjectId
import org.springframework.data.mongodb.repository.MongoRepository

interface MongoSentenceFeedbackRepository : MongoRepository<MongoSentenceFeedback, ObjectId> {
    fun findBySentenceIdAndSubmittedContent(
        sentenceId: ObjectId,
        submittedContent: String,
    ): MongoSentenceFeedback?

    fun findAllByIdIn(ids: List<ObjectId>): List<MongoSentenceFeedback>
}
