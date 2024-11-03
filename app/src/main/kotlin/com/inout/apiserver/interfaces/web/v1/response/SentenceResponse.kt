package com.inout.apiserver.interfaces.web.v1.response

import com.inout.apiserver.domain.word.Sentence

data class SentenceResponse(
    val id: Long,
    val content: String,
) {
    companion object {
        fun of(sentence: Sentence): SentenceResponse {
            return SentenceResponse(
                id = sentence.id,
                content = sentence.content,
            )
        }
    }
}
