package com.inout.apiserver.infrastructure.db.user

import com.inout.apiserver.domain.user.User
import com.inout.apiserver.error.InOutRequireNotNullException
import com.inout.apiserver.infrastructure.db.user.UserEntity
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.time.LocalDateTime

class UserEntityTest {
    private val now = LocalDateTime.now()

    @Test
    fun `toDomain - raises error when id is null`() {
        // given
        val userEntity = UserEntity(
            email = "test@email.com",
            password = "password",
            nickname = "nickname",
        )

        // when & then
        val error = assertThrows<InOutRequireNotNullException> {
            userEntity.toDomain()
        }
        assertEquals("User id is null", error.message)
        assertEquals("IORNN_USER_1", error.code)
    }

    @Test
    fun `toDomain - returns User`() {
        // given
        val email = "test@email.com"
        val nickname = "nickname"
        val userEntity = UserEntity(
            email = email,
            password = "password",
            nickname = nickname,
        ).apply {
            id = 1L
            createdAt = now
            updatedAt = now
        }

        // when
        val user = userEntity.toDomain()

        // then
        assertTrue(user is User)
        assertEquals(1L, user.id)
        assertEquals(email, user.email)
        assertEquals("password", user.password)
        assertEquals(nickname, user.nickname)
        assertEquals(now, user.createdAt)
        assertEquals(now, user.updatedAt)
    }
}