package com.inout.apiserver.entity

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class UserEntityTest {
    @Test
    fun `toDomain - raises error when id is null`() {
        // given
        val userEntity = UserEntity(
            id = null,
            email = "test@email.com",
            password = "password",
            nickname = "nickname",
        )
        // when & then
        assertThrows<IllegalArgumentException> {
            userEntity.toDomain()
        }
    }

    @Test
    fun `toDomain - returns User`() {
        // given
        val email = "test@email.com"
        val nickname = "nickname"
        val userEntity = UserEntity(
            id = 1L,
            email = email,
            password = "password",
            nickname = nickname,
        )
        // when
        val user = userEntity.toDomain()
        // then
        assertEquals(1L, user.id)
        assertEquals(email, user.email)
        assertEquals("password", user.password)
        assertEquals(nickname, user.nickname)
    }
}