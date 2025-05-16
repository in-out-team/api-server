package com.inout.apiserver.interfaces.web.v1.response

import com.inout.apiserver.infrastructure.mongo.word.Sentence

data class SentencesResponse(
    val selectedSentences: List<SentenceResponse>,
    val notSelectedSentences: List<SentenceResponse>,
) {
    companion object {
        fun of(
            selectedSentences: List<Sentence>,
            unselectedSentences: List<Sentence>,
        ): SentencesResponse =
            SentencesResponse(
                selectedSentences = selectedSentences.map { SentenceResponse.of(it) },
                notSelectedSentences = unselectedSentences.map { SentenceResponse.of(it) },
            )
    }
}
