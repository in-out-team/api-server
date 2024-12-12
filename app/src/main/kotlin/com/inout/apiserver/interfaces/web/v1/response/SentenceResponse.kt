package com.inout.apiserver.interfaces.web.v1.response

import com.inout.apiserver.domain.word.Sentence
import com.inout.apiserver.infrastructure.db.word.SentenceEntity

data class SentenceResponse(
    val id: Long,
    val content: String,
    val translation: String,
    // TODO: using SentenceEntity.LexicalCategoryInfo is not preferred, it should be replaced with a DTO or
    //   a common interface from base
    val lexicalCategories: List<SentenceEntity.LexicalCategoryInfo>,
) {
    companion object {
        fun of(sentence: Sentence): SentenceResponse =
            SentenceResponse(
                id = sentence.id,
                content = sentence.content,
                translation = sentence.translation,
                lexicalCategories = sentence.lexicalCategories,
            )
    }
}
