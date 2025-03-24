package com.inout.apiserver.domain.word

import com.inout.apiserver.base.enums.LanguageType
import com.inout.apiserver.base.enums.LexicalCategoryType
import com.inout.apiserver.base.enums.SentenceType
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
                val word = wordFactory.createWord()

                val res = wordService.getWordById(word.id!!)

                res shouldBe word
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

        describe("getWordsWithDefinitions") {
            it("should return Page of Words") {
                val words = listOf(wordFactory.createWord())
                val pageable = PageRequest.of(0, 1)

                val res =
                    wordService.getWordsWithDefinitions(LanguageType.ENGLISH, LanguageType.KOREAN, "book", null, pageable)

                res.content shouldBe words
                res.totalElements shouldBe words.size.toLong()
            }
        }

        describe("getWordByWordDefinitionId") {
            it("should raise NotFoundException if not word with given wordDefinitionId does not exist") {
                val wordDefinitionId = 1L

                val res =
                    assertThrows<NotFoundException> {
                        wordService.getWordByWordDefinitionId(wordDefinitionId)
                    }

                res.message shouldBe "Word Definition not found"
                res.code shouldBe "WORD_2"
            }

            it("should return Word") {
                val word = wordFactory.createWord()
                val wordDefinitionId = word.definitions.first().id!!

                val res = wordService.getWordByWordDefinitionId(wordDefinitionId)

                res shouldBe
                    word.copy(
                        definitions =
                            word.definitions
                                .filter { it.id == wordDefinitionId }
                                .toMutableList(),
                    )
            }
        }

        describe("getWordsByWordDefinitionIds") {
            it("should return list of words") {
                val words =
                    listOf(
                        wordFactory.createWord(
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
                                ),
                        ),
                        wordFactory.createWord(
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
                                ),
                        ),
                    )
                val wordDefinitionIds = words.flatMap { it.definitions }.map { it.id!! }.dropLast(1)

                val res = wordService.getWordsByWordDefinitionIds(wordDefinitionIds)

                res.size shouldBe 2
                val firstWord = res.first()
                firstWord.definitions.map { it.id } shouldBe words.first().definitions.map { it.id }
                val lastWord = res.last()
                lastWord.definitions.size shouldBe 1
                lastWord.definitions.first().id shouldBe
                    words
                        .last()
                        .definitions
                        .first()
                        .id
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
