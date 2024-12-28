package com.inout.apiserver.interfaces.web.v1.response

import com.inout.apiserver.domain.word.SentenceFeedback

data class WritingSentenceFeedbacksResponse(
    val feedbacks: List<Feedback>,
) {
    data class Feedback(
        val submittedContent: String,
        val feedback: String,
    )

    companion object {
        fun of(sentenceFeedbacks: List<SentenceFeedback>): WritingSentenceFeedbacksResponse =
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
