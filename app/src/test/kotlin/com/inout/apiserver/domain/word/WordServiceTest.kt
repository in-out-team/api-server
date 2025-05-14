package com.inout.apiserver.domain.word

import com.inout.apiserver.base.enums.LanguageType
import com.inout.apiserver.base.enums.LexicalCategoryType
import com.inout.apiserver.base.enums.SentenceType
import com.inout.apiserver.base.enums.StatusType
import com.inout.apiserver.domain.user.MongoUserFactory
import com.inout.apiserver.error.ConflictException
import com.inout.apiserver.error.NotFoundException
import com.inout.apiserver.extension.cleanUp
import com.inout.apiserver.helper.InOutSpringBootTest
import com.inout.apiserver.infrastructure.mongo.user.MongoUser
import com.inout.apiserver.infrastructure.mongo.word.MongoSentence
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.collections.shouldBeSortedBy
import io.kotest.matchers.collections.shouldContainAll
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import org.bson.types.ObjectId
import org.junit.jupiter.api.assertThrows
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.data.mongodb.core.MongoTemplate

@InOutSpringBootTest
class WordServiceTest(
    // factories
    private val mongoWordFactory: MongoWordFactory,
    private val mongoUserFactory: MongoUserFactory,
    // services
    private val mongoWordService: MongoWordService,
    // etc
    private val mongoTemplate: MongoTemplate,
) : DescribeSpec({
        afterEach {
            mongoTemplate.cleanUp()
        }

        describe("getWordWithDefinitionsBy - name, fromLanguage, toLanguage") {
            it("should return Word if found") {
                val word = mongoWordFactory.createWord()

                val res =
                    mongoWordService.getWordWithDefinitionsBy(
                        name = word.name,
                        fromLanguage = word.fromLanguage,
                        toLanguage = word.toLanguage,
                    )

                res shouldBe word
            }

            it("should return null if not found") {
                val res =
                    mongoWordService.getWordWithDefinitionsBy(
                        name = "name",
                        fromLanguage = LanguageType.ENGLISH,
                        toLanguage = LanguageType.KOREAN,
                    )

                res shouldBe null
            }
        }

        describe("getWordWithDefinitionsBy - id") {
            it("should return Word if found") {
                val word =
                    mongoWordFactory
                        .createWord(
                            name = "board",
                            definitions =
                                listOf(
                                    WordWithDefinitions.WordDefinition(
                                        lexicalCategory = LexicalCategoryType.NOUN,
                                        meaning = "판자",
                                        preContext = "무엇을 올리거나 붙이기 위해 사용되는 넓고 평평한 나무 조각",
                                        status = StatusType.REMOVED,
                                    ),
                                    WordWithDefinitions.WordDefinition(
                                        lexicalCategory = LexicalCategoryType.VERB,
                                        meaning = "탑승하다",
                                        preContext = "특정한 교통 수단에 몸을 올리다",
                                        status = StatusType.LIVE,
                                    ),
                                    WordWithDefinitions.WordDefinition(
                                        lexicalCategory = LexicalCategoryType.ADJECTIVE,
                                        meaning = "공공의",
                                        preContext = "공공의 기관이나 단체에 속한",
                                        status = StatusType.PENDING,
                                    ),
                                ),
                        )

                val res = mongoWordService.getWordWithDefinitionsBy(id = word.id!!)

                res shouldNotBe null
                res!!.id shouldBe word.id
                res.name shouldBe word.name
                res.fromLanguage shouldBe word.fromLanguage
                res.toLanguage shouldBe word.toLanguage
                res.definitions.size shouldBe 3
                res.definitions.forEach { wordDefinition ->
                    val sameDefinition = word.definitions.first { it.id == wordDefinition.id }
                    wordDefinition.id shouldBe sameDefinition.id
                    wordDefinition.lexicalCategory shouldBe sameDefinition.lexicalCategory
                    wordDefinition.meaning shouldBe sameDefinition.meaning
                    wordDefinition.preContext shouldBe sameDefinition.preContext
                    wordDefinition.status shouldBe sameDefinition.status
                }
            }

            it("should return null if not found") {
                val res = mongoWordService.getWordWithDefinitionsBy(id = ObjectId())

                res shouldBe null
            }
        }

        describe("MongoWordService - createWord") {
            it("should raise ConflictException if word already exists") {
                val word = mongoWordFactory.createWord()

                val res =
                    assertThrows<ConflictException> {
                        mongoWordService.createWord(
                            WordCreateObject(
                                name = word.name,
                                fromLanguage = word.fromLanguage,
                                toLanguage = word.toLanguage,
                                definitions = emptyList(),
                            ),
                        )
                    }

                res.message shouldBe "Word already exists"
                res.code shouldBe "WORD_1"
            }

            it("should return created Word") {
                val wordCreateObject =
                    WordCreateObject(
                        name = "book",
                        fromLanguage = LanguageType.ENGLISH,
                        toLanguage = LanguageType.KOREAN,
                        listOf(
                            WordDefinitionCreateObject(
                                lexicalCategory = LexicalCategoryType.NOUN,
                                meaning = "책",
                                preContext = "정보를 얻거나 즐거움을 얻기 위해 읽는 인쇄물",
                            ),
                            WordDefinitionCreateObject(
                                lexicalCategory = LexicalCategoryType.VERB,
                                meaning = "예약하다",
                                preContext = "특정한 날짜나 시간에 무엇을 하기 위해 미리 자리를 확보하다",
                            ),
                        ),
                    )

                val res = mongoWordService.createWord(wordCreateObject)

                res.id shouldNotBe null
                res.name shouldBe wordCreateObject.name
                res.fromLanguage shouldBe wordCreateObject.fromLanguage
                res.toLanguage shouldBe wordCreateObject.toLanguage
                res.definitions.forEachIndexed { index, wordDefinition ->
                    wordDefinition.id shouldNotBe null
                    wordDefinition.lexicalCategory shouldBe wordCreateObject.definitions[index].lexicalCategory
                    wordDefinition.meaning shouldBe wordCreateObject.definitions[index].meaning
                    wordDefinition.preContext shouldBe wordCreateObject.definitions[index].preContext
                }
            }
        }

        describe("MongoWordService - getWordsWithLiveDefinitions") {
            it("should return Page of Words") {
                val words = listOf(mongoWordFactory.createWord())
                val pageable = PageRequest.of(0, 1)

                val res =
                    mongoWordService.getWordsWithLiveDefinitions(
                        LanguageType.ENGLISH,
                        LanguageType.KOREAN,
                        "book",
                        null,
                        pageable,
                    )

                res.content shouldBe words
                res.totalElements shouldBe words.size.toLong()
            }

            it("should not return non-live definitions") {
                val words =
                    listOf(
                        // all live
                        mongoWordFactory.createWord(
                            name = "book",
                            definitions =
                                listOf(
                                    WordWithDefinitions.WordDefinition(
                                        lexicalCategory = LexicalCategoryType.NOUN,
                                        meaning = "책",
                                        preContext = "정보를 얻거나 즐거움을 얻기 위해 읽는 인쇄물",
                                        status = StatusType.LIVE,
                                    ),
                                    WordWithDefinitions.WordDefinition(
                                        lexicalCategory = LexicalCategoryType.VERB,
                                        meaning = "예약하다",
                                        preContext = "특정한 날짜나 시간에 무엇을 하기 위해 미리 자리를 확보하다",
                                        status = StatusType.LIVE,
                                    ),
                                ),
                        ),
                        // some live
                        mongoWordFactory
                            .createWord(
                                name = "board",
                                definitions =
                                    listOf(
                                        WordWithDefinitions.WordDefinition(
                                            lexicalCategory = LexicalCategoryType.NOUN,
                                            meaning = "판자",
                                            preContext = "무엇을 올리거나 붙이기 위해 사용되는 넓고 평평한 나무 조각",
                                            status = StatusType.REMOVED,
                                        ),
                                        WordWithDefinitions.WordDefinition(
                                            lexicalCategory = LexicalCategoryType.VERB,
                                            meaning = "탑승하다",
                                            preContext = "특정한 교통 수단에 몸을 올리다",
                                            status = StatusType.LIVE,
                                        ),
                                        WordWithDefinitions.WordDefinition(
                                            lexicalCategory = LexicalCategoryType.ADJECTIVE,
                                            meaning = "공공의",
                                            preContext = "공공의 기관이나 단체에 속한",
                                            status = StatusType.PENDING,
                                        ),
                                    ),
                            ),
                        // no definitions
                        mongoWordFactory.createWord(
                            name = "boast",
                            definitions = emptyList(),
                        ),
                        // no live definitions
                        mongoWordFactory
                            .createWord(
                                name = "booked",
                                definitions =
                                    listOf(
                                        WordWithDefinitions.WordDefinition(
                                            lexicalCategory = LexicalCategoryType.ADJECTIVE,
                                            meaning = "예약된",
                                            preContext = "미리 자리를 확보한",
                                            status = StatusType.REMOVED,
                                        ),
                                    ),
                            ),
                    )

                val res =
                    mongoWordService.getWordsWithLiveDefinitions(
                        fromLanguage = LanguageType.ENGLISH,
                        toLanguage = LanguageType.KOREAN,
                        prefix = "b",
                        lexicalCategory = null,
                        pageable = PageRequest.of(0, 5, Sort.by("name").descending()),
                    )

                // only "book" and "board" should be returned
                res.totalElements shouldBe 2
                res.content.size shouldBe 2
                res.content.map { it.id } shouldContainAll
                    words
                        .filter { it.definitions.any { definition -> definition.status == StatusType.LIVE } }
                        .map { it.id }
                res.content.forEach { word ->
                    word.definitions.all { it.status == StatusType.LIVE } shouldBe true
                }
            }
        }

        describe("MongoWordService - getWordByLiveWordDefinitionId") {
            it("should raise NotFoundException if not word with given wordDefinitionId does not exist") {
                val wordDefinitionId = ObjectId()

                val res =
                    assertThrows<NotFoundException> {
                        mongoWordService.getWordByLiveWordDefinitionId(wordDefinitionId)
                    }

                res.message shouldBe "Word Definition not found"
                res.code shouldBe "WORD_2"
            }

            it("should return Word") {
                // given
                val word =
                    mongoWordFactory
                        .createWord(
                            name = "board",
                            definitions =
                                listOf(
                                    WordWithDefinitions.WordDefinition(
                                        lexicalCategory = LexicalCategoryType.NOUN,
                                        meaning = "판자",
                                        preContext = "무엇을 올리거나 붙이기 위해 사용되는 넓고 평평한 나무 조각",
                                        status = StatusType.REMOVED,
                                    ),
                                    WordWithDefinitions.WordDefinition(
                                        lexicalCategory = LexicalCategoryType.VERB,
                                        meaning = "탑승하다",
                                        preContext = "특정한 교통 수단에 몸을 올리다",
                                        status = StatusType.LIVE,
                                    ),
                                    WordWithDefinitions.WordDefinition(
                                        lexicalCategory = LexicalCategoryType.ADJECTIVE,
                                        meaning = "공공의",
                                        preContext = "공공의 기관이나 단체에 속한",
                                        status = StatusType.PENDING,
                                    ),
                                ),
                        )

                // when
                val res =
                    mongoWordService.getWordByLiveWordDefinitionId(word.definitions[1].id!!)

                // then
                res.id shouldBe word.id
                res.name shouldBe word.name
                res.fromLanguage shouldBe word.fromLanguage
                res.toLanguage shouldBe word.toLanguage
                res.definitions.size shouldBe 1
                res.definitions[0].id shouldBe word.definitions[1].id
                res.definitions[0].lexicalCategory shouldBe word.definitions[1].lexicalCategory
                res.definitions[0].meaning shouldBe word.definitions[1].meaning
                res.definitions[0].preContext shouldBe word.definitions[1].preContext
                res.definitions[0].status shouldBe StatusType.LIVE
            }
        }

        describe("MongoWordService - getWordsByLiveWordDefinitionIds") {
            it("should return list of words") {
                // given
                val words =
                    listOf(
                        mongoWordFactory
                            .createWord(
                                name = "book",
                                fromLanguage = LanguageType.ENGLISH,
                                toLanguage = LanguageType.KOREAN,
                                definitions =
                                    listOf(
                                        WordWithDefinitions.WordDefinition(
                                            lexicalCategory = LexicalCategoryType.NOUN,
                                            meaning = "책",
                                            preContext = "정보를 얻거나 즐거움을 얻기 위해 읽는 인쇄물",
                                            status = StatusType.LIVE,
                                        ),
                                        WordWithDefinitions.WordDefinition(
                                            lexicalCategory = LexicalCategoryType.VERB,
                                            meaning = "예약하다",
                                            preContext = "특정한 날짜나 시간에 무엇을 하기 위해 미리 자리를 확보하다",
                                            status = StatusType.LIVE,
                                        ),
                                        WordWithDefinitions.WordDefinition(
                                            lexicalCategory = LexicalCategoryType.VERB,
                                            meaning = "remove",
                                            preContext = "remove",
                                            status = StatusType.REMOVED,
                                        ),
                                    ),
                            ),
                        mongoWordFactory
                            .createWord(
                                name = "booked",
                                fromLanguage = LanguageType.ENGLISH,
                                toLanguage = LanguageType.KOREAN,
                                definitions =
                                    listOf(
                                        WordWithDefinitions.WordDefinition(
                                            lexicalCategory = LexicalCategoryType.ADJECTIVE,
                                            meaning = "예약된",
                                            preContext = "미리 자리를 확보한",
                                            status = StatusType.LIVE,
                                        ),
                                        WordWithDefinitions.WordDefinition(
                                            lexicalCategory = LexicalCategoryType.VERB,
                                            meaning = "예약하다",
                                            preContext = "특정한 날짜나 시간에 무엇을 하기 위해 미리 자리를 확보하다",
                                            status = StatusType.LIVE,
                                        ),
                                        WordWithDefinitions.WordDefinition(
                                            lexicalCategory = LexicalCategoryType.VERB,
                                            meaning = "pending",
                                            preContext = "pending",
                                            status = StatusType.PENDING,
                                        ),
                                    ),
                            ),
                    )
                val wordDefinitionIds =
                    words.flatMap { it.definitions }.map { it.id!! }

                // when
                val res =
                    mongoWordService.getWordsByLiveWordDefinitionIds(wordDefinitionIds)

                // then
                res.size shouldBe 2
                val firstWord = res.first()
                firstWord.definitions.size shouldBe 2
                firstWord.definitions.all { it.status == StatusType.LIVE } shouldBe true
                val lastWord = res.last()
                lastWord.definitions.size shouldBe 2
                lastWord.definitions.all { it.status == StatusType.LIVE } shouldBe true
            }
        }

        describe("MongoService - createUserSentence") {
            var user: MongoUser? = null
            var word: WordWithDefinitions?
            var wordDefinitionId = ObjectId()
            var sentence: MongoSentence? = null

            beforeEach {
                user = mongoUserFactory.createUser()
                word = mongoWordFactory.createWord()
                wordDefinitionId = word!!.definitions.first().id!!
                sentence = mongoWordFactory.createSentence(wordDefinitionId)
            }

            it("should raise ConflictException if userSentence already exists") {
                // given
                mongoWordService.createUserSentence(
                    userId = user!!.id!!,
                    wordDefinitionId = wordDefinitionId,
                    type = SentenceType.READING,
                    sentenceId = sentence!!.id!!,
                )

                // when & then
                val res =
                    assertThrows<ConflictException> {
                        mongoWordService.createUserSentence(
                            userId = user!!.id!!,
                            wordDefinitionId = wordDefinitionId,
                            type = SentenceType.READING,
                            sentenceId = sentence!!.id!!,
                        )
                    }

                res.message shouldBe "User Sentence already exists"
                res.code shouldBe "SENTENCE_2"
            }

            it("should save and return UserSentence") {
                // when
                val res =
                    mongoWordService.createUserSentence(
                        userId = user!!.id!!,
                        wordDefinitionId = wordDefinitionId,
                        type = SentenceType.READING,
                        sentenceId = sentence!!.id!!,
                    )

                // then
                res.id shouldNotBe null
                res.userId shouldBe user!!.id
                res.wordDefinitionId shouldBe wordDefinitionId
                res.type shouldBe SentenceType.READING
                res.sentenceId shouldBe sentence!!.id
            }
        }

        describe("MongoService - getConversationsBy") {
            var user: MongoUser? = null
            var word: WordWithDefinitions?
            var wordDefinitionId = ObjectId()

            beforeEach {
                user = mongoUserFactory.createUser()
                word = mongoWordFactory.createWord()
                wordDefinitionId = word!!.definitions.first().id!!
            }

            it("should return empty list if no conversations found") {
                // when
                val res =
                    mongoWordService.getConversationsBy(
                        user = user!!,
                        wordDefinitionId = wordDefinitionId,
                    )

                // then
                res.size shouldBe 0
            }

            it("should return list of conversations sorted by createdAt") {
                // given
                repeat(2) {
                    mongoWordFactory.createConversation(user!!, wordDefinitionId)
                }

                // when
                val res =
                    mongoWordService.getConversationsBy(
                        user = user!!,
                        wordDefinitionId = wordDefinitionId,
                    )

                // then
                res.size shouldBe 2
                res shouldBeSortedBy { it.createdAt }
            }
        }
    })
