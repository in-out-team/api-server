package com.inout.apiserver.interfaces.web.v1.response

import com.inout.apiserver.infrastructure.mongo.word.Sentence
import org.bson.types.ObjectId

data class SentenceResponse(
    val id: ObjectId,
    val content: String,
    val translation: String,
    val lexicalCategories: List<Sentence.LexicalCategoryInfo>,
) {
    companion object {
        fun of(sentence: Sentence): SentenceResponse =
            SentenceResponse(
                id = sentence.id!!,
                content = sentence.content,
                translation = sentence.translation,
                lexicalCategories = sentence.lexicalCategories,
            )
    }
}
