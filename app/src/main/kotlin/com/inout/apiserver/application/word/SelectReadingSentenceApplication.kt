package com.inout.apiserver.application.word

import com.inout.apiserver.base.enums.SentenceType
import com.inout.apiserver.domain.study.StudyService
import com.inout.apiserver.domain.word.WordService
import com.inout.apiserver.error.BadRequestException
import com.inout.apiserver.error.NotFoundException
import com.inout.apiserver.infrastructure.mongo.user.User
import com.inout.apiserver.infrastructure.mongo.word.UserSentence
import org.bson.types.ObjectId
import org.springframework.stereotype.Component

@Component
class SelectReadingSentenceApplication(
    private val wordService: WordService,
    private val studyService: StudyService,
) {
    companion object {
        private const val MAX_SENTENCES_COUNT = 3
    }

    data class Request(
        val sentenceId: ObjectId,
        val user: User,
    )

    data class Response(
        val userSentence: UserSentence,
    )

    fun run(request: Request): Response {
        val sentence =
            wordService.getSentenceByIdAndType(request.sentenceId, SentenceType.READING) ?: throw NotFoundException(
                message = "Sentence Not Found",
                code = "SENTENCE_1",
            )

        sentence.wordDefinitionId.let { wordDefinitionId ->
            studyService.getByUserIdAndWordDefinitionId(
                userId = request.user.id!!,
                wordDefinitionId = wordDefinitionId,
            ) ?: throw BadRequestException(
                message = "User is not studying given wordDefinitionId of $wordDefinitionId",
                code = "SENTENCE_10",
            )
        }

        wordService
            .getUserSentencesBy(request.user.id!!, sentence.wordDefinitionId, SentenceType.READING)
            .let { userSentences ->
                if (userSentences.size >= MAX_SENTENCES_COUNT) {
                    throw BadRequestException(
                        message = "Maximum of $MAX_SENTENCES_COUNT sentences can be selected",
                        code = "SENTENCE_5",
                    )
                }
            }

        return Response(
            userSentence =
                wordService.createUserSentence(
                    userId = request.user.id,
                    wordDefinitionId = sentence.wordDefinitionId,
                    type = SentenceType.READING,
                    sentenceId = sentence.id!!,
                ),
        )
    }
}
