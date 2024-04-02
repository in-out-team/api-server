package com.inout.apiserver.service

import com.inout.apiserver.controller.v1.request.CreateUserRequest
import com.inout.apiserver.controller.v1.request.UpdateUserRequest
import com.inout.apiserver.domain.User
import com.inout.apiserver.error.ConflictException
import com.inout.apiserver.error.NotFoundException
import com.inout.apiserver.repository.user.UserRepository
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.time.LocalDateTime

class UserServiceTest {
    private val userRepository = mockk<UserRepository>()
    private val userService = UserService(userRepository)
    private val now = LocalDateTime.now()

    @Test
    fun `createUser - should raise error when user already exists`() {
        // Given
        val email = "email@1.com"
        val request = CreateUserRequest(email = email, password = "password", nickname = "nickname")
        val existingUser = User(id = 1L, email = email, password = "password", nickname = "nickname", createdAt = now, updatedAt = now)
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
        val newUser = User(id = 1L, email = email, password = "password", nickname = "nickname", createdAt = now, updatedAt = now)
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
        val user = User(id = 1L, email = email, password = "password", nickname = "nickname", createdAt = now, updatedAt = now)
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
        val user = User(id = 1L, email = "email@1.com", password = "password", nickname = "nickname", createdAt = now, updatedAt = now)
        every { userRepository.findById(id) } returns user

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
        every { userRepository.findById(id) } returns null

        // When
        val result = userService.getUserById(id)

        // Then
        assertNull(result)
    }

    @Test
    fun `updateUser - should raise error when user does not exist`() {
        // Given
        val id = 1L
        val request = UpdateUserRequest(id = id, nickname = "nickname")
        every { userRepository.findById(id) } returns null

        // When
        val exception = assertThrows<NotFoundException> {
            userService.updateUser(request)
        }

        // Then
        assertEquals("User not found", exception.message)
        assertEquals("USER_2", exception.code)
    }

    @Test
    fun `updateUser - should update user`() {
        // Given
        val request = UpdateUserRequest(id = 1L, nickname = "nickname")
        val user = User(id = 1L, email = "email@1.com", password = "password", nickname = "nickname", createdAt = now, updatedAt = now)
        val updatedUser = user.copy(nickname = request.nickname)
        every { userRepository.findById(1L) } returns user
        every { userRepository.update(updatedUser) } returns updatedUser

        // When
        val result = userService.updateUser(request)

        // Then
        // check if everything is the same except for the updated part
        assertEquals(request.nickname, result.nickname)
        assertEquals(user.email, result.email)
        assertEquals(user.password, result.password)
    }
}















