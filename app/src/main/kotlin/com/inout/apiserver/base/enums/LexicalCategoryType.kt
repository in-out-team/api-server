package com.inout.apiserver.base.enums

enum class LexicalCategoryType {
    NOUN,
    VERB,
    ADJECTIVE,
    ADVERB,
    PRONOUN,
    PREPOSITION,
    CONJUNCTION,
    INTERJECTION,
    ARTICLE,
    ;

    companion object {
        fun of(value: String): LexicalCategoryType =
            when (value.lowercase()) {
                "noun" -> NOUN
                "verb" -> VERB
                "adjective" -> ADJECTIVE
                "adverb" -> ADVERB
                "pronoun" -> PRONOUN
                "preposition" -> PREPOSITION
                "conjunction" -> CONJUNCTION
                "interjection" -> INTERJECTION
                "article" -> ARTICLE
                else -> throw IllegalArgumentException("Unknown LexicalCategoryType: $value")
            }
    }
}
