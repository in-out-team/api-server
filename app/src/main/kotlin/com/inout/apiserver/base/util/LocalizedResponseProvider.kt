package com.inout.apiserver.base.util

import com.inout.apiserver.base.enums.LanguageType
import org.springframework.stereotype.Component

@Component
class LocalizedResponseProvider {
    fun getCorrectAnswerFeedback(studyLanguage: LanguageType) =
        when (studyLanguage) {
            LanguageType.ENGLISH -> "Your answer is correct!"
            LanguageType.KOREAN -> "정답입니다!"
        }

    fun getInitialConversationPrompt(
        studyLanguage: LanguageType,
        wordName: String,
    ) = when (studyLanguage) {
        LanguageType.ENGLISH -> "Let's talk about $wordName!"
        LanguageType.KOREAN -> "${wordName}에 대해 이야기 해볼까요?"
    }
}
