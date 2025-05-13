package com.inout.apiserver.domain.user

import com.inout.apiserver.infrastructure.mongo.user.MongoUser
import com.inout.apiserver.infrastructure.mongo.user.MongoUserRepository
import org.springframework.stereotype.Component

@Component
class MongoUserFactory(
    private val userRepository: MongoUserRepository,
) {
    fun createUser(
        email: String = "test@1.com",
        password: String = "password",
        nickname: String = "test",
        timezone: String = "UTC",
    ): MongoUser =
        userRepository
            .save(
                MongoUser(
                    email = email,
                    password = password,
                    nickname = nickname,
                    timezone = timezone,
                ),
            ).let {
                userRepository.findById(it.id!!).get()
            }
}
