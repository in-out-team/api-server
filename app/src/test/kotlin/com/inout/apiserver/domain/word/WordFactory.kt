package com.inout.apiserver.domain.word

import com.inout.apiserver.base.alias.WordDefinitionId
import com.inout.apiserver.base.alias.WordId
import com.inout.apiserver.base.enums.LanguageType
import com.inout.apiserver.base.enums.LexicalCategoryType
import com.inout.apiserver.base.enums.SentenceType
import com.inout.apiserver.infrastructure.db.word.SentenceEntity
import com.inout.apiserver.infrastructure.db.word.SentenceFeedbackEntity
import com.inout.apiserver.infrastructure.db.word.SentenceFeedbackRepository
import com.inout.apiserver.infrastructure.db.word.SentenceRepository
import com.inout.apiserver.infrastructure.db.word.UserSentenceEntity
import com.inout.apiserver.infrastructure.db.word.UserSentenceFeedbackEntity
import com.inout.apiserver.infrastructure.db.word.UserSentenceFeedbackRepository
import com.inout.apiserver.infrastructure.db.word.UserSentenceRepository
import com.inout.apiserver.infrastructure.db.word.Word
import com.inout.apiserver.infrastructure.db.word.WordDefinition
import com.inout.apiserver.infrastructure.db.word.WordRepository
import org.springframework.stereotype.Component

@Component
class WordFactory(
    private val wordRepository: WordRepository,
    private val sentenceRepository: SentenceRepository,
    private val sentenceFeedbackRepository: SentenceFeedbackRepository,
    private val userSentenceRepository: UserSentenceRepository,
    private val userSentenceFeedbackRepository: UserSentenceFeedbackRepository,
) {
    companion object {
        @Deprecated("Use member function createWord instead")
        fun createWord(
            id: WordId = 1L,
            name: String = "book",
            fromLanguage: LanguageType = LanguageType.ENGLISH,
            toLanguage: LanguageType = LanguageType.KOREAN,
            lexicalCategory: LexicalCategoryType = LexicalCategoryType.NOUN,
            meaning: String = "책",
            preContext: String = "정보를 얻거나 즐거움을 얻기 위해 읽는 인쇄물",
        ): Word {
            val wordDefinition =
                WordDefinition(
                    id = id,
                    lexicalCategory = lexicalCategory,
                    meaning = meaning,
                    preContext = preContext,
                )
            return Word(
                id = id,
                name = name,
                fromLanguage = fromLanguage,
                toLanguage = toLanguage,
                definitions = listOf(wordDefinition),
            )
        }
    }

    fun createWord(
        name: String = "book",
        fromLanguage: LanguageType = LanguageType.ENGLISH,
        toLanguage: LanguageType = LanguageType.KOREAN,
        wordDefinitions: List<WordDefinition> =
            listOf(
                WordDefinition(
                    lexicalCategory = LexicalCategoryType.NOUN,
                    meaning = "책",
                    preContext = "정보를 얻거나 즐거움을 얻기 위해 읽는 인쇄물",
                ),
            ),
    ): Word =
        wordRepository.save(
            Word(
                name = name,
                fromLanguage = fromLanguage,
                toLanguage = toLanguage,
                definitions = wordDefinitions,
            ),
        )

    fun createSentence(
        wordDefinitionId: WordDefinitionId,
        content: String = "I read a book",
        translation: String = "나는 책을 읽었다",
        lexicalCategories: List<SentenceEntity.LexicalCategoryInfo> =
            listOf(
                SentenceEntity.LexicalCategoryInfo(
                    word = "I",
                    lexicalCategory = LexicalCategoryType.PRONOUN,
                ),
                SentenceEntity.LexicalCategoryInfo(
                    word = "read",
                    lexicalCategory = LexicalCategoryType.VERB,
                ),
                SentenceEntity.LexicalCategoryInfo(
                    word = "a",
                    lexicalCategory = LexicalCategoryType.ARTICLE,
                ),
                SentenceEntity.LexicalCategoryInfo(
                    word = "book",
                    lexicalCategory = LexicalCategoryType.NOUN,
                ),
            ),
        type: SentenceType = SentenceType.READING,
    ): Sentence =
        sentenceRepository.save(
            SentenceEntity(
                wordDefinitionId = wordDefinitionId,
                type = type,
                content = content,
                translation = translation,
                lexicalCategories = lexicalCategories,
            ),
        )

    fun createSentenceFeedback(
        sentenceId: Long,
        submittedContent: String = "I read book",
        feedback: String = "주어와 동사 사이에 'a'를 넣어야 합니다. 'a'는 무언가 특정한 책을 가리키는데 도움을 줍니다. 모호함을 없애고 명확한 문장을 만들기 위해 필요한 내용입니다.",
    ) = sentenceFeedbackRepository.save(
        SentenceFeedbackEntity(
            sentenceId = sentenceId,
            submittedContent = submittedContent,
            feedback = feedback,
        ),
    )

    fun createUserSentence(
        userId: Long,
        wordDefinitionId: Long,
        sentenceId: Long,
        type: SentenceType = SentenceType.WRITING,
    ) = userSentenceRepository.save(
        UserSentenceEntity(
            userId = userId,
            wordDefinitionId = wordDefinitionId,
            type = type,
            sentenceId = sentenceId,
        ),
    )

    fun createUserSentenceFeedback(
        userSentenceId: Long,
        sentenceFeedbackId: Long,
    ) = userSentenceFeedbackRepository.save(
        UserSentenceFeedbackEntity(
            userSentenceId = userSentenceId,
            sentenceFeedbackId = sentenceFeedbackId,
        ),
    )
}
