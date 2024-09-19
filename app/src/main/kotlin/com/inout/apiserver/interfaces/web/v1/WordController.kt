package com.inout.apiserver.interfaces.web.v1

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
) : WordApiSpec {
    override fun createWord(
        @RequestBody @Valid request: CreateWordRequest,
    ): ResponseEntity<WordResponse> {
        return ResponseEntity(createWordApplication.run(request), CREATED)
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
}
