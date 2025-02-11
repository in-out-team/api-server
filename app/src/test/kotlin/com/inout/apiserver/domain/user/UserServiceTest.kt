package com.inout.apiserver.domain.user

import com.inout.apiserver.error.ConflictException
import com.inout.apiserver.infrastructure.db.user.User
import com.inout.apiserver.infrastructure.db.user.UserRepository
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.security.crypto.password.PasswordEncoder
import java.util.Optional

class UserServiceTest {
    private val userRepository = mockk<UserRepository>()
    private val passwordEncoder = mockk<PasswordEncoder>()
    private val userService = UserService(userRepository, passwordEncoder)

    @Test
    fun `createUser - should raise error when user already exists`() {
        // Given
        val email = "email@1.com"
        val password = "password"
        val nickname = "nickname"
        val existingUser =
            User(id = 1L, email = email, password = password, nickname = nickname)
        every { userRepository.findByEmail(email) } returns existingUser

        // When
        val exception =
            assertThrows<ConflictException> {
                userService.createUser(email = email, password = password, nickname = nickname)
            }

        // Then
        assertEquals("User already exists", exception.message)
        assertEquals("USER_1", exception.code)
    }

    @Test
    fun `createUser - should save user when user does not exist`() {
        // Given
        val email = "email@1.com"
        val password = "password"
        val nickname = "nickname"
        val newUser =
            User(id = 1L, email = email, password = password, nickname = nickname)
        every { userRepository.findByEmail(email) } returns null
        every { passwordEncoder.encode(any()) } returns password
        every { userRepository.save(any()) } returns newUser

        // When
        val result = userService.createUser(email = email, password = password, nickname = nickname)

        // Then
        assertEquals(newUser.email, result.email)
        assertEquals(newUser.nickname, result.nickname)
    }

    @Test
    fun `getUserByEmail - should search user by lowercase email`() {
        // Given
        val email = "Email@1.com"
        every { userRepository.findByEmail(email.lowercase()) } returns null

        // When
        val result = userService.getUserByEmail(email)

        // Then
        assertNull(result)
    }

    @Test
    fun `getUserByEmail - should return user when user exists`() {
        // Given
        val email = "email@1.com"
        val user =
            User(id = 1L, email = email, password = "password", nickname = "nickname")
        every { userRepository.findByEmail(email) } returns user

        // When
        val result = userService.getUserByEmail(email)

        // Then
        assertEquals(user.email, result?.email)
        assertEquals(user.nickname, result?.nickname)
    }

    @Test
    fun `getUserByEmail - should return null when user does not exist`() {
        // Given
        val email = "email@1.com"
        every { userRepository.findByEmail(email) } returns null

        // When
        val result = userService.getUserByEmail(email)

        // Then
        assertNull(result)
    }

    @Test
    fun `getUserById - should return user when user exists`() {
        // Given
        val id = 1L
        val user =
            User(
                id = 1L,
                email = "email@1.com",
                password = "password",
                nickname = "nickname",
            )
        every { userRepository.findById(id) } returns Optional.of(user)

        // When
        val result = userService.getUserById(id)

        // Then
        assertEquals(user.id, result?.id)
        assertEquals(user.email, result?.email)
        assertEquals(user.nickname, result?.nickname)
    }

    @Test
    fun `getUserById - should return null when user does not exist`() {
        // Given
        val id = 1L
        every { userRepository.findById(id) } returns Optional.empty()

        // When
        val result = userService.getUserById(id)

        // Then
        assertNull(result)
    }

    @Test
    fun `updateUser - should update user`() {
        // Given
        val user =
            User(
                id = 1L,
                email = "email@1.com",
                password = "password",
                nickname = "nickname",
            )
        val newNickname = "newNickname"
        val updatedUser = user.copy(nickname = newNickname)
        every { userRepository.findById(any()) } returns Optional.of(user)
        every { userRepository.save(any()) } returns updatedUser

        // When
        val result = userService.updateUser(user, newNickname)

        // Then
        assertEquals(user.id, result.id)
        assertEquals(user.email, result.email)
        assertEquals(user.password, result.password)
        assertEquals(newNickname, result.nickname)
        verify(exactly = 1) { userRepository.save(any()) }
    }
}
