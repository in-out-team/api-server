package com.inout.apiserver.service.boot

import com.inout.apiserver.repository.user.UserRepository
import org.springframework.security.core.userdetails.User as SecurityUser
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.stereotype.Service

@Service
class CustomUserDetailsService(
    private val userRepository: UserRepository
) : UserDetailsService {
    override fun loadUserByUsername(username: String): UserDetails {
        return userRepository.findByEmail(username)
            ?.let { user ->
                SecurityUser.builder()
                    .username(user.email) // email is used as username in our service
                    .password(user.password)
                    .roles("USER") // TODO: fix according to actual user role
                    .build()
            }
            ?: throw UsernameNotFoundException("User not found")
    }
}