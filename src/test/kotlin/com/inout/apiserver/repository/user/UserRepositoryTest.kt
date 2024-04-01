package com.inout.apiserver.repository.user

import com.inout.apiserver.domain.User
import com.inout.apiserver.entity.UserEntity
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class UserRepositoryTest {
    private val userJpaRepository = mockk<UserJpaRepository>()
    private val userRepository = UserRepository(userJpaRepository)

    @Test
    fun `findByEmail - should return null when user not found`() {
        // given
        val email = "email@1.com"
        every { userJpaRepository.findByEmail(email) } returns null

        // when
        val result = userRepository.findByEmail(email)

        // then
        assertNull(result)
    }

    @Test
    fun `findByEmail - should return user when user found`() {
        // given
        val email = "email@1.com"
        val userEntity = UserEntity(
            id = 1L,
            email = email,
            password = "password",
            nickname = "nickname"
        )
        every { userJpaRepository.findByEmail(email) } returns userEntity

        // when
        val result = userRepository.findByEmail(email)

        // then
        assertNotNull(result)
        assertEquals(userEntity.id, result?.id)
        assertEquals(userEntity.email, result?.email)
        assertEquals(userEntity.password, result?.password)
        assertEquals(userEntity.nickname, result?.nickname)
    }

    @Test
    fun `save - should return user`() {
        // given
        val user = UserEntity(
            id = 1L,
            email = "email@1.com",
            password = "password",
            nickname = "nickname"
        )
        every { userJpaRepository.save(any()) } returns user

        // when
        val result = userRepository.save(
            User.newOf(email = user.email, password = user.password, nickname = user.nickname)
        )

        // then
        assertNotNull(result)
        assertEquals(user.id, result.id)
        assertEquals(user.email, result.email)
        assertEquals(user.password, result.password)
        assertEquals(user.nickname, result.nickname)
    }
}