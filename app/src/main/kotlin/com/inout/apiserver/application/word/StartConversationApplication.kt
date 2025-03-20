package com.inout.apiserver.application.word

import com.inout.apiserver.base.alias.WordDefinitionId
import com.inout.apiserver.base.enums.LanguageType
import com.inout.apiserver.base.enums.SenderType
import com.inout.apiserver.domain.study.StudyService
import com.inout.apiserver.domain.word.AudioAIService
import com.inout.apiserver.domain.word.ConversationCreateObject
import com.inout.apiserver.domain.word.WordService
import com.inout.apiserver.error.BadRequestException
import com.inout.apiserver.infrastructure.db.user.User
import com.inout.apiserver.infrastructure.db.word.Conversation
import org.springframework.stereotype.Component

@Component
class StartConversationApplication(
    private val wordService: WordService,
    private val studyService: StudyService,
    private val audioAIService: AudioAIService,
) {
    companion object {
        private const val MAX_CONVERSATION_COUNT = 3
    }

    data class Request(
        val wordDefinitionId: WordDefinitionId,
        val user: User,
    )

    data class Response(
        val conversation: Conversation,
    )

    fun run(request: Request): Response {
        studyService.getByUserIdAndWordDefinitionId(request.user.id!!, request.wordDefinitionId)
            ?: throw BadRequestException(
                message = "User is not studying given wordDefinitionId of ${request.wordDefinitionId}",
                code = "CONVERSATION_1",
            )
        val conversations = wordService.getConversationsBy(request.user, request.wordDefinitionId)
        val conversation =
            conversations.firstOrNull { conversation ->
                conversation.messages.count { it.sender == SenderType.USER } == 0
            } ?: run {
                if (conversations.size >= MAX_CONVERSATION_COUNT) {
                    throw BadRequestException(
                        message = "Reached maximum number of conversations",
                        code = "CONVERSATION_2",
                    )
                }

                val word = wordService.getWordByWordDefinitionId(request.wordDefinitionId)
                val audio =
                    audioAIService.findOrCreateAudio(
                        language = word.fromLanguage,
                        content =
                            when (word.fromLanguage) { // TODO: create a separate class responsible for generating initial conversation
                                LanguageType.ENGLISH -> "Let's talk about ${word.name}!"
                                LanguageType.KOREAN -> "${word.name}에 대해 이야기 해볼까요?"
                            },
                    )

                wordService.createConversation(
                    ConversationCreateObject(
                        userId = request.user.id!!,
                        wordDefinitionId = request.wordDefinitionId,
                        systemMessage = audio.content,
                        systemAudio = audio,
                    ),
                )
            }

        return Response(conversation)
    }
}
