package com.inout.apiserver.interfaces.web.v1

import com.inout.apiserver.application.study.CreateStudyApplication
import com.inout.apiserver.application.study.RateStudyWordApplication
import com.inout.apiserver.application.study.ReadOrCreateDailyStudySetApplication
import com.inout.apiserver.application.study.ReadStudiesApplication
import com.inout.apiserver.config.web.RequestUser
import com.inout.apiserver.domain.user.User
import com.inout.apiserver.interfaces.web.v1.apiSpec.StudyApiSpec
import com.inout.apiserver.interfaces.web.v1.request.CreateStudyRequest
import com.inout.apiserver.interfaces.web.v1.request.RateStudyWordRequest
import com.inout.apiserver.interfaces.web.v1.response.DailyStudySetResponse
import com.inout.apiserver.interfaces.web.v1.response.ResponsePaginationWrapper
import com.inout.apiserver.interfaces.web.v1.response.StudyWordResponse
import io.swagger.v3.oas.annotations.Parameter
import jakarta.validation.Valid
import org.springframework.data.domain.Pageable
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
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
    override fun createStudy(
        @RequestBody @Valid request: CreateStudyRequest,
        @Parameter(hidden = true) @RequestUser user: User,
    ): ResponseEntity<StudyWordResponse> {
        return ResponseEntity(
            createStudyApplication.run(request, user.id).let { StudyWordResponse.of(it.study, it.word) },
            HttpStatus.CREATED,
        )
    }

    override fun getStudies(
        @Parameter(hidden = true) @RequestUser user: User,
        @Parameter(hidden = true) pageable: Pageable,
    ): ResponseEntity<ResponsePaginationWrapper<StudyWordResponse>> {
        val (count, studyWithWords) = readStudiesApplication.run(user.id, pageable)
        return ResponseEntity(
            ResponsePaginationWrapper(
                data = studyWithWords.map { StudyWordResponse.of(it.study, it.word) },
                hasMore = pageable.next().offset < count,
                count = count,
            ),
            HttpStatus.OK,
        )
    }

    override fun getDailyStudySet(
        @Parameter(hidden = true) @RequestUser user: User,
        @RequestParam(name = "date", required = true) date: LocalDate,
    ): ResponseEntity<DailyStudySetResponse> {
        val dailyStudySetResult = readOrCreateDailyStudySetApplication.run(user.id, date)
        return ResponseEntity(
            DailyStudySetResponse.of(dailyStudySetResult.dailyStudySet, dailyStudySetResult.studyWords),
            HttpStatus.OK,
        )
    }

    override fun rateStudyWord(
        @Parameter(hidden = true) @RequestUser user: User,
        @RequestBody @Valid request: RateStudyWordRequest,
    ): ResponseEntity<StudyWordResponse> {
        val rateStudyResult =
            rateStudyWordApplication.run(
                userId = user.id,
                studyId = request.studyId,
                dailyStudySetId = request.dailyStudySetId,
                rating = request.rating,
            )
        return ResponseEntity(
            StudyWordResponse.of(rateStudyResult.studyWord.study, rateStudyResult.studyWord.word),
            HttpStatus.OK,
        )
    }
}
