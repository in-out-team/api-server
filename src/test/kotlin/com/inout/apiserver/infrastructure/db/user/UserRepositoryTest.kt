package com.inout.apiserver.infrastructure.db.user

import com.inout.apiserver.domain.User
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.time.LocalDateTime
import java.util.*

class UserRepositoryTest {
    private val userJpaRepository = mockk<UserJpaRepository>()
    private val userRepository = UserRepository(userJpaRepository)
    private val now = LocalDateTime.now()

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
        val userEntity = UserEntity(id = 1L, email = email, password = "password", nickname = "nickname", createdAt = now, updatedAt = now)
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
        val user = UserEntity(id = 1L, email = "email@1.com", password = "password", nickname = "nickname", createdAt = now, updatedAt = now)
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

    @Test
    fun `update - should return updated user`() {
        // given
        val user = UserEntity(
            id = 1L,
            email = "email@1.com",
            password = "password",
            nickname = "nickname",
            createdAt = now,
            updatedAt = now
        )
        every { userJpaRepository.save(any()) } returns user

        // when
        val result = userRepository.update(user.toDomain())

        // then
        assertNotNull(result)
        assertEquals(user.id, result.id)
        assertEquals(user.email, result.email)
        assertEquals(user.password, result.password)
        assertEquals(user.nickname, result.nickname)
    }

    @Test
    fun `findById - should return null when user not found`() {
        // given
        val id = 1L
        every { userJpaRepository.findById(id) } returns Optional.empty()

        // when
        val result = userRepository.findById(id)

        // then
        assertNull(result)
    }

    @Test
    fun `findById - should return user when user found`() {
        // given
        val id = 1L
        val userEntity = UserEntity(id = id, email = "email@1.com", password = "password", nickname = "nickname", createdAt = now, updatedAt = now)
        every { userJpaRepository.findById(id) } returns Optional.of(userEntity)

        // when
        val result = userRepository.findById(id)

        // then
        assertNotNull(result)
        assertEquals(userEntity.id, result?.id)
    }
}