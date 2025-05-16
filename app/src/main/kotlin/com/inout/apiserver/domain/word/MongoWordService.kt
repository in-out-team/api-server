package com.inout.apiserver.domain.word

import com.inout.apiserver.base.enums.LanguageType
import com.inout.apiserver.base.enums.LexicalCategoryType
import com.inout.apiserver.base.enums.SenderType
import com.inout.apiserver.base.enums.SentenceType
import com.inout.apiserver.base.enums.StatusType
import com.inout.apiserver.error.BadRequestException
import com.inout.apiserver.error.ConflictException
import com.inout.apiserver.error.NotFoundException
import com.inout.apiserver.infrastructure.mongo.user.MongoUser
import com.inout.apiserver.infrastructure.mongo.word.MongoAiAudio
import com.inout.apiserver.infrastructure.mongo.word.MongoConversation
import com.inout.apiserver.infrastructure.mongo.word.MongoConversationMessage
import com.inout.apiserver.infrastructure.mongo.word.MongoConversationRepository
import com.inout.apiserver.infrastructure.mongo.word.MongoSentence
import com.inout.apiserver.infrastructure.mongo.word.MongoSentenceFeedback
import com.inout.apiserver.infrastructure.mongo.word.MongoSentenceFeedbackRepository
import com.inout.apiserver.infrastructure.mongo.word.MongoSentenceRepository
import com.inout.apiserver.infrastructure.mongo.word.MongoUserSentence
import com.inout.apiserver.infrastructure.mongo.word.MongoUserSentenceFeedback
import com.inout.apiserver.infrastructure.mongo.word.MongoUserSentenceFeedbackRepository
import com.inout.apiserver.infrastructure.mongo.word.MongoUserSentenceRepository
import com.inout.apiserver.infrastructure.mongo.word.MongoWord
import com.inout.apiserver.infrastructure.mongo.word.MongoWordDefinition
import com.inout.apiserver.infrastructure.mongo.word.MongoWordRepository
import org.bson.types.ObjectId
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class MongoWordService(
    private val mongoWordRepository: MongoWordRepository,
    private val mongoSentenceRepository: MongoSentenceRepository,
    private val mongoUserSentenceRepository: MongoUserSentenceRepository,
    private val mongoUserSentenceFeedbackRepository: MongoUserSentenceFeedbackRepository,
    private val mongoSentenceFeedbackRepository: MongoSentenceFeedbackRepository,
    private val mongoConversationRepository: MongoConversationRepository,
) {
    fun getWordWithDefinitionsBy(
        name: String,
        fromLanguage: LanguageType,
        toLanguage: LanguageType,
    ): WordWithDefinitions? =
        mongoWordRepository.findByNameAndFromLanguageAndToLanguage(
            name = name,
            fromLanguage = fromLanguage,
            toLanguage = toLanguage,
        )

    fun getWordWithLiveDefinitionsBy(
        name: String,
        fromLanguage: LanguageType,
        toLanguage: LanguageType,
    ): WordWithDefinitions? =
        getWordWithDefinitionsBy(name = name, fromLanguage = fromLanguage, toLanguage = toLanguage)
            ?.let { wordWithDefinitions ->
                wordWithDefinitions.copy(
                    definitions =
                        wordWithDefinitions.definitions.filter { it.status == StatusType.LIVE },
                )
            }

    fun getWordWithDefinitionsBy(id: ObjectId): WordWithDefinitions? = mongoWordRepository.findById(id = id)

    fun getWordWithLiveDefinitionsBy(id: ObjectId): WordWithDefinitions? =
        getWordWithDefinitionsBy(id = id)
            ?.let { wordWithDefinitions ->
                wordWithDefinitions.copy(
                    definitions =
                        wordWithDefinitions.definitions.filter { it.status == StatusType.LIVE },
                )
            }

    @Transactional
    fun createWord(wordCreateObject: WordCreateObject): WordWithDefinitions {
        getWordWithDefinitionsBy(
            name = wordCreateObject.name,
            fromLanguage = wordCreateObject.fromLanguage,
            toLanguage = wordCreateObject.toLanguage,
        )?.let {
            throw ConflictException(message = "Word already exists", code = "WORD_1")
        }

        val newWord = mongoWordRepository.saveWord(MongoWord.fromCreateObject(wordCreateObject))
        val newWordDefinitions =
            wordCreateObject.definitions
                .map {
                    MongoWordDefinition.fromCreateObject(
                        WordDefinitionCreateObject(
                            lexicalCategory = it.lexicalCategory,
                            meaning = it.meaning,
                            preContext = it.preContext,
                        ),
                        wordId = newWord.id!!,
                    )
                }.map { newWordDefinition ->
                    mongoWordRepository.saveWordDefinition(newWordDefinition)
                }

        return WordWithDefinitions.of(
            word = newWord,
            definitions = newWordDefinitions,
        )
    }

    fun createSentence(
        wordDefinitionId: ObjectId,
        type: SentenceType,
        sentence: String,
        translation: String,
        lexicalCategories: List<MongoSentence.LexicalCategoryInfo>,
    ): MongoSentence =
        mongoSentenceRepository.save(
            MongoSentence.fromCreateObject(
                wordDefinitionId = wordDefinitionId,
                type = type,
                content = sentence,
                translation = translation,
                lexicalCategories = lexicalCategories,
            ),
        )

    fun getWordsWithLiveDefinitions(
        fromLanguage: LanguageType,
        toLanguage: LanguageType,
        prefix: String,
        lexicalCategory: LexicalCategoryType?,
        pageable: Pageable,
    ): Page<WordWithDefinitions> =
        mongoWordRepository.findAllWithLiveDefinitionsBy(
            fromLanguage = fromLanguage,
            toLanguage = toLanguage,
            prefix = prefix,
            lexicalCategory = lexicalCategory,
            pageable = pageable,
        )

    fun getWordsWithDefinitions(
        fromLanguage: LanguageType,
        toLanguage: LanguageType,
        prefix: String,
        lexicalCategory: LexicalCategoryType?,
        pageable: Pageable,
    ): Page<WordWithDefinitions> =
        mongoWordRepository.findAllWithDefinitionsBy(
            fromLanguage = fromLanguage,
            toLanguage = toLanguage,
            prefix = prefix,
            lexicalCategory = lexicalCategory,
            pageable = pageable,
        )

    fun getWordByLiveWordDefinitionId(wordDefinitionId: ObjectId): WordWithDefinitions =
        mongoWordRepository
            .findByLiveDefinitionsId(wordDefinitionId)
            ?: throw NotFoundException(message = "Word Definition not found", code = "WORD_2")

    fun getWordsByLiveWordDefinitionIds(wordDefinitionIds: List<ObjectId>): List<WordWithDefinitions> =
        mongoWordRepository.findAllByLiveDefinitionsIds(wordDefinitionIds)

    fun getSentencesByWordDefinitionId(wordDefinitionId: ObjectId): List<MongoSentence> =
        mongoSentenceRepository.findAllByWordDefinitionId(wordDefinitionId)

    fun getSentencesByWordDefinitionIdAndType(
        wordDefinitionId: ObjectId,
        type: SentenceType,
    ): List<MongoSentence> =
        mongoSentenceRepository.findAllByWordDefinitionIdAndType(
            wordDefinitionId = wordDefinitionId,
            type = type,
        )

    fun createUserSentence(
        userId: ObjectId,
        wordDefinitionId: ObjectId,
        type: SentenceType,
        sentenceId: ObjectId,
    ): MongoUserSentence {
        mongoUserSentenceRepository
            .findByUserIdAndSentenceId(
                userId = userId,
                sentenceId = sentenceId,
            )?.let {
                throw ConflictException(message = "User Sentence already exists", code = "SENTENCE_2")
            }

        return mongoUserSentenceRepository.save(
            MongoUserSentence.fromCreateObject(
                userId = userId,
                wordDefinitionId = wordDefinitionId,
                type = type,
                sentenceId = sentenceId,
            ),
        )
    }

    fun getSentenceByIdAndType(
        id: ObjectId,
        type: SentenceType,
    ): MongoSentence? =
        mongoSentenceRepository.findByIdAndType(
            id = id,
            type = type,
        )

    fun getUserSentenceBy(
        userId: ObjectId,
        sentenceId: ObjectId,
    ): MongoUserSentence? =
        mongoUserSentenceRepository.findByUserIdAndSentenceId(
            userId = userId,
            sentenceId = sentenceId,
        )

    fun getUserSentencesBy(
        userId: ObjectId,
        wordDefinitionId: ObjectId,
        type: SentenceType,
    ): List<MongoUserSentence> =
        mongoUserSentenceRepository.findAllByUserIdAndWordDefinitionIdAndType(
            userId = userId,
            wordDefinitionId = wordDefinitionId,
            type = type,
        )

    fun deleteUserSentence(userSentence: MongoUserSentence) {
        mongoUserSentenceRepository.delete(userSentence)
    }

    // TODO: check this method & add test case
    fun loadUserSentencesForReading(
        userId: ObjectId,
        sentences: List<MongoSentence>,
    ): List<MongoUserSentence> {
        val wordDefinitionId =
            sentences
                .map { it.wordDefinitionId }
                .distinct()
                .takeIf { it.size == 1 }
                ?.first()
        if (wordDefinitionId == null || !sentences.all { it.type == SentenceType.READING }) {
            throw BadRequestException(
                message = "Cannot load user sentences for different word definitions or type",
                code = "SENTENCE_4",
            )
        }

        val userSentences =
            mongoUserSentenceRepository.findAllByUserIdAndWordDefinitionIdAndType(
                userId = userId,
                wordDefinitionId = wordDefinitionId,
                type = SentenceType.READING,
            )
        if (userSentences.isNotEmpty()) {
            return userSentences
        }

        return sentences.map { sentence ->
            createUserSentence(
                userId = userId,
                wordDefinitionId = sentence.wordDefinitionId,
                type = sentence.type,
                sentenceId = sentence.id!!,
            )
        }
    }

    fun getUserSentenceFeedbacks(userSentenceId: ObjectId): List<MongoUserSentenceFeedback> =
        mongoUserSentenceFeedbackRepository.findAllByUserSentenceId(userSentenceId)

    fun getSentenceFeedbackBy(
        sentenceId: ObjectId,
        submittedContent: String,
    ): MongoSentenceFeedback? =
        mongoSentenceFeedbackRepository.findBySentenceIdAndSubmittedContent(
            sentenceId = sentenceId,
            submittedContent = submittedContent,
        )

    fun createSentenceFeedback(
        sentenceId: ObjectId,
        submittedContent: String,
        feedback: String,
    ): MongoSentenceFeedback =
        mongoSentenceFeedbackRepository.save(
            MongoSentenceFeedback.fromCreateObject(
                sentenceId = sentenceId,
                submittedContent = submittedContent,
                feedback = feedback,
            ),
        )

    fun createUserSentenceFeedback(
        userSentenceId: ObjectId,
        sentenceFeedbackId: ObjectId,
    ): MongoUserSentenceFeedback =
        mongoUserSentenceFeedbackRepository.save(
            MongoUserSentenceFeedback.fromCreateObject(
                userSentenceId = userSentenceId,
                sentenceFeedbackId = sentenceFeedbackId,
            ),
        )

    fun getSentenceFeedbacksByIds(sentenceFeedbackIds: List<ObjectId>): List<MongoSentenceFeedback> =
        mongoSentenceFeedbackRepository.findAllByIdIn(sentenceFeedbackIds)

    fun getConversationById(conversationId: ObjectId): ConversationWithMessages? = mongoConversationRepository.findById(conversationId)

    fun getConversationsBy(
        user: MongoUser,
        wordDefinitionId: ObjectId,
    ): List<ConversationWithMessages> =
        mongoConversationRepository
            .findAllByUserIdAndWordDefinitionId(
                userId = user.id!!,
                wordDefinitionId = wordDefinitionId,
            ).sortedBy { it.createdAt }

    fun createConversation(
        userId: ObjectId,
        wordDefinitionId: ObjectId,
        systemMessage: String,
        systemAudio: MongoAiAudio,
    ): ConversationWithMessages {
        val conversation =
            mongoConversationRepository.saveConversation(
                MongoConversation.fromCreateObject(
                    userId = userId,
                    wordDefinitionId = wordDefinitionId,
                ),
            )

        mongoConversationRepository.saveConversationMessage(
            MongoConversationMessage(
                conversationId = conversation.id!!,
                sender = SenderType.SYSTEM,
                content = systemMessage,
                audio = systemAudio,
            ),
        )

        return getConversationById(conversation.id)!!
    }

    @Transactional
    fun doConversation(
        conversation: ConversationWithMessages,
        userResponseMessage: String,
        systemResponseMessage: String,
        systemResponseAudio: MongoAiAudio,
    ): ConversationWithMessages {
        // 1. Add user message
        mongoConversationRepository.saveConversationMessage(
            MongoConversationMessage(
                conversationId = conversation.id!!,
                sender = SenderType.USER,
                content = userResponseMessage,
            ),
        )

        // 2. Add system message
        mongoConversationRepository.saveConversationMessage(
            MongoConversationMessage(
                conversationId = conversation.id,
                sender = SenderType.SYSTEM,
                content = systemResponseMessage,
                audio = systemResponseAudio,
            ),
        )

        return getConversationById(conversation.id)!!
    }

    fun approveWordDefinition(
        word: WordWithDefinitions,
        wordDefinitionId: ObjectId,
    ): WordWithDefinitions {
        val targetWordDefinition =
            word.definitions.find { it.id == wordDefinitionId }
                ?: throw NotFoundException(
                    message = "Word definition not found",
                    code = "WORD_4",
                )
        if (targetWordDefinition.status != StatusType.PENDING) {
            throw BadRequestException(
                message = "Cannot approve word definition that is not pending",
                code = "WORD_6",
            )
        }

        mongoWordRepository.updateWordDefinitionStatus(
            wordDefinitionId = wordDefinitionId,
            status = StatusType.LIVE,
        )

        return getWordWithDefinitionsBy(id = word.id!!)!!
    }

    fun removeWordDefinition(
        word: WordWithDefinitions,
        wordDefinitionId: ObjectId,
    ): WordWithDefinitions {
        word.definitions.find { it.id == wordDefinitionId }
            ?: throw NotFoundException(
                message = "Word definition not found",
                code = "WORD_4",
            )

        mongoWordRepository.updateWordDefinitionStatus(
            wordDefinitionId = wordDefinitionId,
            status = StatusType.REMOVED,
        )

        return getWordWithDefinitionsBy(id = word.id!!)!!
    }

    fun addWordDefinition(
        word: WordWithDefinitions,
        lexicalCategory: LexicalCategoryType,
        meaning: String,
        preContext: String,
    ): WordWithDefinitions {
        val newWordDefinition =
            MongoWordDefinition.fromCreateObject(
                WordDefinitionCreateObject(
                    lexicalCategory = lexicalCategory,
                    meaning = meaning,
                    preContext = preContext,
                ),
                wordId = word.id!!,
            )

        mongoWordRepository.saveWordDefinition(newWordDefinition)

        return getWordWithDefinitionsBy(id = word.id)!!
    }
}
