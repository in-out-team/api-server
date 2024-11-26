package com.inout.apiserver.interfaces.web.v1

import com.inout.apiserver.application.word.CreateSentenceApplication
import com.inout.apiserver.application.word.CreateWordApplication
import com.inout.apiserver.application.word.ReadSentencesApplication
import com.inout.apiserver.application.word.ReadWordsApplication
import com.inout.apiserver.application.word.SelectSentenceApplication
import com.inout.apiserver.base.enums.LanguageType
import com.inout.apiserver.base.enums.LexicalCategoryType
import com.inout.apiserver.domain.user.User
import com.inout.apiserver.interfaces.web.v1.apiSpec.WordApiSpec
import com.inout.apiserver.interfaces.web.v1.request.CreateWordRequest
import com.inout.apiserver.interfaces.web.v1.response.ResponsePaginationWrapper
import com.inout.apiserver.interfaces.web.v1.response.SentenceResponse
import com.inout.apiserver.interfaces.web.v1.response.UserSentenceResponse
import com.inout.apiserver.interfaces.web.v1.response.WordResponse
import com.inout.apiserver.interfaces.web.v1.response.WordWithDefinitionsResponse
import jakarta.validation.Valid
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.springframework.data.domain.Pageable
import org.springframework.http.HttpStatus.CREATED
import org.springframework.http.HttpStatus.OK
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/v1/words")
class WordController(
    private val createWordApplication: CreateWordApplication,
    private val readWordsApplication: ReadWordsApplication,
    private val readSentencesApplication: ReadSentencesApplication,
    private val createSentenceApplication: CreateSentenceApplication,
    private val selectSentenceApplication: SelectSentenceApplication,
) : WordApiSpec {
    override fun createWord(
        @RequestBody @Valid request: CreateWordRequest,
    ): ResponseEntity<WordResponse> {
        val wordResponse = createWordApplication.run(request)
        // TODO: send it to some sort of queue implemented later (e.g. Kafka)
        CoroutineScope(Dispatchers.IO).launch {
            createSentenceApplication.run(wordResponse.id)
        }
        return ResponseEntity(wordResponse, CREATED)
    }

    override fun readWordsWithMatchingPrefix(
        pageable: Pageable,
        fromLanguage: LanguageType,
        toLanguage: LanguageType,
        prefix: String,
        lexicalCategory: LexicalCategoryType?,
    ): ResponseEntity<ResponsePaginationWrapper<WordWithDefinitionsResponse>> {
        val (count, words) =
            readWordsApplication.run(fromLanguage, toLanguage, prefix, lexicalCategory, pageable)

        return ResponseEntity(
            ResponsePaginationWrapper(
                data = words.map { WordWithDefinitionsResponse.of(it, lexicalCategory) },
                hasMore = pageable.next().offset < count,
                count = count,
            ),
            OK,
        )
    }

    override fun readSentencesByWordDefinitionId(wordDefinitionId: Long): ResponseEntity<List<SentenceResponse>> {
        val sentences = readSentencesApplication.run(wordDefinitionId)

        return ResponseEntity(
            sentences.map { SentenceResponse.of(it) },
            OK,
        )
    }

    override fun selectSentence(
        wordId: Long,
        wordDefinitionId: Long,
        sentenceId: Long,
        user: User,
    ) = ResponseEntity(
        UserSentenceResponse.of(selectSentenceApplication.run(wordId, wordDefinitionId, sentenceId, user)),
        CREATED,
    )
}
