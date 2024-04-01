package com.inout.apiserver.domain

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class UserTest {
    @Test
    fun `toEntity - returns UserEntity`() {
        // given
        val user = User(id = 1L, email = "test@email.com", password = "password", nickname = "nickname")
        // when
        val userEntity = user.toEntity()
        // then
        assertEquals(user.id, userEntity.id)
        assertEquals(user.email, userEntity.email)
        assertEquals(user.password, userEntity.password)
        assertEquals(user.nickname, userEntity.nickname)
    }
}