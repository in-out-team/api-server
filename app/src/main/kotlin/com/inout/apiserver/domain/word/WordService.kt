package com.inout.apiserver.domain.word

import com.inout.apiserver.base.enums.LanguageType
import com.inout.apiserver.base.enums.LexicalCategoryType
import com.inout.apiserver.base.enums.SenderType
import com.inout.apiserver.base.enums.SentenceType
import com.inout.apiserver.base.enums.StatusType
import com.inout.apiserver.error.BadRequestException
import com.inout.apiserver.error.ConflictException
import com.inout.apiserver.error.NotFoundException
import com.inout.apiserver.infrastructure.mongo.user.User
import com.inout.apiserver.infrastructure.mongo.word.AiAudio
import com.inout.apiserver.infrastructure.mongo.word.Conversation
import com.inout.apiserver.infrastructure.mongo.word.ConversationMessage
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
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class WordService(
    private val wordRepository: WordRepository,
    private val sentenceRepository: SentenceRepository,
    private val userSentenceRepository: UserSentenceRepository,
    private val userSentenceFeedbackRepository: UserSentenceFeedbackRepository,
    private val sentenceFeedbackRepository: SentenceFeedbackRepository,
    private val conversationRepository: ConversationRepository,
) {
    fun getWordWithDefinitionsBy(
        name: String,
        fromLanguage: LanguageType,
        toLanguage: LanguageType,
    ): WordWithDefinitions? =
        wordRepository.findByNameAndFromLanguageAndToLanguage(
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

    fun getWordWithDefinitionsBy(id: ObjectId): WordWithDefinitions? = wordRepository.findById(id = id)

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

        val newWord = wordRepository.saveWord(Word.fromCreateObject(wordCreateObject))
        val newWordDefinitions =
            wordCreateObject.definitions
                .map {
                    WordDefinition.fromCreateObject(
                        WordDefinitionCreateObject(
                            lexicalCategory = it.lexicalCategory,
                            meaning = it.meaning,
                            preContext = it.preContext,
                        ),
                        wordId = newWord.id!!,
                    )
                }.map { newWordDefinition ->
                    wordRepository.saveWordDefinition(newWordDefinition)
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
        lexicalCategories: List<Sentence.LexicalCategoryInfo>,
    ): Sentence =
        sentenceRepository.save(
            Sentence.fromCreateObject(
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
        wordRepository.findAllWithLiveDefinitionsBy(
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
        wordRepository.findAllWithDefinitionsBy(
            fromLanguage = fromLanguage,
            toLanguage = toLanguage,
            prefix = prefix,
            lexicalCategory = lexicalCategory,
            pageable = pageable,
        )

    fun getWordByLiveWordDefinitionId(wordDefinitionId: ObjectId): WordWithDefinitions =
        wordRepository
            .findByLiveDefinitionsId(wordDefinitionId)
            ?: throw NotFoundException(message = "Word Definition not found", code = "WORD_2")

    fun getWordsByLiveWordDefinitionIds(wordDefinitionIds: List<ObjectId>): List<WordWithDefinitions> =
        wordRepository.findAllByLiveDefinitionsIds(wordDefinitionIds)

    fun getSentencesByWordDefinitionId(wordDefinitionId: ObjectId): List<Sentence> =
        sentenceRepository.findAllByWordDefinitionId(wordDefinitionId)

    fun getSentencesByWordDefinitionIdAndType(
        wordDefinitionId: ObjectId,
        type: SentenceType,
    ): List<Sentence> =
        sentenceRepository.findAllByWordDefinitionIdAndType(
            wordDefinitionId = wordDefinitionId,
            type = type,
        )

    fun createUserSentence(
        userId: ObjectId,
        wordDefinitionId: ObjectId,
        type: SentenceType,
        sentenceId: ObjectId,
    ): UserSentence {
        userSentenceRepository
            .findByUserIdAndSentenceId(
                userId = userId,
                sentenceId = sentenceId,
            )?.let {
                throw ConflictException(message = "User Sentence already exists", code = "SENTENCE_2")
            }

        return userSentenceRepository.save(
            UserSentence.fromCreateObject(
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
    ): Sentence? =
        sentenceRepository.findByIdAndType(
            id = id,
            type = type,
        )

    fun getUserSentenceBy(
        userId: ObjectId,
        sentenceId: ObjectId,
    ): UserSentence? =
        userSentenceRepository.findByUserIdAndSentenceId(
            userId = userId,
            sentenceId = sentenceId,
        )

    fun getUserSentencesBy(
        userId: ObjectId,
        wordDefinitionId: ObjectId,
        type: SentenceType,
    ): List<UserSentence> =
        userSentenceRepository.findAllByUserIdAndWordDefinitionIdAndType(
            userId = userId,
            wordDefinitionId = wordDefinitionId,
            type = type,
        )

    fun deleteUserSentence(userSentence: UserSentence) {
        userSentenceRepository.delete(userSentence)
    }

    // TODO: check this method & add test case
    fun loadUserSentencesForReading(
        userId: ObjectId,
        sentences: List<Sentence>,
    ): List<UserSentence> {
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
            userSentenceRepository.findAllByUserIdAndWordDefinitionIdAndType(
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

    fun getUserSentenceFeedbacks(userSentenceId: ObjectId): List<UserSentenceFeedback> =
        userSentenceFeedbackRepository.findAllByUserSentenceId(userSentenceId)

    fun getSentenceFeedbackBy(
        sentenceId: ObjectId,
        submittedContent: String,
    ): SentenceFeedback? =
        sentenceFeedbackRepository.findBySentenceIdAndSubmittedContent(
            sentenceId = sentenceId,
            submittedContent = submittedContent,
        )

    fun createSentenceFeedback(
        sentenceId: ObjectId,
        submittedContent: String,
        feedback: String,
    ): SentenceFeedback =
        sentenceFeedbackRepository.save(
            SentenceFeedback.fromCreateObject(
                sentenceId = sentenceId,
                submittedContent = submittedContent,
                feedback = feedback,
            ),
        )

    fun createUserSentenceFeedback(
        userSentenceId: ObjectId,
        sentenceFeedbackId: ObjectId,
    ): UserSentenceFeedback =
        userSentenceFeedbackRepository.save(
            UserSentenceFeedback.fromCreateObject(
                userSentenceId = userSentenceId,
                sentenceFeedbackId = sentenceFeedbackId,
            ),
        )

    fun getSentenceFeedbacksByIds(sentenceFeedbackIds: List<ObjectId>): List<SentenceFeedback> =
        sentenceFeedbackRepository.findAllByIdIn(sentenceFeedbackIds)

    fun getConversationById(conversationId: ObjectId): ConversationWithMessages? = conversationRepository.findById(conversationId)

    fun getConversationsBy(
        user: User,
        wordDefinitionId: ObjectId,
    ): List<ConversationWithMessages> =
        conversationRepository
            .findAllByUserIdAndWordDefinitionId(
                userId = user.id!!,
                wordDefinitionId = wordDefinitionId,
            ).sortedBy { it.createdAt }

    fun createConversation(
        userId: ObjectId,
        wordDefinitionId: ObjectId,
        systemMessage: String,
        systemAudio: AiAudio,
    ): ConversationWithMessages {
        val conversation =
            conversationRepository.saveConversation(
                Conversation.fromCreateObject(
                    userId = userId,
                    wordDefinitionId = wordDefinitionId,
                ),
            )

        conversationRepository.saveConversationMessage(
            ConversationMessage(
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
        systemResponseAudio: AiAudio,
    ): ConversationWithMessages {
        // 1. Add user message
        conversationRepository.saveConversationMessage(
            ConversationMessage(
                conversationId = conversation.id!!,
                sender = SenderType.USER,
                content = userResponseMessage,
            ),
        )

        // 2. Add system message
        conversationRepository.saveConversationMessage(
            ConversationMessage(
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

        wordRepository.updateWordDefinitionStatus(
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

        wordRepository.updateWordDefinitionStatus(
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
            WordDefinition.fromCreateObject(
                WordDefinitionCreateObject(
                    lexicalCategory = lexicalCategory,
                    meaning = meaning,
                    preContext = preContext,
                ),
                wordId = word.id!!,
            )

        wordRepository.saveWordDefinition(newWordDefinition)

        return getWordWithDefinitionsBy(id = word.id)!!
    }
}
