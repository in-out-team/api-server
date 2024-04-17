package com.inout.apiserver.domain.user

import com.inout.apiserver.domain.user.User
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.time.LocalDateTime

class UserTest {
    private val now = LocalDateTime.now()

    @Test
    fun `toEntity - returns UserEntity`() {
        // given
        val user = User(id = 1L, email = "test@email.com", password = "password", nickname = "nickname", createdAt = now, updatedAt = now)

        // when
        val userEntity = user.toEntity()

        // then
        assertEquals(user.id, userEntity.id)
        assertEquals(user.email, userEntity.email)
        assertEquals(user.password, userEntity.password)
        assertEquals(user.nickname, userEntity.nickname)
        assertEquals(user.createdAt, userEntity.createdAt)
        assertEquals(user.updatedAt, userEntity.updatedAt)
    }
}