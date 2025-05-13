package com.inout.apiserver.infrastructure.security

import com.inout.apiserver.infrastructure.mongo.user.MongoUser
import com.inout.apiserver.infrastructure.mongo.user.MongoUserRepository
import io.mockk.every
import io.mockk.mockk
import org.bson.types.ObjectId
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.springframework.security.core.userdetails.UsernameNotFoundException

class CustomUserDetailsServiceTest {
    private val userRepository = mockk<MongoUserRepository>()
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
            MongoUser(id = ObjectId(), email = email, password = "password", nickname = "test1")
        every { userRepository.findByEmail(email) } returns user

        // when
        val userDetails = customUserDetailsService.loadUserByUsername(email)

        // then
        Assertions.assertEquals(email, userDetails.username)
        Assertions.assertEquals("password", userDetails.password)
        Assertions.assertEquals("ROLE_USER", userDetails.authorities.first().authority)
    }
}
