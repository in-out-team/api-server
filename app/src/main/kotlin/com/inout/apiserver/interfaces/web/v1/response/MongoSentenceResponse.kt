package com.inout.apiserver.interfaces.web.v1.response

import com.inout.apiserver.infrastructure.mongo.word.MongoSentence
import org.bson.types.ObjectId

data class MongoSentenceResponse(
    val id: ObjectId,
    val content: String,
    val translation: String,
    val lexicalCategories: List<MongoSentence.LexicalCategoryInfo>,
) {
    companion object {
        fun of(sentence: MongoSentence): MongoSentenceResponse =
            MongoSentenceResponse(
                id = sentence.id!!,
                content = sentence.content,
                translation = sentence.translation,
                lexicalCategories = sentence.lexicalCategories,
            )
    }
}
