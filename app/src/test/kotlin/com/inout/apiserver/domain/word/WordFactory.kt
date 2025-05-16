package com.inout.apiserver.domain.word

import com.inout.apiserver.base.enums.LanguageType
import com.inout.apiserver.base.enums.LexicalCategoryType
import com.inout.apiserver.base.enums.SentenceType
import com.inout.apiserver.base.enums.StatusType
import com.inout.apiserver.infrastructure.mongo.user.User
import com.inout.apiserver.infrastructure.mongo.word.Conversation
import com.inout.apiserver.infrastructure.mongo.word.ConversationRepository
import com.inout.apiserver.infrastructure.mongo.word.Sentence
import com.inout.apiserver.infrastructure.mongo.word.SentenceFeedback
import com.inout.apiserver.infrastructure.mongo.word.SentenceFeedbackRepository
import com.inout.apiserver.infrastructure.mongo.word.SentenceRepository
import com.inout.apiserver.infrastructure.mongo.word.UserSentence
import com.inout.apiserver.infrastructure.mongo.word.UserSentenceFeedback
import com.inout.apiserver.infrastructure.mongo.word.UserSentenceFeedbackRepository
import com.inout.apiserver.infrastructure.mongo.word.UserSentenceRepository
import com.inout.apiserver.infrastructure.mongo.word.Word
import com.inout.apiserver.infrastructure.mongo.word.WordDefinition
import com.inout.apiserver.infrastructure.mongo.word.WordRepository
import org.bson.types.ObjectId
import org.springframework.stereotype.Component

@Component
class WordFactory(
    private val wordRepository: WordRepository,
    private val sentenceRepository: SentenceRepository,
    private val conversationRepository: ConversationRepository,
    private val userSentenceRepository: UserSentenceRepository,
    private val userSentenceFeedbackRepository: UserSentenceFeedbackRepository,
    private val sentenceFeedbackRepository: SentenceFeedbackRepository,
) {
    fun createWord(
        name: String = "book",
        fromLanguage: LanguageType = LanguageType.ENGLISH,
        toLanguage: LanguageType = LanguageType.KOREAN,
        definitions: List<WordWithDefinitions.WordDefinition> =
            listOf(
                WordWithDefinitions.WordDefinition(
                    lexicalCategory = LexicalCategoryType.NOUN,
                    meaning = "책",
                    preContext = "정보를 얻거나 즐거움을 얻기 위해 읽는 인쇄물",
                    status = StatusType.LIVE,
                ),
            ),
    ): WordWithDefinitions {
        val newWord =
            wordRepository.saveWord(
                Word(
                    name = name,
                    fromLanguage = fromLanguage,
                    toLanguage = toLanguage,
                ),
            )

        val newWordDefinitions =
            definitions
                .map {
                    WordDefinition(
                        wordId = newWord.id!!,
                        lexicalCategory = it.lexicalCategory,
                        meaning = it.meaning,
                        preContext = it.preContext,
                        status = it.status,
                    )
                }.map {
                    wordRepository.saveWordDefinition(it)
                }

        return WordWithDefinitions.of(
            word = newWord,
            definitions = newWordDefinitions,
        )
    }

    fun createSentence(
        wordDefinitionId: ObjectId,
        content: String = "I read a book",
        translation: String = "나는 책을 읽었다",
        lexicalCategories: List<Sentence.LexicalCategoryInfo> =
            listOf(
                Sentence.LexicalCategoryInfo(
                    word = "I",
                    lexicalCategory = LexicalCategoryType.PRONOUN,
                ),
                Sentence.LexicalCategoryInfo(
                    word = "read",
                    lexicalCategory = LexicalCategoryType.VERB,
                ),
                Sentence.LexicalCategoryInfo(
                    word = "book",
                    lexicalCategory = LexicalCategoryType.NOUN,
                ),
            ),
        type: SentenceType = SentenceType.READING,
    ): Sentence =
        sentenceRepository.save(
            Sentence(
                wordDefinitionId = wordDefinitionId,
                type = type,
                content = content,
                translation = translation,
                lexicalCategories = lexicalCategories,
            ),
        )

    fun createConversation(
        user: User,
        wordDefinitionId: ObjectId,
    ): Conversation =
        conversationRepository.saveConversation(
            Conversation(
                userId = user.id!!,
                wordDefinitionId = wordDefinitionId,
            ),
        )

    fun createUserSentence(
        userId: ObjectId,
        wordDefinitionId: ObjectId,
        sentenceId: ObjectId,
        type: SentenceType = SentenceType.WRITING,
    ): UserSentence =
        userSentenceRepository
            .save(
                UserSentence(
                    userId = userId,
                    wordDefinitionId = wordDefinitionId,
                    sentenceId = sentenceId,
                    type = type,
                ),
            )

    fun createUserSentenceFeedback(
        userSentenceId: ObjectId,
        sentenceFeedbackId: ObjectId,
    ): UserSentenceFeedback =
        userSentenceFeedbackRepository
            .save(
                UserSentenceFeedback(
                    userSentenceId = userSentenceId,
                    sentenceFeedbackId = sentenceFeedbackId,
                ),
            )

    fun createSentenceFeedback(
        sentenceId: ObjectId,
        submittedContent: String = "I read book",
        feedback: String = "주어와 동사 사이에 'a'를 넣어야 합니다. 'a'는 무언가 특정한 책을 가리키는데 도움을 줍니다. 모호함을 없애고 명확한 문장을 만들기 위해 필요한 내용입니다.",
    ): SentenceFeedback =
        sentenceFeedbackRepository
            .save(
                SentenceFeedback(
                    sentenceId = sentenceId,
                    submittedContent = submittedContent,
                    feedback = feedback,
                ),
            ).let {
                sentenceFeedbackRepository.findById(it.id!!).get()
            }
}
