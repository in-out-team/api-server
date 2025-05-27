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
import org.springframework.data.mongodb.core.aggregation.Aggregation
import org.springframework.data.mongodb.core.aggregation.Aggregation.addFields
import org.springframework.data.mongodb.core.aggregation.Aggregation.count
import org.springframework.data.mongodb.core.aggregation.Aggregation.limit
import org.springframework.data.mongodb.core.aggregation.Aggregation.lookup
import org.springframework.data.mongodb.core.aggregation.Aggregation.match
import org.springframework.data.mongodb.core.aggregation.Aggregation.newAggregation
import org.springframework.data.mongodb.core.aggregation.Aggregation.skip
import org.springframework.data.mongodb.core.aggregation.Aggregation.sort
import org.springframework.data.mongodb.core.aggregation.AggregationOperation
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.stereotype.Repository

class WordAggregationBuilder(
    private val fromLanguage: LanguageType,
    private val toLanguage: LanguageType,
    private val prefix: String,
    private val pageable: Pageable,
) {
    private val stages = mutableListOf<AggregationOperation>()

    private var filterLiveDefinitions: Boolean = false
    private var lexicalCategory: LexicalCategoryType? = null

    fun withLexicalCategory(lexicalCategory: LexicalCategoryType?): WordAggregationBuilder {
        this.lexicalCategory = lexicalCategory
        return this
    }

    fun filterLiveOnly(): WordAggregationBuilder {
        this.filterLiveDefinitions = true
        return this
    }

    fun build(): Pair<Aggregation, Aggregation> {
        // 1. Match stage to filter by fromLanguage, toLanguage, and prefix
        stages.add(
            match(
                Criteria
                    .where("fromLanguage")
                    .`is`(fromLanguage)
                    .and("toLanguage")
                    .`is`(toLanguage)
                    .and("name")
                    .regex("^$prefix.*", "i"),
            ),
        )

        // 2. Lookup stage to join with word_definitions collection
        stages.add(lookup("word_definitions", "_id", "wordId", "definitions"))

        // 3. AddFields stage to filter definitions based on status and lexicalCategory
        if (filterLiveDefinitions) {
            val filterConditions =
                mutableListOf(
                    Document("\$eq", listOf("$\$def.status", StatusType.LIVE.name)),
                )
            lexicalCategory?.let {
                filterConditions.add(
                    Document("\$eq", listOf("$\$def.lexicalCategory", it.name)),
                )
            }

            stages.add(
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
                                        filterConditions[0]
                                    } else {
                                        Document("\$and", filterConditions)
                                    },
                                ),
                        ),
                    ).build(),
            )
            stages.add(match(Criteria.where("definitions").not().size(0)))
        } else {
            stages.add(
                addFields()
                    .addField("definitions")
                    .withValue("\$definitions")
                    .build(),
            )
        }

        val aggregation =
            newAggregation(stages + sort(Sort.by(Sort.Order.asc("name"))) + skip(pageable.offset) + limit(pageable.pageSize.toLong()))
        val countAggregation = newAggregation(stages + count().`as`("total"))
        return aggregation to countAggregation
    }
}

interface WordRepositoryInternal : MongoRepository<Word, ObjectId> {
    fun findByNameAndFromLanguageAndToLanguage(
        name: String,
        fromLanguage: LanguageType,
        toLanguage: LanguageType,
    ): Word?
}

interface WordDefinitionRepository : MongoRepository<WordDefinition, ObjectId> {
    fun findAllByIdInAndStatus(
        wordDefinitionIds: List<ObjectId>,
        status: StatusType,
    ): List<WordDefinition>

    fun findAllByWordId(wordId: ObjectId): List<WordDefinition>

    fun findByIdAndStatus(
        wordId: ObjectId,
        status: StatusType,
    ): WordDefinition?
}

@Repository
class WordRepository(
    private val wordRepository: WordRepositoryInternal,
    private val wordDefinitionRepository: WordDefinitionRepository,
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

    fun saveWord(word: Word): Word = wordRepository.save(word)

    fun saveWordDefinition(wordDefinition: WordDefinition): WordDefinition = wordDefinitionRepository.save(wordDefinition)

    fun findAllWithLiveDefinitionsBy(
        fromLanguage: LanguageType,
        toLanguage: LanguageType,
        prefix: String,
        lexicalCategory: LexicalCategoryType?,
        pageable: Pageable,
    ): Page<WordWithDefinitions> =
        WordAggregationBuilder(
            fromLanguage = fromLanguage,
            toLanguage = toLanguage,
            prefix = prefix,
            pageable = pageable,
        ).withLexicalCategory(lexicalCategory)
            .filterLiveOnly()
            .build()
            .let { (aggregation, countAggregation) ->
                getPagedResults(
                    aggregation = aggregation,
                    countAggregation = countAggregation,
                    pageable = pageable,
                )
            }

    fun findAllWithDefinitionsBy(
        fromLanguage: LanguageType,
        toLanguage: LanguageType,
        prefix: String,
        lexicalCategory: LexicalCategoryType?,
        pageable: Pageable,
    ): Page<WordWithDefinitions> =
        WordAggregationBuilder(
            fromLanguage = fromLanguage,
            toLanguage = toLanguage,
            prefix = prefix,
            pageable = pageable,
        ).withLexicalCategory(lexicalCategory)
            .build()
            .let { (aggregation, countAggregation) ->
                getPagedResults(
                    aggregation = aggregation,
                    countAggregation = countAggregation,
                    pageable = pageable,
                )
            }

    private fun getPagedResults(
        aggregation: Aggregation,
        countAggregation: Aggregation,
        pageable: Pageable,
    ): Page<WordWithDefinitions> {
        val results =
            mongoTemplate
                .aggregate(
                    aggregation,
                    "words",
                    WordWithDefinitions::class.java,
                ).mappedResults

        val countResult =
            mongoTemplate
                .aggregate(
                    countAggregation,
                    "words",
                    Document::class.java,
                ).uniqueMappedResult
                ?.get("total") as? Int ?: 0

        return PageImpl(
            results,
            pageable,
            countResult.toLong(),
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
