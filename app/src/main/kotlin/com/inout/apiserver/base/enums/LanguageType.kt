package com.inout.apiserver.base.enums

enum class LanguageType {
    ENGLISH,
    KOREAN,
    ;

    val twoLetterCode: String
        get() =
            when (this) {
                ENGLISH -> "en"
                KOREAN -> "ko"
            }
}
