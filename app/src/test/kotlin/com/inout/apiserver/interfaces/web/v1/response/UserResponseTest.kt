package com.inout.apiserver.interfaces.web.v1.response

import com.inout.apiserver.domain.user.User
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class UserResponseTest {

    @Test
    fun `of - should correctly convert user to user response`() {
        // given
        val user = User(
            id = 1,
            email = "email@1.com",
            password = "password",
            nickname = "nickname",
            createdAt = null,
            updatedAt = null
        )

        // when
        val result = UserResponse.of(user)

        // then
        assertEquals(user.id, result.id)
        assertEquals(user.email, result.email)
        assertEquals(user.nickname, result.nickname)
    }
}