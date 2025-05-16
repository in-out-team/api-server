package com.inout.apiserver.interfaces.web.v1.response

import com.inout.apiserver.infrastructure.mongo.word.UserSentence
import org.bson.types.ObjectId

data class UserSentenceResponse(
    val id: ObjectId,
    val userId: ObjectId,
    val wordDefinitionId: ObjectId,
    val sentenceId: ObjectId,
) {
    companion object {
        fun of(userSentence: UserSentence): UserSentenceResponse =
            UserSentenceResponse(
                id = userSentence.id!!,
                userId = userSentence.userId,
                wordDefinitionId = userSentence.wordDefinitionId,
                sentenceId = userSentence.sentenceId,
            )
    }
}
