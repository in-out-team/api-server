package com.inout.apiserver.application.word

import com.inout.apiserver.domain.user.User
import com.inout.apiserver.domain.word.WordService
import org.springframework.stereotype.Component

@Component
class GetWritingSentenceFeedback(
    private val wordService: WordService,
) {
    data class Request(
        val user: User,
        val sentenceId: Long,
        /**
         * user's submitted answer for given writing practice sentence
         */
        val submittedContent: String,
    )

    fun run(request: Request) {
        /**
         * check whether user is in progress of writing practice
         * - user has userSentence of respective sentenceId
         * - userSentence should be in progress
         *   - in progress?: has less than 3 attempts (3 is the maximum number of attempts)
         *
         * check if submittedContent is correct
         * - correct? must have valid content (can be jumbled, but should have all words)
         *
         * if feedback is processable, provide feedback
         * - feedback can be either from db (which is stored from previously attempted AI feedback)
         *   or newly generated from AI model
         */
    }
}
