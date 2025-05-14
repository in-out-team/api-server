package com.inout.apiserver.infrastructure.mongo.word

import com.inout.apiserver.base.enums.LanguageType
import com.inout.apiserver.base.enums.LexicalCategoryType
import com.inout.apiserver.base.enums.StatusType
import com.inout.apiserver.domain.word.WordWithDefinitions
import org.bson.Document
import org.bson.types.ObjectId
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.aggregation.Aggregation.addFields
import org.springframework.data.mongodb.core.aggregation.Aggregation.count
import org.springframework.data.mongodb.core.aggregation.Aggregation.limit
import org.springframework.data.mongodb.core.aggregation.Aggregation.lookup
import org.springframework.data.mongodb.core.aggregation.Aggregation.match
import org.springframework.data.mongodb.core.aggregation.Aggregation.newAggregation
import org.springframework.data.mongodb.core.aggregation.Aggregation.skip
import org.springframework.data.mongodb.core.aggregation.Aggregation.sort
import org.springframework.data.mongodb.core.query.Criteria
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
        lexicalCategory: LexicalCategoryType?,
        pageable: Pageable,
    ): Page<WordWithDefinitions> {
        val matchStage =
            match(
                Criteria
                    .where("fromLanguage")
                    .`is`(fromLanguage)
                    .and("toLanguage")
                    .`is`(toLanguage)
                    .and("name")
                    .regex("^$prefix.*", "i"),
            )
        val lookupStage = lookup("word_definitions", "_id", "wordId", "definitions")
        val filterConditions =
            mutableListOf(
                Document("\$eq", listOf("$\$def.status", StatusType.LIVE.name)),
            )
        if (lexicalCategory != null) {
            filterConditions.add(
                Document("\$eq", listOf("$\$def.lexicalCategory", lexicalCategory)),
            )
        }
        val addFieldsStage =
            addFields()
                .addField("definitions")
                .withValue(
                    Document(
                        "\$filter",
                        Document()
                            .append("input", "\$definitions")
                            .append("as", "def")
                            .append(
                                "cond",
                                if (filterConditions.size == 1) {
                                    filterConditions[0] // Only status
                                } else {
                                    Document("\$and", filterConditions) // status + lexicalCategory
                                },
                            ),
                    ),
                ).build()
        val matchHavingDefinitionsStage =
            match(
                Criteria
                    .where("definitions")
                    .not()
                    .size(0),
            )

        val countAggregation =
            newAggregation(
                matchStage,
                lookupStage,
                addFieldsStage,
                matchHavingDefinitionsStage,
                count().`as`("total"),
            )
        val aggregation =
            newAggregation(
                matchStage,
                lookupStage,
                addFieldsStage,
                matchHavingDefinitionsStage,
                sort(Sort.by(Sort.Order.asc("name"))),
                skip(pageable.offset),
                limit(pageable.pageSize.toLong()),
            )

        val countResult = mongoTemplate.aggregate(countAggregation, "words", Document::class.java).uniqueMappedResult
        val total = countResult?.get("total") as? Long ?: 0L
        val results =
            mongoTemplate
                .aggregate(
                    aggregation,
                    "words",
                    WordWithDefinitions::class.java,
                ).mappedResults

        return PageImpl(
            results,
            pageable,
            total,
        )
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

    fun updateWordDefinitionStatus(
        wordDefinitionId: ObjectId,
        status: StatusType,
    ) {
        val wordDefinition =
            wordDefinitionRepository.findById(wordDefinitionId).orElse(null)
                ?: throw IllegalArgumentException("Word definition not found")

        wordDefinitionRepository.save(
            wordDefinition.copy(
                status = status,
            ),
        )
    }
}
