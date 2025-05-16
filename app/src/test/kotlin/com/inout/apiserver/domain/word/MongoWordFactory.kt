package com.inout.apiserver.domain.word

import com.inout.apiserver.base.enums.LanguageType
import com.inout.apiserver.base.enums.LexicalCategoryType
import com.inout.apiserver.base.enums.SentenceType
import com.inout.apiserver.base.enums.StatusType
import com.inout.apiserver.infrastructure.mongo.user.MongoUser
import com.inout.apiserver.infrastructure.mongo.word.MongoConversation
import com.inout.apiserver.infrastructure.mongo.word.MongoConversationRepository
import com.inout.apiserver.infrastructure.mongo.word.MongoSentence
import com.inout.apiserver.infrastructure.mongo.word.MongoSentenceRepository
import com.inout.apiserver.infrastructure.mongo.word.MongoUserSentence
import com.inout.apiserver.infrastructure.mongo.word.MongoUserSentenceFeedback
import com.inout.apiserver.infrastructure.mongo.word.MongoUserSentenceFeedbackRepository
import com.inout.apiserver.infrastructure.mongo.word.MongoUserSentenceRepository
import com.inout.apiserver.infrastructure.mongo.word.MongoWord
import com.inout.apiserver.infrastructure.mongo.word.MongoWordDefinition
import com.inout.apiserver.infrastructure.mongo.word.MongoWordRepository
import org.bson.types.ObjectId
import org.springframework.stereotype.Component

@Component
class MongoWordFactory(
    private val wordRepository: MongoWordRepository,
    private val sentenceRepository: MongoSentenceRepository,
    private val conversationRepository: MongoConversationRepository,
    private val mongoUserSentenceRepository: MongoUserSentenceRepository,
    private val mongoUserSentenceFeedbackRepository: MongoUserSentenceFeedbackRepository,
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
                MongoWord(
                    name = name,
                    fromLanguage = fromLanguage,
                    toLanguage = toLanguage,
                ),
            )

        val newWordDefinitions =
            definitions
                .map {
                    MongoWordDefinition(
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
        lexicalCategories: List<MongoSentence.LexicalCategoryInfo> =
            listOf(
                MongoSentence.LexicalCategoryInfo(
                    word = "I",
                    lexicalCategory = LexicalCategoryType.PRONOUN,
                ),
                MongoSentence.LexicalCategoryInfo(
                    word = "read",
                    lexicalCategory = LexicalCategoryType.VERB,
                ),
                MongoSentence.LexicalCategoryInfo(
                    word = "book",
                    lexicalCategory = LexicalCategoryType.NOUN,
                ),
            ),
        type: SentenceType = SentenceType.READING,
    ): MongoSentence =
        sentenceRepository.save(
            MongoSentence(
                wordDefinitionId = wordDefinitionId,
                type = type,
                content = content,
                translation = translation,
                lexicalCategories = lexicalCategories,
            ),
        )

    fun createConversation(
        user: MongoUser,
        wordDefinitionId: ObjectId,
    ): MongoConversation =
        conversationRepository.saveConversation(
            MongoConversation(
                userId = user.id!!,
                wordDefinitionId = wordDefinitionId,
            ),
        )

    fun createUserSentence(
        userId: ObjectId,
        wordDefinitionId: ObjectId,
        sentenceId: ObjectId,
        type: SentenceType = SentenceType.WRITING,
    ): MongoUserSentence =
        mongoUserSentenceRepository
            .save(
                MongoUserSentence(
                    userId = userId,
                    wordDefinitionId = wordDefinitionId,
                    sentenceId = sentenceId,
                    type = type,
                ),
            )

    fun createUserSentenceFeedback(
        userSentenceId: ObjectId,
        sentenceFeedbackId: ObjectId,
    ): MongoUserSentenceFeedback =
        mongoUserSentenceFeedbackRepository
            .save(
                MongoUserSentenceFeedback(
                    userSentenceId = userSentenceId,
                    sentenceFeedbackId = sentenceFeedbackId,
                ),
            )
}
