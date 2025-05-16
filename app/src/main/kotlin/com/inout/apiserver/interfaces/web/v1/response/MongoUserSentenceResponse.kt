package com.inout.apiserver.interfaces.web.v1.response

import com.inout.apiserver.infrastructure.mongo.word.MongoUserSentence
import org.bson.types.ObjectId

data class MongoUserSentenceResponse(
    val id: ObjectId,
    val userId: ObjectId,
    val wordDefinitionId: ObjectId,
    val sentenceId: ObjectId,
) {
    companion object {
        fun of(userSentence: MongoUserSentence): MongoUserSentenceResponse =
            MongoUserSentenceResponse(
                id = userSentence.id!!,
                userId = userSentence.userId,
                wordDefinitionId = userSentence.wordDefinitionId,
                sentenceId = userSentence.sentenceId,
            )
    }
}
