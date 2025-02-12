package com.inout.apiserver.interfaces.web.v1

import com.inout.apiserver.application.word.CreateSentenceApplication
import com.inout.apiserver.application.word.CreateWordApplication
import com.inout.apiserver.application.word.ReadWordsApplication
import com.inout.apiserver.base.enums.LanguageType
import com.inout.apiserver.base.enums.LexicalCategoryType
import com.inout.apiserver.interfaces.web.v1.apiSpec.WordApiSpec
import com.inout.apiserver.interfaces.web.v1.request.CreateWordRequest
import com.inout.apiserver.interfaces.web.v1.response.ResponsePaginationWrapper
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
    private val createSentenceApplication: CreateSentenceApplication,
) : WordApiSpec {
    override fun createWord(
        @RequestBody @Valid request: CreateWordRequest,
    ): ResponseEntity<WordResponse> {
        val result =
            createWordApplication.run(
                CreateWordApplication.Request(
                    name = request.name,
                    fromLanguage = request.fromLanguage,
                    toLanguage = request.toLanguage,
                ),
            )
        // TODO: send it to some sort of queue implemented later (e.g. Kafka)
        CoroutineScope(Dispatchers.IO).launch {
            createSentenceApplication.run(result.word.id!!)
        }
        return ResponseEntity(
            WordResponse.of(result.word),
            CREATED,
        )
    }

    override fun readWordsWithMatchingPrefix(
        pageable: Pageable,
        fromLanguage: LanguageType,
        toLanguage: LanguageType,
        prefix: String,
        lexicalCategory: LexicalCategoryType?,
    ): ResponseEntity<ResponsePaginationWrapper<WordWithDefinitionsResponse>> {
        val (count, words) =
            readWordsApplication.run(
                ReadWordsApplication.Request(
                    fromLanguage = fromLanguage,
                    toLanguage = toLanguage,
                    prefix = prefix,
                    lexicalCategoryType = lexicalCategory,
                    pageable = pageable,
                ),
            )

        return ResponseEntity(
            ResponsePaginationWrapper(
                data = words.map { WordWithDefinitionsResponse.of(it, lexicalCategory) },
                hasMore = pageable.next().offset < count,
                count = count,
            ),
            OK,
        )
    }
}
