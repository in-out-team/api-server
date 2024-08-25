package com.inout.apiserver.interfaces.web.v1

import com.inout.apiserver.application.study.CreateStudyApplication
import com.inout.apiserver.interfaces.web.v1.request.CreateStudyRequest
import com.inout.apiserver.interfaces.web.v1.response.StudyWithWordResponse
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/v1/studies")
class StudyController(
    private val createStudyApplication: CreateStudyApplication,
) {
    @PostMapping
    fun createStudy(@RequestBody @Valid request: CreateStudyRequest): ResponseEntity<StudyWithWordResponse> {
        val userId = 1L // TODO: fix this with actual user id
        return ResponseEntity(createStudyApplication.run(request, userId), HttpStatus.CREATED)
    }
}
