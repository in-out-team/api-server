package com.inout.apiserver.infrastructure.db.user

import com.inout.apiserver.domain.user.User
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.springframework.data.jpa.domain.AbstractPersistable_.id
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
        val userEntity = UserEntity(email = email, password = "password", nickname = "nickname")
            .apply { id = 1L; createdAt = now; updatedAt = now}
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
        val userEntity = UserEntity(email = "email@1.com", password = "password", nickname = "nickname")
            .apply { id = 1L; createdAt = now; updatedAt = now }
        every { userJpaRepository.save(any()) } returns userEntity

        // when
        val result = userRepository.save(userEntity)

        // then
        assertTrue(result is User)
        assertEquals(userEntity.id, result.id)
        assertEquals(userEntity.email, result.email)
        assertEquals(userEntity.password, result.password)
        assertEquals(userEntity.nickname, result.nickname)
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
        val userEntity = UserEntity(email = "email@1.com", password = "password", nickname = "nickname")
            .apply { id = 1L; createdAt = now; updatedAt = now }
        every { userJpaRepository.findById(1L) } returns Optional.of(userEntity)

        // when
        val result = userRepository.findById(1L)

        // then
        assertNotNull(result)
        assertEquals(userEntity.id, result?.id)
    }
}