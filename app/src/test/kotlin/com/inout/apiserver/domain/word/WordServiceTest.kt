package com.inout.apiserver.domain.word

import com.inout.apiserver.base.enums.LanguageType
import com.inout.apiserver.base.enums.LexicalCategoryType
import com.inout.apiserver.base.enums.SentenceType
import com.inout.apiserver.base.enums.StatusType
import com.inout.apiserver.domain.study.StudyFactory
import com.inout.apiserver.domain.user.UserFactory
import com.inout.apiserver.error.ConflictException
import com.inout.apiserver.error.NotFoundException
import com.inout.apiserver.extension.cleanUp
import com.inout.apiserver.helper.InOutSpringBootTest
import com.inout.apiserver.infrastructure.db.user.User
import com.inout.apiserver.infrastructure.db.word.Sentence
import com.inout.apiserver.infrastructure.db.word.UserSentenceRepository
import com.inout.apiserver.infrastructure.db.word.Word
import com.inout.apiserver.infrastructure.db.word.WordRepository
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.collections.shouldBeSortedBy
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import org.junit.jupiter.api.assertThrows
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.jdbc.core.JdbcTemplate
import java.util.Optional

@InOutSpringBootTest
class WordServiceTest(
    private val wordRepository: WordRepository,
    private val userSentenceRepository: UserSentenceRepository,
    // factories
    private val wordFactory: WordFactory,
    private val userFactory: UserFactory,
    private val studyFactory: StudyFactory,
    // services
    private val wordService: WordService,
    // etc
    private val jdbcTemplate: JdbcTemplate,
) : DescribeSpec({
        afterEach {
            jdbcTemplate.cleanUp()
        }

        describe("getWordByNameAndFromLanguageAndToLanguage") {
            it("should return Word if found") {
                val word = wordFactory.createWord()

                val res =
                    wordService.getWordByNameAndFromLanguageAndToLanguage(word.name, word.fromLanguage, word.toLanguage)

                res shouldBe word
            }

            it("should return null if not found") {
                wordRepository.findByNameAndFromLanguageAndToLanguage(
                    "name",
                    LanguageType.ENGLISH,
                    LanguageType.KOREAN,
                ) shouldBe null

                val res =
                    wordService.getWordByNameAndFromLanguageAndToLanguage("name", LanguageType.ENGLISH, LanguageType.KOREAN)

                res shouldBe null
            }
        }

        describe("GetWordById") {
            it("should return Word if found") {
                val word =
                    wordFactory
                        .createWord(
                            name = "board",
                            wordDefinitions =
                                listOf(
                                    WordDefinitionCreateObject(
                                        lexicalCategory = LexicalCategoryType.NOUN,
                                        meaning = "판자",
                                        preContext = "무엇을 올리거나 붙이기 위해 사용되는 넓고 평평한 나무 조각",
                                    ),
                                    WordDefinitionCreateObject(
                                        lexicalCategory = LexicalCategoryType.VERB,
                                        meaning = "탑승하다",
                                        preContext = "특정한 교통 수단에 몸을 올리다",
                                    ),
                                    WordDefinitionCreateObject(
                                        lexicalCategory = LexicalCategoryType.ADJECTIVE,
                                        meaning = "공공의",
                                        preContext = "공공의 기관이나 단체에 속한",
                                    ),
                                ),
                        ).also { word ->
                            word.removeDefinition(word.definitions.first().id!!)
                            word.definitions[2] = word.definitions[2].copy(status = StatusType.PENDING)
                            wordRepository.save(word)
                        }

                val res = wordService.getWordById(word.id!!)

                res shouldNotBe null
                res!!.id shouldBe word.id
                res.name shouldBe word.name
                res.fromLanguage shouldBe word.fromLanguage
                res.toLanguage shouldBe word.toLanguage
                res.definitions.size shouldBe 3
                res.definitions.forEach { wordDefinition ->
                    val sameDefinition = word.definitions.first { it.id == wordDefinition.id }
                    wordDefinition.id shouldBe sameDefinition.id
                    wordDefinition.wordId shouldBe sameDefinition.wordId
                    wordDefinition.lexicalCategory shouldBe sameDefinition.lexicalCategory
                    wordDefinition.meaning shouldBe sameDefinition.meaning
                    wordDefinition.preContext shouldBe sameDefinition.preContext
                    wordDefinition.createdAt shouldNotBe null
                    wordDefinition.updatedAt shouldNotBe null
                    wordDefinition.status shouldBe sameDefinition.status
                }
            }

            it("should return null if not found") {
                wordRepository.findById(1L) shouldBe Optional.empty()

                val res = wordService.getWordById(1L)

                res shouldBe null
            }
        }

        describe("createWord") {
            it("should raise ConflictException if word already exists") {
                val word = wordFactory.createWord()

                val res =
                    assertThrows<ConflictException> {
                        wordService.createWord(
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

                val res = wordService.createWord(wordCreateObject)

                res.id shouldNotBe null
                res.name shouldBe wordCreateObject.name
                res.fromLanguage shouldBe wordCreateObject.fromLanguage
                res.toLanguage shouldBe wordCreateObject.toLanguage
                res.definitions.forEachIndexed { index, wordDefinition ->
                    wordDefinition.id shouldNotBe null
                    wordDefinition.wordId shouldBe res.id
                    wordDefinition.lexicalCategory shouldBe wordCreateObject.definitions[index].lexicalCategory
                    wordDefinition.meaning shouldBe wordCreateObject.definitions[index].meaning
                    wordDefinition.preContext shouldBe wordCreateObject.definitions[index].preContext
                    wordDefinition.createdAt shouldNotBe null
                    wordDefinition.updatedAt shouldNotBe null
                }
                res.createdAt shouldNotBe null
                res.updatedAt shouldNotBe null
                wordRepository.findById(res.id!!) shouldBe Optional.of(res)
            }
        }

        describe("getWordsWithLiveDefinitions") {
            it("should return Page of Words") {
                val words = listOf(wordFactory.createWord())
                val pageable = PageRequest.of(0, 1)

                val res =
                    wordService.getWordsWithLiveDefinitions(
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
                        wordFactory.createWord(
                            name = "book",
                            wordDefinitions =
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
                        ),
                        // some live
                        wordFactory
                            .createWord(
                                name = "board",
                                wordDefinitions =
                                    listOf(
                                        WordDefinitionCreateObject(
                                            lexicalCategory = LexicalCategoryType.NOUN,
                                            meaning = "판자",
                                            preContext = "무엇을 올리거나 붙이기 위해 사용되는 넓고 평평한 나무 조각",
                                        ),
                                        WordDefinitionCreateObject(
                                            lexicalCategory = LexicalCategoryType.VERB,
                                            meaning = "탑승하다",
                                            preContext = "특정한 교통 수단에 몸을 올리다",
                                        ),
                                        WordDefinitionCreateObject(
                                            lexicalCategory = LexicalCategoryType.ADJECTIVE,
                                            meaning = "공공의",
                                            preContext = "공공의 기관이나 단체에 속한",
                                        ),
                                    ),
                            ).let { word ->
                                word.removeDefinition(word.definitions.first().id!!)
                                word.definitions[2] = word.definitions[2].copy(status = StatusType.PENDING)
                                wordRepository.save(word)
                            },
                        // no definitions
                        wordFactory.createWord(
                            name = "boast",
                            wordDefinitions = emptyList(),
                        ),
                        // no live definitions
                        wordFactory
                            .createWord(
                                name = "booked",
                                wordDefinitions =
                                    listOf(
                                        WordDefinitionCreateObject(
                                            lexicalCategory = LexicalCategoryType.ADJECTIVE,
                                            meaning = "예약된",
                                            preContext = "미리 자리를 확보한",
                                        ),
                                    ),
                            ).let { word ->
                                wordRepository.save(word.removeDefinition(word.definitions.first().id!!))
                            },
                    )

                val res =
                    wordService.getWordsWithLiveDefinitions(
                        fromLanguage = LanguageType.ENGLISH,
                        toLanguage = LanguageType.KOREAN,
                        prefix = "b",
                        lexicalCategory = null,
                        pageable = PageRequest.of(0, 5, Sort.by("name").descending()),
                    )

                // only "book" and "board" should be returned
                res.totalElements shouldBe 2
                res.content.size shouldBe 2
                res.content.map { it.id } shouldBe
                    words
                        .filter { it.definitions.any { definition -> definition.status == StatusType.LIVE } }
                        .map { it.id }
                res.content.forEach { word ->
                    word.definitions.all { it.status == StatusType.LIVE } shouldBe true
                }
            }
        }

        describe("getWordByLiveWordDefinitionId") {
            it("should raise NotFoundException if not word with given wordDefinitionId does not exist") {
                val wordDefinitionId = 1L

                val res =
                    assertThrows<NotFoundException> {
                        wordService.getWordByLiveWordDefinitionId(wordDefinitionId)
                    }

                res.message shouldBe "Word Definition not found"
                res.code shouldBe "WORD_2"
            }

            it("should return Word") {
                // given
                val word =
                    wordFactory
                        .createWord(
                            name = "board",
                            wordDefinitions =
                                listOf(
                                    WordDefinitionCreateObject(
                                        lexicalCategory = LexicalCategoryType.NOUN,
                                        meaning = "판자",
                                        preContext = "무엇을 올리거나 붙이기 위해 사용되는 넓고 평평한 나무 조각",
                                    ),
                                    WordDefinitionCreateObject(
                                        lexicalCategory = LexicalCategoryType.VERB,
                                        meaning = "탑승하다",
                                        preContext = "특정한 교통 수단에 몸을 올리다",
                                    ),
                                    WordDefinitionCreateObject(
                                        lexicalCategory = LexicalCategoryType.ADJECTIVE,
                                        meaning = "공공의",
                                        preContext = "공공의 기관이나 단체에 속한",
                                    ),
                                ),
                        ).also { word ->
                            word.removeDefinition(word.definitions.first().id!!)
                            word.definitions[2] = word.definitions[2].copy(status = StatusType.PENDING)
                            wordRepository.save(word)
                        }

                // when
                val res = wordRepository.findByLiveDefinitionsId(wordDefinitionId = word.definitions[1].id!!)

                // then
                res!!.id shouldBe word.id
                res.name shouldBe word.name
                res.fromLanguage shouldBe word.fromLanguage
                res.toLanguage shouldBe word.toLanguage
                res.definitions.size shouldBe 1
                res.definitions[0].id shouldBe word.definitions[1].id
                res.definitions[0].wordId shouldBe word.definitions[1].wordId
                res.definitions[0].lexicalCategory shouldBe word.definitions[1].lexicalCategory
                res.definitions[0].meaning shouldBe word.definitions[1].meaning
                res.definitions[0].preContext shouldBe word.definitions[1].preContext
                res.definitions[0].createdAt shouldNotBe null
                res.definitions[0].updatedAt shouldNotBe null
                res.definitions[0].status shouldBe StatusType.LIVE
            }
        }

        describe("getWordsByLiveWordDefinitionIds") {
            it("should return list of words") {
                // given
                val words =
                    listOf(
                        wordFactory
                            .createWord(
                                name = "book",
                                fromLanguage = LanguageType.ENGLISH,
                                toLanguage = LanguageType.KOREAN,
                                wordDefinitions =
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
                                        WordDefinitionCreateObject(
                                            lexicalCategory = LexicalCategoryType.VERB,
                                            meaning = "remove",
                                            preContext = "remove",
                                        ),
                                    ),
                            ).let { word ->
                                word.removeDefinition(word.definitions.first { it.meaning == "remove" }.id!!)
                                wordRepository.save(word)
                            },
                        wordFactory
                            .createWord(
                                name = "booked",
                                fromLanguage = LanguageType.ENGLISH,
                                toLanguage = LanguageType.KOREAN,
                                wordDefinitions =
                                    listOf(
                                        WordDefinitionCreateObject(
                                            lexicalCategory = LexicalCategoryType.ADJECTIVE,
                                            meaning = "예약된",
                                            preContext = "미리 자리를 확보한",
                                        ),
                                        WordDefinitionCreateObject(
                                            lexicalCategory = LexicalCategoryType.VERB,
                                            meaning = "예약하다",
                                            preContext = "특정한 날짜나 시간에 무엇을 하기 위해 미리 자리를 확보하다",
                                        ),
                                        WordDefinitionCreateObject(
                                            lexicalCategory = LexicalCategoryType.VERB,
                                            meaning = "pending",
                                            preContext = "pending",
                                        ),
                                    ),
                            ).let { word ->
                                val index = word.definitions.indexOfFirst { it.meaning == "pending" }
                                word.definitions[index] = word.definitions[index].copy(status = StatusType.PENDING)
                                wordRepository.save(word)
                            },
                    )
                val wordDefinitionIds = words.flatMap { it.definitions }.map { it.id!! }

                // when
                val res = wordService.getWordsByLiveWordDefinitionIds(wordDefinitionIds)

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

        describe("createUserSentence") {
            var user: User? = null
            var word: Word?
            var wordDefinitionId = 0L
            var sentence: Sentence? = null

            beforeEach {
                user = userFactory.createUser()
                word = wordFactory.createWord()
                wordDefinitionId = word!!.definitions.first().id!!
                sentence =
                    wordFactory.createSentence(
                        wordDefinitionId = wordDefinitionId,
                    )
            }

            it("should raise ConflictException if userSentence already exists") {
                // Given
                val userSentenceCreateObject =
                    UserSentenceCreateObject(
                        userId = user!!.id!!,
                        wordDefinitionId = wordDefinitionId,
                        type = SentenceType.READING,
                        sentenceId = sentence!!.id!!,
                    )
                wordService.createUserSentence(userSentenceCreateObject)

                // When, Then
                val result =
                    assertThrows<ConflictException> {
                        wordService.createUserSentence(userSentenceCreateObject)
                    }

                result.message shouldBe "User Sentence already exists"
                result.code shouldBe "SENTENCE_2"
            }

            it("should save and return UserSentence") {
                // Given
                val userSentenceCreateObject =
                    UserSentenceCreateObject(
                        userId = user!!.id!!,
                        wordDefinitionId = wordDefinitionId,
                        type = SentenceType.READING,
                        sentenceId = sentence!!.id!!,
                    )

                // When
                val result = wordService.createUserSentence(userSentenceCreateObject)

                // Then
                result.userId shouldBe userSentenceCreateObject.userId
                result.wordDefinitionId shouldBe userSentenceCreateObject.wordDefinitionId
                result.type shouldBe userSentenceCreateObject.type
                result.sentenceId shouldBe userSentenceCreateObject.sentenceId
                userSentenceRepository.findByUserIdAndSentenceId(
                    userId = userSentenceCreateObject.userId,
                    sentenceId = userSentenceCreateObject.sentenceId,
                ) shouldBe result
            }
        }

        describe("getConversationsBy") {
            var user: User? = null
            var word: Word?
            var wordDefinitionId = 0L

            beforeEach {
                user = userFactory.createUser()
                word = wordFactory.createWord()
                wordDefinitionId = word!!.definitions.first().id!!
                studyFactory.createStudy(user!!.id!!, wordDefinitionId)
            }

            it("should return empty list if no conversations found") {
                wordService.getConversationsBy(user!!, wordDefinitionId) shouldBe emptyList()
            }

            it("should return list of conversations sorted by createdAt") {
                val conversations =
                    (1..3)
                        .map {
                            wordFactory.createConversation(user!!.id!!, wordDefinitionId)
                        }

                val result = wordService.getConversationsBy(user!!, wordDefinitionId)
                result shouldBe conversations
                result shouldBeSortedBy { it.createdAt!! }
            }
        }
    })
