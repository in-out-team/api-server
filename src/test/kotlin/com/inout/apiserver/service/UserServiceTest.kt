package com.inout.apiserver.service

import com.inout.apiserver.controller.v1.request.CreateUserRequest
import com.inout.apiserver.domain.User
import com.inout.apiserver.error.ConflictException
import com.inout.apiserver.repository.user.UserRepository
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class UserServiceTest {
    private val userRepository = mockk<UserRepository>()
    private val userService = UserService(userRepository)

    @Test
    fun `createUser - should raise error when user already exists`() {
        // Given
        val email = "email@1.com"
        val request = CreateUserRequest(email = email, password = "password", nickname = "nickname")
        val existingUser = User(id = 1L, email = email, password = "password", nickname = "nickname")
        every { userRepository.findByEmail(email) } returns existingUser

        // When
        val exception = assertThrows<ConflictException> {
            userService.createUser(request)
        }

        // Then
        assertEquals("User already exists", exception.message)
        assertEquals("USER_1", exception.code)
    }

    @Test
    fun `createUser - should save user when user does not exist`() {
        // Given
        val email = "email@1.com"
        val request = CreateUserRequest(email = email, password = "password", nickname = "nickname")
        val newUser = User(id = 1L, email = email, password = "password", nickname = "nickname")
        every { userRepository.findByEmail(email) } returns null
        every { userRepository.save(any()) } returns newUser

        // When
        val result = userService.createUser(request)

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
        val user = User(id = 1L, email = email, password = "password", nickname = "nickname")
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
}















