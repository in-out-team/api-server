package com.inout.apiserver.application.user

import com.inout.apiserver.interfaces.web.v1.request.CreateUserRequest
import com.inout.apiserver.error.InOutRequireNotNullException
import com.inout.apiserver.domain.user.UserService
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class CreateUserApplicationTest {
    private val userService = mockk<UserService>()
    private val createUserApplication = CreateUserApplication(userService)

    @Test
    fun `run - should raise error to the caller when creating user fails`() {
        // given
        val request = mockk<CreateUserRequest>()
        every { userService.createUser(request) } throws RuntimeException("Failed to create user")

        // when
        val error = assertThrows(RuntimeException::class.java) {
            createUserApplication.run(request)
        }

        // then
        assertEquals("Failed to create user", error.message)
    }

    @Test
    fun `run - should throw InOutRequireNotNullException when user id is null`() {
        // given
        val request = mockk<CreateUserRequest>()
        every { userService.createUser(request) } returns mockk { every { id } returns null }

        // when
        val error = assertThrows(InOutRequireNotNullException::class.java) {
            createUserApplication.run(request)
        }

        // then
        assertEquals("User id is null", error.message)
        assertEquals("IORNN_USER_1", error.code)
    }

    @Test
    fun `run - should return UserResponse when user is created successfully`() {
        // given
        val request = mockk<CreateUserRequest>()
        every { userService.createUser(request) } returns mockk {
            every { id } returns 1L
            every { email } returns "email@1.com"
            every { nickname } returns "nickname"
        }

        // when
        val response = createUserApplication.run(request)

        // then
        assertEquals(1L, response.id)
        assertEquals("email@1.com", response.email)
        assertEquals("nickname", response.nickname)
    }
}