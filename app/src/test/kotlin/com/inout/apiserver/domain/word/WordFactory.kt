package com.inout.apiserver.domain.word

import com.inout.apiserver.base.enums.LanguageType
import com.inout.apiserver.base.enums.LexicalCategoryType
import com.inout.apiserver.infrastructure.db.word.SentenceEntity
import com.inout.apiserver.infrastructure.db.word.SentenceRepository
import com.inout.apiserver.infrastructure.db.word.WordDefinitionEntity
import com.inout.apiserver.infrastructure.db.word.WordEntity
import com.inout.apiserver.infrastructure.db.word.WordRepository
import org.springframework.stereotype.Component
import java.time.Instant

@Component
class WordFactory(
    private val wordRepository: WordRepository,
    private val sentenceRepository: SentenceRepository,
) {
    companion object {
        @Deprecated("Use member function createWord instead")
        fun createWord(
            id: Long = 1L,
            name: String = "book",
            fromLanguage: LanguageType = LanguageType.ENGLISH,
            toLanguage: LanguageType = LanguageType.KOREAN,
            lexicalCategory: LexicalCategoryType = LexicalCategoryType.NOUN,
            meaning: String = "책",
            preContext: String = "정보를 얻거나 즐거움을 얻기 위해 읽는 인쇄물",
        ): Word {
            val now = Instant.now()
            val wordDefinition =
                WordDefinition(
                    id = id,
                    lexicalCategory = lexicalCategory,
                    meaning = meaning,
                    preContext = preContext,
                )
            return Word(
                id = id,
                name = name,
                fromLanguage = fromLanguage,
                toLanguage = toLanguage,
                definitions = listOf(wordDefinition),
                createdAt = now,
                updatedAt = now,
            )
        }
    }

    fun createWord(
        name: String = "book",
        fromLanguage: LanguageType = LanguageType.ENGLISH,
        toLanguage: LanguageType = LanguageType.KOREAN,
        wordDefinitions: List<WordDefinitionEntity> =
            listOf(
                WordDefinitionEntity(
                    lexicalCategory = LexicalCategoryType.NOUN,
                    meaning = "책",
                    preContext = "정보를 얻거나 즐거움을 얻기 위해 읽는 인쇄물",
                ),
            ),
    ): Word =
        wordRepository.save(
            WordEntity(
                name = name,
                fromLanguage = fromLanguage,
                toLanguage = toLanguage,
                definitions = wordDefinitions,
            ),
        )

    fun createSentence(
        wordDefinitionId: Long,
        content: String = "I read a book",
        translation: String = "나는 책을 읽었다",
        lexicalCategories: List<SentenceEntity.LexicalCategoryInfo> =
            listOf(
                SentenceEntity.LexicalCategoryInfo(
                    word = "I",
                    lexicalCategory = LexicalCategoryType.PRONOUN,
                ),
                SentenceEntity.LexicalCategoryInfo(
                    word = "read",
                    lexicalCategory = LexicalCategoryType.VERB,
                ),
                SentenceEntity.LexicalCategoryInfo(
                    word = "a",
                    lexicalCategory = LexicalCategoryType.ARTICLE,
                ),
                SentenceEntity.LexicalCategoryInfo(
                    word = "book",
                    lexicalCategory = LexicalCategoryType.NOUN,
                ),
            ),
    ): Sentence =
        sentenceRepository.save(
            SentenceEntity(
                wordDefinitionId = wordDefinitionId,
                content = content,
                translation = translation,
                lexicalCategories = lexicalCategories,
            ),
        )
}
