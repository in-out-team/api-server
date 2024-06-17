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
    UNKNOWN;

    companion object {
        fun of(value: String): LexicalCategoryType {
            return when (value.lowercase()) {
                "noun" -> NOUN
                "verb" -> VERB
                "adjective" -> ADJECTIVE
                "adverb" -> ADVERB
                "pronoun" -> PRONOUN
                "preposition" -> PREPOSITION
                "conjunction" -> CONJUNCTION
                "interjection" -> INTERJECTION
                else -> UNKNOWN
            }
        }
    }
}
