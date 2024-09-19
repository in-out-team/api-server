package com.inout.apiserver.infrastructure.db.word

import com.inout.apiserver.base.enums.LanguageType
import com.inout.apiserver.base.enums.LexicalCategoryType
import com.inout.apiserver.domain.word.Word
import com.inout.apiserver.infrastructure.db.DbTestSupport
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.context.annotation.Import
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort

@Import(WordRepository::class)
class WordRepositoryTest(
    private val wordJpaRepository: WordJpaRepository,
    private val wordRepository: WordRepository,
) : DbTestSupport() {
    @Nested
    inner class Save {
        @Test
        fun `should raise error when same combination of name and language exists`() {
            // given
            val wordEntity = createWordEntity()
            wordJpaRepository.save(wordEntity)

            // when & then
            assertThatThrownBy {
                wordRepository.save(
                    WordEntity(
                        name = "test",
                        fromLanguage = LanguageType.ENGLISH,
                        toLanguage = LanguageType.KOREAN,
                        definitions = emptyList(),
                    ),
                )
            }
                .isInstanceOf(DataIntegrityViolationException::class.java)
                .hasMessageContaining("could not execute statement")
        }

        @Test
        fun `should return new saved word`() {
            // given
            val wordEntity = createWordEntity()

            // when
            val result = wordRepository.save(wordEntity)

            // then
            assertTrue(result is Word)
            assertEquals(wordEntity.name, result.name)
            assertEquals(wordEntity.fromLanguage, result.fromLanguage)
            assertEquals(wordEntity.toLanguage, result.toLanguage)
            assertEquals(wordEntity.definitions.size, result.definitions.size)
            assertEquals(wordEntity.definitions[0].lexicalCategory, result.definitions[0].lexicalCategory)
            assertEquals(wordEntity.definitions[0].meaning, result.definitions[0].meaning)
            assertEquals(wordEntity.definitions[0].preContext, result.definitions[0].preContext)
            assertNotNull(result.id)
            assertNotNull(result.createdAt)
            assertNotNull(result.updatedAt)
            assertNotNull(result.definitions[0].id)
        }
    }

    @Nested
    inner class FindByNameAndLanguage {
        @Test
        fun `should return null when word not found`() {
            // given
            val name = "test"
            val wordEntity = createWordEntity(name = name)
            wordJpaRepository.save(wordEntity)

            // when
            val result =
                wordRepository.findByNameAndFromLanguageAndToLanguage(
                    name = "1-$name",
                    fromLanguage = wordEntity.fromLanguage,
                    toLanguage = wordEntity.toLanguage,
                )

            // then
            assertNull(result)
        }

        @Test
        fun `should return word when word found`() {
            // given
            val name = "test"
            val wordEntity = createWordEntity(name = name)
            wordJpaRepository.save(wordEntity)

            // when
            val result =
                wordRepository.findByNameAndFromLanguageAndToLanguage(
                    name = name,
                    fromLanguage = wordEntity.fromLanguage,
                    toLanguage = wordEntity.toLanguage,
                )

            // then
            assertNotNull(result)
            assertEquals(wordEntity.name, result?.name)
            assertEquals(wordEntity.fromLanguage, result?.fromLanguage)
            assertEquals(wordEntity.toLanguage, result?.toLanguage)
            assertEquals(wordEntity.definitions.size, result?.definitions?.size)
            assertEquals(wordEntity.definitions[0].lexicalCategory, result?.definitions?.get(0)?.lexicalCategory)
            assertEquals(wordEntity.definitions[0].meaning, result?.definitions?.get(0)?.meaning)
            assertEquals(wordEntity.definitions[0].preContext, result?.definitions?.get(0)?.preContext)
        }
    }

    @Nested
    inner class FindById {
        @Test
        fun `should return null when word not found`() {
            // given
            assertEquals(wordJpaRepository.count(), 0)

            // when
            val result = wordRepository.findById(1L)

            // then
            assertNull(result)
        }

        @Test
        fun `should return word when word found`() {
            // given
            val wordEntity = createWordEntity()
            val savedWord = wordJpaRepository.save(wordEntity)

            // when
            val result = wordRepository.findById(requireNotNull(savedWord.id))

            // then
            assertNotNull(result)
            assertEquals(wordEntity.name, result?.name)
            assertEquals(wordEntity.fromLanguage, result?.fromLanguage)
            assertEquals(wordEntity.toLanguage, result?.toLanguage)
            assertEquals(wordEntity.definitions.size, result?.definitions?.size)
            assertEquals(wordEntity.definitions[0].lexicalCategory, result?.definitions?.get(0)?.lexicalCategory)
            assertEquals(wordEntity.definitions[0].meaning, result?.definitions?.get(0)?.meaning)
            assertEquals(wordEntity.definitions[0].preContext, result?.definitions?.get(0)?.preContext)
            assertEquals(savedWord.id, result?.id)
            assertEquals(savedWord.createdAt, result?.createdAt)
            assertEquals(savedWord.updatedAt, result?.updatedAt)
            assertEquals(savedWord.definitions[0].id, result?.definitions?.get(0)?.id)
        }
    }

    @Nested
    inner class FindWordsWithDefinitions {
        @Test
        fun `should return words with matching prefix`() {
            // given
            val wordEntity1 =
                createWordEntity(name = "book", fromLanguage = LanguageType.ENGLISH, toLanguage = LanguageType.KOREAN)
            val wordEntity2 =
                createWordEntity(name = "booked", fromLanguage = LanguageType.ENGLISH, toLanguage = LanguageType.KOREAN)
            wordJpaRepository.save(wordEntity1)
            wordJpaRepository.save(wordEntity2)

            // when
            val result =
                wordRepository.findWordsWithDefinitions(
                    fromLanguage = LanguageType.ENGLISH,
                    toLanguage = LanguageType.KOREAN,
                    prefix = "book",
                    lexicalCategory = null,
                    pageable = PageRequest.of(0, 1, Sort.by(Sort.Order.asc("name"))),
                )

            // then
            assertEquals(2, result.totalElements)
            assertEquals(1, result.numberOfElements)
            assertEquals(2, result.totalPages)
            assertEquals(1, result.content.size)
            assertEquals(wordEntity1.name, result.content[0].name)
        }

        @Test
        fun `should return words with matching prefix and lexical category`() {
            // given
            val wordEntity1 =
                createWordEntity(name = "book", fromLanguage = LanguageType.ENGLISH, toLanguage = LanguageType.KOREAN)
            val wordEntity2 =
                createWordEntity(
                    name = "booked",
                    fromLanguage = LanguageType.ENGLISH,
                    toLanguage = LanguageType.KOREAN,
                    lexicalCategory = LexicalCategoryType.VERB,
                )
            wordJpaRepository.save(wordEntity1)
            wordJpaRepository.save(wordEntity2)

            // when
            val result =
                wordRepository.findWordsWithDefinitions(
                    fromLanguage = LanguageType.ENGLISH,
                    toLanguage = LanguageType.KOREAN,
                    prefix = "book",
                    lexicalCategory = LexicalCategoryType.VERB,
                    pageable = PageRequest.of(0, 1, Sort.by(Sort.Order.asc("name"))),
                )

            // then
            assertEquals(1, result.totalElements)
            assertEquals(1, result.numberOfElements)
            assertEquals(1, result.totalPages)
            assertEquals(1, result.content.size)
            assertEquals(wordEntity2.name, result.content[0].name)
        }
    }

    @Nested
    inner class FindByWordDefinitionId {
        @Test
        fun `should return word with matching word definition id`() {
            // given
            val wordEntity = createWordEntity()
            val savedWord = wordJpaRepository.save(wordEntity)

            // when
            val result = wordRepository.findByWordDefinitionId(requireNotNull(savedWord.definitions[0].id))

            // then
            assertNotNull(result)
            assertEquals(savedWord.id, result?.id)
        }

        @Test
        fun `should return null when word definition id not found`() {
            // given
            val wordEntity = createWordEntity()
            val savedWordEntity = wordJpaRepository.save(wordEntity)

            // when
            val result = wordRepository.findByWordDefinitionId(savedWordEntity.definitions[0].id!! + 1)

            // then
            assertNull(result)
        }
    }

    @Nested
    inner class FindAllByWordDefinitionIds {
        @Test
        fun `should return words with matching word definition ids`() {
            // given
            val wordEntity1 = createWordEntity(name = "book")
            val wordEntity2 = createWordEntity(name = "booking")
            val wordEntity3 = createWordEntity(name = "booked")
            val savedWordEntity1 = wordJpaRepository.save(wordEntity1)
            val savedWordEntity2 = wordJpaRepository.save(wordEntity2)
            val savedWordEntity3 = wordJpaRepository.save(wordEntity3)

            // when
            val expectedWordEntities = listOf(savedWordEntity1, savedWordEntity2)
            val notExpectedWordEntities = listOf(savedWordEntity3)
            val sut =
                wordRepository.findAllByWordDefinitionIds(
                    expectedWordEntities.map { it.definitions[0].id!! },
                )

            // then
            assertEquals(2, sut.size)
            assertTrue(
                expectedWordEntities.all { expectedWordEntity ->
                    sut.any { it.id == expectedWordEntity.id }
                },
            )
            assertTrue(
                notExpectedWordEntities.all { notExpectedWordEntity ->
                    sut.none { it.id == notExpectedWordEntity.id }
                },
            )
        }
    }

    private fun createWordEntity(
        name: String = "test",
        fromLanguage: LanguageType = LanguageType.ENGLISH,
        toLanguage: LanguageType = LanguageType.KOREAN,
        lexicalCategory: LexicalCategoryType = LexicalCategoryType.NOUN,
    ): WordEntity {
        return WordEntity(
            name = name,
            fromLanguage = fromLanguage,
            toLanguage = toLanguage,
            definitions =
                listOf(
                    WordDefinitionEntity(
                        lexicalCategory = lexicalCategory,
                        meaning = "test",
                        preContext = "test preContext",
                    ),
                ),
        )
    }
}
