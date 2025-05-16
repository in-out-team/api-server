package com.inout.apiserver.application.word

import com.inout.apiserver.domain.study.MongoStudyService
import com.inout.apiserver.domain.word.ConversationWithMessages
import com.inout.apiserver.domain.word.MongoWordService
import com.inout.apiserver.error.BadRequestException
import com.inout.apiserver.infrastructure.mongo.user.MongoUser
import org.bson.types.ObjectId
import org.springframework.stereotype.Component

@Component
class GetConversationsApplication(
    private val studyService: MongoStudyService,
    private val wordService: MongoWordService,
) {
    data class Request(
        val wordDefinitionId: ObjectId,
        val user: MongoUser,
    )

    data class Response(
        val conversations: List<ConversationWithMessages>,
    )

    fun run(request: Request): Response {
        studyService.getByUserIdAndWordDefinitionId(request.user.id!!, request.wordDefinitionId)
            ?: throw BadRequestException(
                message = "User is not studying given wordDefinitionId of ${request.wordDefinitionId}",
                code = "CONVERSATION_1",
            )

        return Response(
            conversations = wordService.getConversationsBy(request.user, request.wordDefinitionId),
        )
    }
}
