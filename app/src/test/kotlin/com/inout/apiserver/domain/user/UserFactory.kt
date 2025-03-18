package com.inout.apiserver.domain.user

import com.inout.apiserver.infrastructure.db.user.User
import com.inout.apiserver.infrastructure.db.user.UserRepository
import org.springframework.stereotype.Component

@Component
class UserFactory(
    private val userRepository: UserRepository,
) {
    fun createUser(
        email: String = "test@1.com",
        password: String = "password",
        nickname: String = "test",
    ): User =
        userRepository
            .save(
                User(
                    email = email,
                    password = password,
                    nickname = nickname,
                ),
            ).let {
                userRepository.findById(it.id!!).get()
            }
}
