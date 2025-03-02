package com.inout.apiserver.application.word

import com.inout.apiserver.base.alias.ConversationId
import com.inout.apiserver.base.enums.SenderType
import com.inout.apiserver.base.service.openai.OpenAIService
import com.inout.apiserver.domain.word.WordAIService
import com.inout.apiserver.domain.word.WordService
import com.inout.apiserver.error.BadRequestException
import com.inout.apiserver.error.InternalServerErrorException
import com.inout.apiserver.error.NotFoundException
import com.inout.apiserver.infrastructure.db.user.User
import com.inout.apiserver.infrastructure.db.word.Conversation
import org.springframework.stereotype.Component

@Component
class RespondToConversationApplication(
    private val wordService: WordService,
    private val openAIService: OpenAIService,
    private val wordAIService: WordAIService,
) {
    companion object {
        const val MAX_SYSTEM_RESPONSES = 4
    }

    data class Request(
        val conversationId: ConversationId,
        val responseMessage: String,
        val user: User,
    )

    data class Response(
        val updatedConversation: Conversation,
    )

    fun run(request: Request): Response {
        val conversation =
            wordService
                .getConversationById(request.conversationId)
                ?.takeIf { it.userId == request.user.id }
                ?: throw NotFoundException(message = "Conversation not found", code = "CONVERSATION_3")

        if (conversation.messages.count { it.sender == SenderType.SYSTEM } >= MAX_SYSTEM_RESPONSES) {
            throw BadRequestException(message = "System response limit exceeded", code = "CONVERSATION_4")
        }

        if (conversation.messages.last().sender == SenderType.USER) {
            throw BadRequestException(message = "System has not responded yet", code = "CONVERSATION_5")
        }

        val word = wordService.getWordByWordDefinitionId(conversation.wordDefinitionId)
        val wordDefinition =
            word.definitions.firstOrNull { it.id == conversation.wordDefinitionId }
                ?: throw InternalServerErrorException(message = "Data Integrity Error", code = "CONVERSATION_6")

        conversation.addUserMessage(request.responseMessage)

        val systemResponse =
            openAIService.fetchConversation(
                fromLanguage = word.fromLanguage.toString(),
                toLanguage = word.toLanguage.toString(),
                wordName = word.name,
                wordMeaning = wordDefinition.meaning,
                messages = conversation.messages.map { it.sender to it.content },
            )
        val systemAudio =
            wordAIService.findOrCreateAudio(
                language = word.fromLanguage,
                content = systemResponse, // TODO: error raised when response is too long (varchar of 255)
            )

        conversation.addSystemMessage(systemResponse, systemAudio)

        val updatedConversation = wordService.updateConversation(conversation)
        return Response(updatedConversation)
    }
}
