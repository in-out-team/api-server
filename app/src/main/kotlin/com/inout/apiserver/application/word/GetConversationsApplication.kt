package com.inout.apiserver.application.word

import com.inout.apiserver.base.alias.WordDefinitionId
import com.inout.apiserver.domain.study.StudyService
import com.inout.apiserver.domain.word.WordService
import com.inout.apiserver.error.BadRequestException
import com.inout.apiserver.infrastructure.db.user.User
import com.inout.apiserver.infrastructure.db.word.Conversation
import org.springframework.stereotype.Component

@Component
class GetConversationsApplication(
    private val studyService: StudyService,
    private val wordService: WordService,
) {
    data class Request(
        val wordDefinitionId: WordDefinitionId,
        val user: User,
    )

    data class Response(
        val conversations: List<Conversation>,
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
