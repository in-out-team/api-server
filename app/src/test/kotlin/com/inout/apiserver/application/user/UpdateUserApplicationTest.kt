package com.inout.apiserver.application.user

import com.inout.apiserver.interfaces.web.v1.request.UpdateUserRequest
import com.inout.apiserver.domain.user.UserService
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class UpdateUserApplicationTest {
    private val userService = mockk<UserService>()
    private val updateUserApplication = UpdateUserApplication(userService)

    @Test
    fun `run - should raise error to the caller when updating user fails`() {
        // given
        val request = mockk<UpdateUserRequest>()
        every { userService.updateUser(request) } throws RuntimeException("Failed to update user")

        // when
        val error = assertThrows(RuntimeException::class.java) {
            updateUserApplication.run(request)
        }

        // then
        assertEquals("Failed to update user", error.message)
    }

    @Test
    fun `run - should return UserResponse when user is updated successfully`() {
        // given
        val request = mockk<UpdateUserRequest>()
        every { userService.updateUser(request) } returns mockk {
            every { id } returns 1L
            every { email } returns "email@1.com"
            every { nickname } returns "nickname"
        }

        // when
        val result = updateUserApplication.run(request)

        // then
        assertEquals(1L, result.id)
        assertEquals("email@1.com", result.email)
        assertEquals("nickname", result.nickname)
    }
}