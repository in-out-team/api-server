package com.inout.apiserver.interfaces.web.v1

import com.inout.apiserver.application.study.CreateStudyApplication
import com.inout.apiserver.application.study.RateStudyWordApplication
import com.inout.apiserver.application.study.ReadOrCreateDailyStudySetApplication
import com.inout.apiserver.application.study.ReadStudiesApplication
import com.inout.apiserver.config.web.RequestMongoUser
import com.inout.apiserver.config.web.RequestUser
import com.inout.apiserver.error.HttpException
import com.inout.apiserver.infrastructure.db.user.User
import com.inout.apiserver.infrastructure.mongo.user.MongoUser
import com.inout.apiserver.interfaces.web.v1.apiSpec.StudyApiSpec
import com.inout.apiserver.interfaces.web.v1.request.CreateStudyRequest
import com.inout.apiserver.interfaces.web.v1.request.RateStudyWordRequest
import com.inout.apiserver.interfaces.web.v1.response.MongoDailyStudySetResponse
import com.inout.apiserver.interfaces.web.v1.response.MongoStudyWordResponse
import com.inout.apiserver.interfaces.web.v1.response.ResponsePaginationWrapper
import com.inout.apiserver.interfaces.web.v1.response.StudyWordResponse
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import jakarta.validation.Valid
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
import java.time.LocalDate

@RestController
@RequestMapping("/v1/studies")
class StudyController(
    private val createStudyApplication: CreateStudyApplication,
    private val readStudiesApplication: ReadStudiesApplication,
    private val readOrCreateDailyStudySetApplication: ReadOrCreateDailyStudySetApplication,
    private val rateStudyWordApplication: RateStudyWordApplication,
) : StudyApiSpec {
    @PostMapping
    @Operation(
        summary = "단어장에 단어 추가",
        description = "단어장에 학습할 단어를 추가합니다.",
        requestBody =
            io.swagger.v3.oas.annotations.parameters.RequestBody(
                description = "학습 단어 추가 요청값",
                required = true,
                content = [
                    Content(
                        mediaType = "application/json",
                        schema = Schema(implementation = CreateStudyRequest::class),
                    ),
                ],
            ),
        responses = [
            ApiResponse(
                responseCode = "201",
                description = "학습 단어 추가 성공",
                content = [
                    Content(
                        mediaType = "application/json",
                        schema = Schema(implementation = MongoStudyWordResponse::class),
                    ),
                ],
            ),
            ApiResponse(
                responseCode = "404",
                description = "학습 단어 추가 실패 (code: WORD_2) - 존재하지 않는 단어",
                content = [
                    Content(
                        mediaType = "application/json",
                        schema = Schema(implementation = HttpException::class),
                    ),
                ],
            ),
            ApiResponse(
                responseCode = "409",
                description = "학습 단어 추가 실패 (code: STUDY_1) - 이미 단어장에 추가된 단어",
                content = [
                    Content(
                        mediaType = "application/json",
                        schema = Schema(implementation = HttpException::class),
                    ),
                ],
            ),
        ],
    )
    fun createStudy(
        @RequestBody @Valid request: CreateStudyRequest,
        @Parameter(hidden = true) @RequestMongoUser user: MongoUser,
    ): ResponseEntity<MongoStudyWordResponse> =
        ResponseEntity(
            createStudyApplication
                .run(
                    CreateStudyApplication.Request(
                        user = user,
                        wordDefinitionId = request.wordDefinitionId,
                    ),
                ).let { MongoStudyWordResponse.of(study = it.studyWord.study, word = it.studyWord.word) },
            CREATED,
        )

    @GetMapping
    @Operation(
        summary = "학습 단어 조회",
        description = "요청자의 단어장에 있는 학습 단어를 조회합니다.",
        parameters = [
            Parameter(
                name = "page",
                description = "페이지 번호 (0부터 시작)",
                required = false,
                example = "0",
            ),
            Parameter(
                name = "size",
                description = "페이지 크기",
                required = false,
                example = "20",
            ),
            Parameter(
                name = "sort",
                description = "정렬 조건 (createdAt, due, lastReview 지원)",
                required = false,
                example = "createdAt,desc",
            ),
            Parameter(
                name = "wordNamePrefix",
                description = "단어 이름 접두어",
                required = false,
                example = "apple",
            ),
        ],
        responses = [
            ApiResponse(
                responseCode = "200",
                description = "학습 단어 조회 성공",
                useReturnTypeSchema = true,
            ),
        ],
    )
    fun getStudies(
        @Parameter(hidden = true) @RequestMongoUser user: MongoUser,
        @Parameter(hidden = true) pageable: Pageable,
        @RequestParam(required = false) wordNamePrefix: String?,
    ): ResponseEntity<ResponsePaginationWrapper<MongoStudyWordResponse>> {
        val (count, studyWithWords) =
            readStudiesApplication.run(
                ReadStudiesApplication.Request(
                    user = user,
                    wordNamePrefix = wordNamePrefix,
                    pageable = pageable,
                ),
            )
        return ResponseEntity(
            ResponsePaginationWrapper(
                data = studyWithWords.map { MongoStudyWordResponse.of(it.study, it.word) },
                hasMore = pageable.next().offset < count,
                count = count,
            ),
            OK,
        )
    }

    @GetMapping("/daily")
    @Operation(
        summary = "일일 학습셋 조회",
        description = "요청자의 일일 학습셋을 조회합니다.",
        parameters = [
            Parameter(
                name = "date",
                description = "조회할 일자",
                required = true,
                example = "2024-09-29",
            ),
        ],
        responses = [
            ApiResponse(
                responseCode = "200",
                description = "일일 학습 진행 상황 조회 성공",
                content = [
                    Content(
                        mediaType = "application/json",
                        schema = Schema(implementation = MongoDailyStudySetResponse::class),
                    ),
                ],
            ),
        ],
    )
    fun getDailyStudySet(
        @Parameter(hidden = true) @RequestMongoUser user: MongoUser,
        @RequestParam(name = "date", required = true) date: LocalDate,
    ): ResponseEntity<MongoDailyStudySetResponse> {
        val dailyStudySetResult =
            readOrCreateDailyStudySetApplication.run(
                ReadOrCreateDailyStudySetApplication.Request(user = user, date = date),
            )
        return ResponseEntity(
            MongoDailyStudySetResponse.of(dailyStudySetResult.dailyStudySet, dailyStudySetResult.studyWords),
            OK,
        )
    }

    override fun rateStudyWord(
        @Parameter(hidden = true) @RequestUser user: User,
        @RequestBody @Valid request: RateStudyWordRequest,
    ): ResponseEntity<StudyWordResponse> {
        val rateStudyResult =
            rateStudyWordApplication.run(
                RateStudyWordApplication.Request(
                    user = user,
                    dailyStudySetId = request.dailyStudySetId,
                    studyId = request.studyId,
                    rating = request.rating,
                ),
            )
        return ResponseEntity(
            StudyWordResponse.of(rateStudyResult.studyWord.study, rateStudyResult.studyWord.word),
            OK,
        )
    }
}
