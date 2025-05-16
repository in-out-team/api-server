package com.inout.apiserver.infrastructure.security

import com.inout.apiserver.infrastructure.mongo.user.UserRepository
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.stereotype.Service
import org.springframework.security.core.userdetails.User as SecurityUser

@Service
class CustomUserDetailsService(
    private val userRepository: UserRepository,
) : UserDetailsService {
    override fun loadUserByUsername(username: String): UserDetails =
        userRepository
            .findByEmail(username)
            ?.let { user ->
                SecurityUser
                    .builder()
                    .username(user.email) // email is used as username in our service
                    .password(user.password)
                    .roles(*user.roles.map { it.uppercase() }.toTypedArray())
                    .build()
            }
            ?: throw UsernameNotFoundException("User not found")
}
