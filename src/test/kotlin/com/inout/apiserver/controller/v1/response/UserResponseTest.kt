package com.inout.apiserver.controller.v1.response

import com.inout.apiserver.domain.User
import com.inout.apiserver.error.InOutRequireNotNullException
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class UserResponseTest {
    @Test
    fun `from - should throw error when id does not exist`() {
        // given
        val user = User(id = null, email = "email@1.com", password = "password", nickname = "nickname", createdAt = null, updatedAt = null)

        // when
        val result = assertThrows(InOutRequireNotNullException::class.java) {
            UserResponse.from(user)
        }

        // then
        assertEquals("User id is null", result.message)
        assertEquals("IORNN_USER_1", result.code)
    }

    @Test
    fun `from - should correctly convert user to user response`() {
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
        val result = UserResponse.from(user)

        // then
        assertEquals(user.id, result.id)
        assertEquals(user.email, result.email)
        assertEquals(user.nickname, result.nickname)
    }
}