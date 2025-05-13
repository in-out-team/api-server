package com.inout.apiserver.interfaces.web.v1

import com.inout.apiserver.application.user.CreateUserApplication
import com.inout.apiserver.application.user.ReadUserApplication
import com.inout.apiserver.application.user.UpdateUserApplication
import com.inout.apiserver.config.web.RequestUser
import com.inout.apiserver.infrastructure.db.user.User
import com.inout.apiserver.interfaces.web.v1.apiSpec.UserApiSpec
import com.inout.apiserver.interfaces.web.v1.request.CreateUserRequest
import com.inout.apiserver.interfaces.web.v1.request.UpdateUserRequest
import com.inout.apiserver.interfaces.web.v1.response.MongoUserResponse
import com.inout.apiserver.interfaces.web.v1.response.UserResponse
import jakarta.validation.Valid
import org.springframework.http.HttpStatus.CREATED
import org.springframework.http.HttpStatus.OK
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/v1/users")
class UserController(
    private val createUserApplication: CreateUserApplication,
    private val updateUserApplication: UpdateUserApplication,
    private val readUserApplication: ReadUserApplication,
) : UserApiSpec {
    // TODO: should only be allowed in non-production environments
    override fun createUser(
        @RequestBody @Valid request: CreateUserRequest,
    ): ResponseEntity<MongoUserResponse> =
        ResponseEntity(
            MongoUserResponse.of(
                user =
                    createUserApplication
                        .run(
                            CreateUserApplication.Request(
                                email = request.email,
                                password = request.password,
                                nickname = request.nickname,
                            ),
                        ).newUser,
            ),
            CREATED,
        )

    override fun updateUser(
        @RequestBody @Valid request: UpdateUserRequest,
        @RequestUser user: User,
    ): ResponseEntity<UserResponse> =
        ResponseEntity(
            UserResponse.of(
                user =
                    updateUserApplication
                        .run(
                            UpdateUserApplication.Request(
                                user = user,
                                newNickname = request.nickname,
                                studyLanguage = request.studyLanguage,
                                nativeLanguage = request.nativeLanguage,
                                studyPerDay = request.studyPerDay,
                                timezone = request.timezone,
                            ),
                        ).updatedUser,
            ),
            OK,
        )

    override fun getUser(
        @PathVariable id: Long,
    ): ResponseEntity<UserResponse> =
        ResponseEntity(
            UserResponse.of(
                user = readUserApplication.run(ReadUserApplication.Request(id = id)).user,
            ),
            OK,
        )
}
