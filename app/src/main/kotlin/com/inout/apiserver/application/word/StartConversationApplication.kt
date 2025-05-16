package com.inout.apiserver.application.word

import com.inout.apiserver.base.enums.SenderType
import com.inout.apiserver.base.util.LocalizedResponseProvider
import com.inout.apiserver.domain.study.MongoStudyService
import com.inout.apiserver.domain.word.AudioAIService
import com.inout.apiserver.domain.word.ConversationWithMessages
import com.inout.apiserver.domain.word.MongoWordService
import com.inout.apiserver.error.BadRequestException
import com.inout.apiserver.infrastructure.mongo.user.MongoUser
import org.bson.types.ObjectId
import org.springframework.stereotype.Component

@Component
class StartConversationApplication(
    private val wordService: MongoWordService,
    private val studyService: MongoStudyService,
    private val audioAIService: AudioAIService,
    private val localizedResponseProvider: LocalizedResponseProvider,
) {
    companion object {
        private const val MAX_CONVERSATION_COUNT = 3
    }

    data class Request(
        val wordDefinitionId: ObjectId,
        val user: MongoUser,
    )

    data class Response(
        val conversation: ConversationWithMessages,
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

                val word = wordService.getWordByLiveWordDefinitionId(request.wordDefinitionId)
                val audio =
                    audioAIService.findOrCreateAudio(
                        language = word.fromLanguage,
                        content =
                            localizedResponseProvider.getInitialConversationPrompt(
                                studyLanguage = word.fromLanguage,
                                wordName = word.name,
                            ),
                    )

                wordService.createConversation(
                    userId = request.user.id,
                    wordDefinitionId = request.wordDefinitionId,
                    systemMessage = audio.content,
                    systemAudio = audio,
                )
            }

        return Response(conversation)
    }
}
