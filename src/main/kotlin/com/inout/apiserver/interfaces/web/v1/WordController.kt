package com.inout.apiserver.interfaces.web.v1

import com.inout.apiserver.application.word.CreateWordApplication
import com.inout.apiserver.application.word.ReadWordApplication
import com.inout.apiserver.base.enums.LexicalCategoryType
import com.inout.apiserver.interfaces.web.v1.apiSpec.WordApiSpec
import com.inout.apiserver.interfaces.web.v1.request.CreateWordRequest
import com.inout.apiserver.interfaces.web.v1.request.ReadWordRequest
import com.inout.apiserver.interfaces.web.v1.response.ResponsePaginationWrapper
import com.inout.apiserver.interfaces.web.v1.response.WordDefinitionResponse
import com.inout.apiserver.interfaces.web.v1.response.WordResponse
import jakarta.validation.Valid
import org.springframework.data.domain.Pageable
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.http.HttpStatus.*

@RestController
@RequestMapping("/v1/words")
class WordController(
    private val createWordApplication: CreateWordApplication,
    private val readWordApplication: ReadWordApplication,
) : WordApiSpec {
    override fun createWord(@RequestBody @Valid request: CreateWordRequest): ResponseEntity<WordResponse> {
        return ResponseEntity(createWordApplication.run(request), CREATED)
    }

    override fun readWord(@RequestBody @Valid request: ReadWordRequest): ResponseEntity<WordResponse> {
        return ResponseEntity(readWordApplication.run(request), OK)
    }

    override fun readWordDefinitionsWithMatchingPrefix(
        pageable: Pageable,
        prefix: String,
        lexicalCategoryType: LexicalCategoryType?
    ): ResponseEntity<ResponsePaginationWrapper<WordDefinitionResponse>> {
        return ResponseEntity(ResponsePaginationWrapper(emptyList(), 0, 0, 0), OK)
    }
}
