package com.inout.apiserver.infrastructure.mongo.word

import org.bson.types.ObjectId
import org.springframework.data.mongodb.repository.MongoRepository

interface SentenceFeedbackRepository : MongoRepository<SentenceFeedback, ObjectId> {
    fun findBySentenceIdAndSubmittedContent(
        sentenceId: ObjectId,
        submittedContent: String,
    ): SentenceFeedback?

    fun findAllByIdIn(ids: List<ObjectId>): List<SentenceFeedback>
}
