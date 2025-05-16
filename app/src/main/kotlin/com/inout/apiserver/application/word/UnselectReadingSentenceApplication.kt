package com.inout.apiserver.application.word

import com.inout.apiserver.base.enums.SentenceType
import com.inout.apiserver.domain.word.MongoWordService
import com.inout.apiserver.error.NotFoundException
import com.inout.apiserver.infrastructure.mongo.user.MongoUser
import org.bson.types.ObjectId
import org.springframework.stereotype.Component

@Component
class UnselectReadingSentenceApplication(
    private val wordService: MongoWordService,
) {
    data class Request(
        val sentenceId: ObjectId,
        val user: MongoUser,
    )

    fun run(request: Request) {
        wordService
            .getUserSentenceBy(request.user.id!!, request.sentenceId)
            ?.takeIf { userSentence ->
                wordService.getSentenceByIdAndType(
                    userSentence.sentenceId,
                    SentenceType.READING,
                ) != null
            }?.let { userSentence -> wordService.deleteUserSentence(userSentence) }
            ?: throw NotFoundException(message = "UserSentence not found", code = "SENTENCE_3")
    }
}
