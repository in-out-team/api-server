package com.inout.apiserver.infrastructure.security

import com.inout.apiserver.domain.user.User
import com.inout.apiserver.infrastructure.db.user.UserRepository
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.springframework.security.core.userdetails.UsernameNotFoundException

class CustomUserDetailsServiceTest {
    private val userRepository = mockk<UserRepository>()
    private val customUserDetailsService = CustomUserDetailsService(userRepository)
    private val email = "test@1.com"

    @Test
    fun `loadUserByUsername - should throw UsernameNotFoundException when user not found`() {
        // given
        every { userRepository.findByEmail(email) } returns null

        // when & then
        Assertions.assertThrows(UsernameNotFoundException::class.java) {
            customUserDetailsService.loadUserByUsername(email)
        }
    }

    @Test
    fun `loadUserByUsername - should return UserDetails when user found`() {
        // given
        val user =
            User(id = 1L, email = email, password = "password", nickname = "test1", createdAt = null, updatedAt = null)
        every { userRepository.findByEmail(email) } returns user

        // when
        val userDetails = customUserDetailsService.loadUserByUsername(email)

        // then
        Assertions.assertEquals(email, userDetails.username)
        Assertions.assertEquals("password", userDetails.password)
        Assertions.assertEquals("ROLE_USER", userDetails.authorities.first().authority)
    }
}