package com.inout.apiserver.application.user

import com.inout.apiserver.domain.user.User
import com.inout.apiserver.domain.user.UserService
import com.inout.apiserver.interfaces.web.v1.request.UpdateUserRequest
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test

class UpdateUserApplicationTest {
    private val userService = mockk<UserService>()
    private val updateUserApplication = UpdateUserApplication(userService)
    private val user =
        mockk<User> {
            every { id } returns 1L
            every { email } returns "email@1.com"
            every { nickname } returns "nickname"
        }

    @Test
    fun `run - should raise error to the caller when updating user fails`() {
        // given
        val request =
            mockk<UpdateUserRequest> {
                every { id } returns 1L
            }
        every { userService.updateUser(request) } throws RuntimeException("Failed to update user")

        // when
        val error =
            assertThrows(RuntimeException::class.java) {
                updateUserApplication.run(request, user)
            }

        // then
        assertEquals("Failed to update user", error.message)
    }

    @Test
    fun `run - should return UserResponse when user is updated successfully`() {
        // given
        val request =
            mockk<UpdateUserRequest> {
                every { id } returns 1L
            }
        every { userService.updateUser(request) } returns user

        // when
        val result = updateUserApplication.run(request, user)

        // then
        assertEquals(1L, result.id)
        assertEquals("email@1.com", result.email)
        assertEquals("nickname", result.nickname)
    }
}
