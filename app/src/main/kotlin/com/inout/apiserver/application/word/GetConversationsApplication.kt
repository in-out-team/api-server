package com.inout.apiserver.application.word

import com.inout.apiserver.domain.study.StudyService
import com.inout.apiserver.domain.user.User
import com.inout.apiserver.domain.word.Conversation
import com.inout.apiserver.domain.word.WordService
import com.inout.apiserver.error.BadRequestException
import org.springframework.stereotype.Component

@Component
class GetConversationsApplication(
    private val studyService: StudyService,
    private val wordService: WordService,
) {
    data class Response(
        val conversations: List<Conversation>,
    )

    fun run(
        wordDefinitionId: Long,
        user: User,
    ): Response {
        studyService.getByUserIdAndWordDefinitionId(user.id, wordDefinitionId)
            ?: throw BadRequestException(
                message = "User is not studying given wordDefinitionId of $wordDefinitionId",
                code = "CONVERSATION_1",
            )

        return Response(
            conversations = wordService.getConversationsBy(user, wordDefinitionId),
        )
    }
}
