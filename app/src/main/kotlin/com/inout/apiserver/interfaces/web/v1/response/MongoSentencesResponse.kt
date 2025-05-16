package com.inout.apiserver.interfaces.web.v1.response

import com.inout.apiserver.infrastructure.mongo.word.MongoSentence

data class MongoSentencesResponse(
    val selectedSentences: List<MongoSentenceResponse>,
    val notSelectedSentences: List<MongoSentenceResponse>,
) {
    companion object {
        fun of(
            selectedSentences: List<MongoSentence>,
            unselectedSentences: List<MongoSentence>,
        ): MongoSentencesResponse =
            MongoSentencesResponse(
                selectedSentences = selectedSentences.map { MongoSentenceResponse.of(it) },
                notSelectedSentences = unselectedSentences.map { MongoSentenceResponse.of(it) },
            )
    }
}
