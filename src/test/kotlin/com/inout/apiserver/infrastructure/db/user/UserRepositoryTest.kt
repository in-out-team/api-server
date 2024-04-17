package com.inout.apiserver.infrastructure.db.user

import com.inout.apiserver.domain.user.User
import com.inout.apiserver.infrastructure.db.DbTestSupport
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.springframework.context.annotation.Import

@Import(UserRepository::class)
class UserRepositoryTest(
    private val userJpaRepository: UserJpaRepository,
    private val userRepository: UserRepository,
) : DbTestSupport() {
    private val testEmail = "test@1.com"
    private val testPassword = "password"


    @Test
    fun `save - should raise error when same email exists`() {
        // given
        val userEntity = UserEntity(email = testEmail, password = testPassword, nickname = "test1")
        userJpaRepository.save(userEntity)

        // when & then
        assertThatThrownBy {
            userRepository.save(UserEntity(email = testEmail, password = testPassword, nickname = "test2"))
        }
            .isInstanceOf(Exception::class.java)
            .hasMessageContaining("could not execute statement")
    }

    @Test
    fun `save - should return new saved user`() {
        // given
        val userEntity = UserEntity(email = testEmail, password = testPassword, nickname = "test1")

        // when
        val result = userRepository.save(userEntity)

        // then
        assertTrue(result is User)
        assertEquals(userEntity.email, result.email)
        assertEquals(userEntity.password, result.password)
        assertEquals(userEntity.nickname, result.nickname)
        assertNotNull(result.id)
        assertNotNull(result.createdAt)
        assertNotNull(result.updatedAt)
    }

    @Test
    fun `findByEmail - should return null when user not found`() {
        // given
        val userEntity = UserEntity(email = testEmail, password = testPassword, nickname = "test1")
        userJpaRepository.save(userEntity)

        // when
        val result = userRepository.findByEmail("1-${testEmail}")

        // then
        assertNull(result)
    }

    @Test
    fun `findByEmail - should return user when user found`() {
        // given
        val email = "test@1.com"
        val userEntity = UserEntity(email = email, password = "password", nickname = "test1")
        val savedUser = userJpaRepository.save(userEntity)

        // when
        val result = userRepository.findByEmail(email)

        // then
        assertTrue(result is User)
        assertEquals(userEntity.email, result?.email)
        assertEquals(userEntity.password, result?.password)
        assertEquals(userEntity.nickname, result?.nickname)
        assertEquals(savedUser.id, result?.id)
        assertEquals(savedUser.createdAt, result?.createdAt)
        assertEquals(savedUser.updatedAt, result?.updatedAt)
    }
}