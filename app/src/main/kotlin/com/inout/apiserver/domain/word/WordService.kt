package com.inout.apiserver.domain.word

import com.inout.apiserver.base.alias.SentenceId
import com.inout.apiserver.base.alias.UserId
import com.inout.apiserver.base.alias.UserSentenceId
import com.inout.apiserver.base.alias.WordDefinitionId
import com.inout.apiserver.base.alias.WordId
import com.inout.apiserver.base.enums.LanguageType
import com.inout.apiserver.base.enums.LexicalCategoryType
import com.inout.apiserver.base.enums.SentenceType
import com.inout.apiserver.error.BadRequestException
import com.inout.apiserver.error.ConflictException
import com.inout.apiserver.error.NotFoundException
import com.inout.apiserver.infrastructure.db.word.Sentence
import com.inout.apiserver.infrastructure.db.word.SentenceFeedbackEntity
import com.inout.apiserver.infrastructure.db.word.SentenceFeedbackRepository
import com.inout.apiserver.infrastructure.db.word.SentenceRepository
import com.inout.apiserver.infrastructure.db.word.UserSentenceEntity
import com.inout.apiserver.infrastructure.db.word.UserSentenceFeedbackEntity
import com.inout.apiserver.infrastructure.db.word.UserSentenceFeedbackRepository
import com.inout.apiserver.infrastructure.db.word.UserSentenceRepository
import com.inout.apiserver.infrastructure.db.word.Word
import com.inout.apiserver.infrastructure.db.word.WordRepository
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
) {
    fun getWordByNameAndFromLanguageAndToLanguage(
        name: String,
        fromLanguage: LanguageType,
        toLanguage: LanguageType,
    ): Word? = wordRepository.findByNameAndFromLanguageAndToLanguage(name, fromLanguage, toLanguage)

    fun getWordById(id: WordId): Word? = wordRepository.findById(id).orElse(null)

    @Transactional
    fun createWord(wordCreateObject: WordCreateObject): Word {
        getWordByNameAndFromLanguageAndToLanguage(
            name = wordCreateObject.name,
            fromLanguage = wordCreateObject.fromLanguage,
            toLanguage = wordCreateObject.toLanguage,
        )?.let {
            throw ConflictException(message = "Word already exists", code = "WORD_1")
        }

        return wordRepository.save(Word.fromCreateObject(wordCreateObject))
    }

    @Transactional
    fun createSentence(sentenceCreateObject: SentenceCreateObject): Sentence =
        sentenceRepository.save(Sentence.fromCreateObject(sentenceCreateObject))

    fun getWordsWithDefinitions(
        fromLanguage: LanguageType,
        toLanguage: LanguageType,
        prefix: String,
        lexicalCategory: LexicalCategoryType?,
        pageable: Pageable,
    ): Page<Word> = wordRepository.findWordsWithDefinitions(fromLanguage, toLanguage, prefix, lexicalCategory, pageable)

    fun getWordByWordDefinitionId(wordDefinitionId: WordDefinitionId): Word =
        wordRepository
            .findByWordDefinitionId(wordDefinitionId)
            ?.let { word -> word.copy(definitions = word.definitions.filter { it.id == wordDefinitionId }) }
            ?: throw NotFoundException(message = "Word Definition not found", code = "WORD_2")

    fun getWordsByWordDefinitionIds(wordDefinitionIds: List<WordDefinitionId>): List<Word> {
        val ids = wordDefinitionIds.toSet()
        return wordRepository
            .findAllByWordDefinitionIds(wordDefinitionIds)
            .map { word -> word.copy(definitions = word.definitions.filter { it.id in ids }) }
    }

    fun getSentencesByWordDefinitionId(wordDefinitionId: WordDefinitionId): List<Sentence> =
        sentenceRepository.findAllByWordDefinitionId(wordDefinitionId)

    fun getSentencesByWordDefinitionIdAndType(
        wordDefinitionId: WordDefinitionId,
        type: SentenceType,
    ): List<Sentence> = sentenceRepository.findAllByWordDefinitionIdAndType(wordDefinitionId, type)

    fun createUserSentence(userSentenceCreateObject: UserSentenceCreateObject): UserSentence {
        userSentenceRepository
            .findByUserIdAndSentenceId(
                userId = userSentenceCreateObject.userId,
                sentenceId = userSentenceCreateObject.sentenceId,
            )?.let {
                throw ConflictException(message = "User Sentence already exists", code = "SENTENCE_2")
            }

        return userSentenceRepository.save(UserSentenceEntity.fromCreateObject(userSentenceCreateObject))
    }

    fun getSentenceByIdAndType(
        id: SentenceId,
        type: SentenceType,
    ): Sentence? = sentenceRepository.findById(id).orElse(null)?.takeIf { it.type == type }

    fun getUserSentenceBy(
        userId: UserId,
        sentenceId: SentenceId,
    ): UserSentence? = userSentenceRepository.findByUserIdAndSentenceId(userId, sentenceId)

    fun getUserSentencesBy(
        userId: UserId,
        wordDefinitionId: WordDefinitionId,
        type: SentenceType,
    ): List<UserSentence> = userSentenceRepository.findAllByUserIdAndWordDefinitionIdAndType(userId, wordDefinitionId, type)

    fun deleteUserSentence(userSentence: UserSentence) {
        userSentenceRepository.delete(UserSentenceEntity.of(userSentence))
    }

    fun loadUserSentencesForReading(
        userId: UserId,
        sentences: List<Sentence>,
    ): List<UserSentence> {
        // TODO:
        //  - need to add test case
        //  - need to add validation for sentences (size > 0)
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
                UserSentenceCreateObject(
                    userId = userId,
                    wordDefinitionId = sentence.wordDefinitionId,
                    type = sentence.type,
                    sentenceId = sentence.id!!,
                ),
            )
        }
    }

    fun getUserSentenceFeedbacks(userSentenceId: UserSentenceId): List<UserSentenceFeedback> =
        userSentenceFeedbackRepository.findAllByUserSentenceId(userSentenceId)

    fun getSentenceFeedbackBy(
        sentenceId: SentenceId,
        submittedContent: String,
    ) = sentenceFeedbackRepository.findBySentenceIdAndSubmittedContent(sentenceId, submittedContent)

    fun createSentenceFeedback(sentenceFeedbackCreateObject: SentenceFeedbackCreateObject): SentenceFeedback =
        sentenceFeedbackRepository.save(SentenceFeedbackEntity.fromCreateObject(sentenceFeedbackCreateObject))

    fun createUserSentenceFeedback(userSentenceFeedbackCreateObject: UserSentenceFeedbackCreateObject): UserSentenceFeedback =
        userSentenceFeedbackRepository.save(UserSentenceFeedbackEntity.fromCreateObject(userSentenceFeedbackCreateObject))

    fun getSentenceFeedbacksByIds(sentenceFeedbackIds: List<Long>): List<SentenceFeedback> =
        sentenceFeedbackRepository.findAllByIdIn(sentenceFeedbackIds)
}
