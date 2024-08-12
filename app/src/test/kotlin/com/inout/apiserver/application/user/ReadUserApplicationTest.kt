package com.inout.apiserver.application.user

import com.inout.apiserver.domain.user.User
import com.inout.apiserver.error.NotFoundException
import com.inout.apiserver.domain.user.UserService
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class ReadUserApplicationTest {
    private val userService = mockk<UserService>()
    private val readUserApplication = ReadUserApplication(userService)

    @Test
    fun `run - should raise error when user not found`() {
        // given
        val id = 1L
        every { userService.getUserById(id) } returns null

        // when
        val exception = assertThrows(NotFoundException::class.java) {
            readUserApplication.run(id)
        }

        // then
        assertEquals("User not found", exception.message)
    }

    @Test
    fun `run - should return user response`() {
        // given
        val id = 1L
        val user = User(id = id, email = "email@1.com", nickname = "nickname1", password = "password1", createdAt = null, updatedAt = null)
        every { userService.getUserById(id) } returns user

        // when
        val userResponse = readUserApplication.run(id)

        // then
        assertEquals(user.id, userResponse.id)
        assertEquals(user.email, userResponse.email)
        assertEquals(user.nickname, userResponse.nickname)
    }
}