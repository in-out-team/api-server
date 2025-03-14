package com.inout.apiserver.interfaces.web.v1

import com.inout.apiserver.application.word.CreateSentenceApplication
import com.inout.apiserver.application.word.CreateWordApplication
import com.inout.apiserver.application.word.ReadWordsApplication
import com.inout.apiserver.base.enums.LanguageType
import com.inout.apiserver.base.enums.LexicalCategoryType
import com.inout.apiserver.config.web.RequestUser
import com.inout.apiserver.error.HttpException
import com.inout.apiserver.infrastructure.db.user.User
import com.inout.apiserver.interfaces.web.v1.request.CreateWordRequest
import com.inout.apiserver.interfaces.web.v1.response.DictionaryWordWithDefinitionsResponse
import com.inout.apiserver.interfaces.web.v1.response.ResponsePaginationWrapper
import com.inout.apiserver.interfaces.web.v1.response.WordResponse
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import jakarta.validation.Valid
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.springdoc.core.converters.models.PageableAsQueryParam
import org.springframework.data.domain.Pageable
import org.springframework.http.HttpStatus.CREATED
import org.springframework.http.HttpStatus.OK
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/v1/words")
class WordController(
    private val createWordApplication: CreateWordApplication,
    private val readWordsApplication: ReadWordsApplication,
    private val createSentenceApplication: CreateSentenceApplication,
) {
    @PostMapping
    @Operation(
        summary = "사전 단어 등록",
        description = "사전 단어 등록을 요청합니다.",
        requestBody =
            io.swagger.v3.oas.annotations.parameters.RequestBody(
                description = "단어 생성 요청값",
                required = true,
                content = [
                    Content(
                        mediaType = "application/json",
                        schema = Schema(implementation = CreateWordRequest::class),
                    ),
                ],
            ),
        responses = [
            ApiResponse(
                responseCode = "201",
                description = "사전 단어 등록 성공",
                content = [
                    Content(
                        mediaType = "application/json",
                        schema = Schema(implementation = WordResponse::class),
                    ),
                ],
            ),
            ApiResponse(
                responseCode = "409",
                description = "사전 단어 등록 실패 (code: WORD_1) - 이미 존재하는 단어",
                content = [
                    Content(
                        mediaType = "application/json",
                        schema = Schema(implementation = HttpException::class),
                    ),
                ],
            ),
            // TODO: add more error codes (ex. in case openai service is down, or returns unexpected output)
        ],
    )
    fun createWord(
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
            createSentenceApplication.run(
                CreateSentenceApplication.Request(
                    wordId = result.word.id!!,
                ),
            )
        }
        return ResponseEntity(
            WordResponse.of(result.word),
            CREATED,
        )
    }

    @GetMapping("/definitions")
    @PageableAsQueryParam
    @Operation(
        summary = "사전 단어 조회, prefix와 lexicalCategoryType으로 필터링 가능",
        description = "사전 단어 정의 조회를 요청합니다.",
        parameters = [
            Parameter(
                name = "fromLanguage",
                description = "영한사전 기준 영어에 해당되는 값. ex) apple의 뜻을 한글로 알고싶을 경우 ENGLISH로 제공",
                required = true,
                schema =
                    Schema(
                        implementation = LanguageType::class,
                        example = "ENGLISH",
                    ),
            ),
            Parameter(
                name = "toLanguage",
                description = "영한사전 기준 한국어에 해당되는 값. ex) apple의 뜻을 한글로 알고싶을 경우 KOREAN으로 제공",
                required = true,
                schema =
                    Schema(
                        implementation = LanguageType::class,
                        example = "KOREAN",
                    ),
            ),
            Parameter(
                name = "prefix",
                description = "단어 prefix",
                required = true,
                schema = Schema(implementation = String::class),
            ),
            Parameter(
                name = "lexicalCategory",
                description = "단어 품사",
                required = false,
                schema = Schema(implementation = LexicalCategoryType::class),
            ),
        ],
        responses = [
            ApiResponse(
                responseCode = "200",
                description = "사전에 단어 정의 조회 성공",
                useReturnTypeSchema = true,
            ),
        ],
    )
    fun readWordsWithMatchingPrefix(
        @Parameter(hidden = true)
        pageable: Pageable,
        @RequestParam(required = true)
        fromLanguage: LanguageType,
        @RequestParam(required = true)
        toLanguage: LanguageType,
        @RequestParam(required = true)
        prefix: String,
        @RequestParam(required = false)
        lexicalCategory: LexicalCategoryType?,
        @Parameter(hidden = true) @RequestUser user: User,
    ): ResponseEntity<ResponsePaginationWrapper<DictionaryWordWithDefinitionsResponse>> {
        val (count, words, studyingWordDefinitionIds) =
            readWordsApplication.run(
                ReadWordsApplication.Request(
                    fromLanguage = fromLanguage,
                    toLanguage = toLanguage,
                    prefix = prefix,
                    lexicalCategoryType = lexicalCategory,
                    pageable = pageable,
                    user = user,
                ),
            )

        return ResponseEntity(
            ResponsePaginationWrapper(
                data =
                    words.map {
                        DictionaryWordWithDefinitionsResponse.of(
                            it,
                            lexicalCategory,
                            studyingWordDefinitionIds.toSet(),
                        )
                    },
                hasMore = pageable.next().offset < count,
                count = count,
            ),
            OK,
        )
    }
}
