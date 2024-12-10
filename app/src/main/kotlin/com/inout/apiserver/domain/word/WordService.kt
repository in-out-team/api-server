package com.inout.apiserver.domain.word

import com.inout.apiserver.base.enums.LanguageType
import com.inout.apiserver.base.enums.LexicalCategoryType
import com.inout.apiserver.base.enums.SentenceType
import com.inout.apiserver.error.BadRequestException
import com.inout.apiserver.error.ConflictException
import com.inout.apiserver.error.NotFoundException
import com.inout.apiserver.infrastructure.db.word.SentenceEntity
import com.inout.apiserver.infrastructure.db.word.SentenceRepository
import com.inout.apiserver.infrastructure.db.word.UserSentenceEntity
import com.inout.apiserver.infrastructure.db.word.UserSentenceRepository
import com.inout.apiserver.infrastructure.db.word.WordEntity
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
) {
    fun getWordByNameAndFromLanguageAndToLanguage(
        name: String,
        fromLanguage: LanguageType,
        toLanguage: LanguageType,
    ): Word? = wordRepository.findByNameAndFromLanguageAndToLanguage(name, fromLanguage, toLanguage)

    fun getWordById(id: Long): Word? = wordRepository.findById(id)

    @Transactional
    fun createWord(wordCreateObject: WordCreateObject): Word {
        getWordByNameAndFromLanguageAndToLanguage(
            name = wordCreateObject.name,
            fromLanguage = wordCreateObject.fromLanguage,
            toLanguage = wordCreateObject.toLanguage,
        )?.let {
            throw ConflictException(message = "Word already exists", code = "WORD_1")
        }

        return wordRepository.save(WordEntity.fromCreateObject(wordCreateObject))
    }

    @Transactional
    fun createSentence(sentenceCreateObject: SentenceCreateObject): Sentence =
        sentenceRepository.save(SentenceEntity.fromCreateObject(sentenceCreateObject))

    fun getWordsWithDefinitions(
        fromLanguage: LanguageType,
        toLanguage: LanguageType,
        prefix: String,
        lexicalCategory: LexicalCategoryType?,
        pageable: Pageable,
    ): Page<Word> = wordRepository.findWordsWithDefinitions(fromLanguage, toLanguage, prefix, lexicalCategory, pageable)

    fun getWordByWordDefinitionId(wordDefinitionId: Long): Word =
        wordRepository
            .findByWordDefinitionId(wordDefinitionId)
            ?.let { word -> word.copy(definitions = word.definitions.filter { it.id == wordDefinitionId }) }
            ?: throw NotFoundException(message = "Word Definition not found", code = "WORD_2")

    fun getWordsByWordDefinitionIds(wordDefinitionIds: List<Long>): List<Word> {
        val ids = wordDefinitionIds.toSet()
        return wordRepository
            .findAllByWordDefinitionIds(wordDefinitionIds)
            .map { word -> word.copy(definitions = word.definitions.filter { it.id in ids }) }
    }

    fun getSentencesByWordDefinitionId(wordDefinitionId: Long): List<Sentence> =
        sentenceRepository.findAllByWordDefinitionId(wordDefinitionId)

    fun getSentencesByWordDefinitionIdAndType(
        wordDefinitionId: Long,
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
        id: Long,
        type: SentenceType,
    ): Sentence? = sentenceRepository.findById(id)?.takeIf { it.type == type }

    fun getUserSentenceBy(
        userId: Long,
        sentenceId: Long,
    ): UserSentence? = userSentenceRepository.findByUserIdAndSentenceId(userId, sentenceId)

    fun getUserSentencesBy(
        userId: Long,
        wordDefinitionId: Long,
    ): List<UserSentence> = userSentenceRepository.findAllByUserIdAndWordDefinitionId(userId, wordDefinitionId)

    fun deleteUserSentence(userSentence: UserSentence) {
        userSentenceRepository.delete(UserSentenceEntity.of(userSentence))
    }

    fun loadUserSentences(
        userId: Long,
        sentences: List<Sentence>,
    ): List<UserSentence> {
        // TODO:
        //  - need to add test case
        //  - need to add validation for sentences (size > 0)
        if (sentences.map { it.wordDefinitionId }.distinct().size != 1) {
            throw BadRequestException(
                message = "Cannot load user sentences for different word definitions",
                code = "SENTENCE_4",
            )
        }

        val userSentences =
            userSentenceRepository.findAllByUserIdAndWordDefinitionId(
                userId = userId,
                wordDefinitionId = sentences.first().wordDefinitionId,
            )
        if (userSentences.isNotEmpty()) {
            return userSentences
        }

        return sentences.map { sentence ->
            createUserSentence(
                UserSentenceCreateObject(
                    userId = userId,
                    wordDefinitionId = sentence.wordDefinitionId,
                    sentenceId = sentence.id,
                ),
            )
        }
    }
}
