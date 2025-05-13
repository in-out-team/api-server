package com.inout.apiserver.application.user

import com.inout.apiserver.base.enums.LanguageType
import com.inout.apiserver.domain.user.MongoUserService
import com.inout.apiserver.infrastructure.mongo.user.MongoUser
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
@Transactional
class UpdateUserApplication(
    private val userService: MongoUserService,
) {
    data class Request(
        val user: MongoUser,
        val newNickname: String,
        val studyLanguage: LanguageType,
        val nativeLanguage: LanguageType,
        val studyPerDay: Int,
        val timezone: String,
    )

    data class Response(
        val updatedUser: MongoUser,
    )

    fun run(request: Request): Response {
        val updatedUser =
            userService.updateUser(
                user = request.user,
                nickname = request.newNickname,
                studyLanguage = request.studyLanguage,
                nativeLanguage = request.nativeLanguage,
                studyPerDay = request.studyPerDay,
                timezone = request.timezone,
            )
        return Response(updatedUser = updatedUser)
    }
}
