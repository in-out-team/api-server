package com.inout.apiserver.interfaces.web.v1.response

import com.inout.apiserver.infrastructure.mongo.word.MongoSentenceFeedback

data class WritingSentenceFeedbacksResponse(
    val feedbacks: List<Feedback>,
) {
    data class Feedback(
        val submittedContent: String,
        val feedback: String,
    )

    companion object {
        fun of(sentenceFeedbacks: List<MongoSentenceFeedback>): WritingSentenceFeedbacksResponse =
            WritingSentenceFeedbacksResponse(
                feedbacks =
                    sentenceFeedbacks.map {
                        Feedback(
                            submittedContent = it.submittedContent,
                            feedback = it.feedback,
                        )
                    },
            )
    }
}
