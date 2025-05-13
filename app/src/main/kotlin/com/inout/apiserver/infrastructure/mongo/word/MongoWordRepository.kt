package com.inout.apiserver.infrastructure.mongo.word

import com.inout.apiserver.base.enums.LanguageType
import com.inout.apiserver.base.enums.StatusType
import com.inout.apiserver.domain.word.WordWithDefinitions
import org.bson.types.ObjectId
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.stereotype.Repository

interface MongoWordRepositoryInternal : MongoRepository<MongoWord, ObjectId> {
    fun findByNameAndFromLanguageAndToLanguage(
        name: String,
        fromLanguage: LanguageType,
        toLanguage: LanguageType,
    ): MongoWord?
}

interface MongoWordDefinitionRepository : MongoRepository<MongoWordDefinition, ObjectId> {
    fun findAllByIdInAndStatus(
        wordDefinitionIds: List<ObjectId>,
        status: StatusType,
    ): List<MongoWordDefinition>

    fun findAllByWordId(wordId: ObjectId): List<MongoWordDefinition>

    fun findByIdAndStatus(
        wordId: ObjectId,
        status: StatusType,
    ): MongoWordDefinition?
}

@Repository
class MongoWordRepository(
    private val wordRepository: MongoWordRepositoryInternal,
    private val wordDefinitionRepository: MongoWordDefinitionRepository,
    private val mongoTemplate: MongoTemplate,
) {
    fun findByNameAndFromLanguageAndToLanguage(
        name: String,
        fromLanguage: LanguageType,
        toLanguage: LanguageType,
    ): WordWithDefinitions? {
        val word =
            wordRepository.findByNameAndFromLanguageAndToLanguage(
                name = name,
                fromLanguage = fromLanguage,
                toLanguage = toLanguage,
            ) ?: return null

        val definitions = wordDefinitionRepository.findAllByWordId(word.id!!)

        return WordWithDefinitions.of(word, definitions)
    }

    fun findById(id: ObjectId): WordWithDefinitions? {
        val word = wordRepository.findById(id).orElse(null) ?: return null
        val definitions = wordDefinitionRepository.findAllByWordId(word.id!!)
        return WordWithDefinitions.of(word, definitions)
    }

    fun saveWord(word: MongoWord): MongoWord = wordRepository.save(word)

    fun saveWordDefinition(wordDefinition: MongoWordDefinition): MongoWordDefinition = wordDefinitionRepository.save(wordDefinition)

    fun findAllWithLiveDefinitionsBy(
        fromLanguage: LanguageType,
        toLanguage: LanguageType,
        prefix: String,
        lexicalCategory: String?,
        pageable: Pageable,
    ): Page<WordWithDefinitions> {
        TODO()
    }

    fun findByLiveDefinitionsId(wordDefinitionId: ObjectId): WordWithDefinitions? {
        val wordDefinition =
            wordDefinitionRepository.findByIdAndStatus(
                wordId = wordDefinitionId,
                status = StatusType.LIVE,
            ) ?: return null

        val word = wordRepository.findById(wordDefinition.wordId).orElse(null) ?: return null

        return WordWithDefinitions.of(word, listOf(wordDefinition))
    }

    fun findAllByLiveDefinitionsIds(wordDefinitionIds: List<ObjectId>): List<WordWithDefinitions> {
        val wordDefinitions =
            wordDefinitionRepository.findAllByIdInAndStatus(
                wordDefinitionIds = wordDefinitionIds,
                status = StatusType.LIVE,
            )

        val wordIds = wordDefinitions.map { it.wordId }.distinct()

        val words = wordRepository.findAllById(wordIds)

        return words.map { word ->
            val definitions = wordDefinitions.filter { it.wordId == word.id }
            WordWithDefinitions.of(word, definitions)
        }
    }
}
