package com.inout.apiserver.service

import com.inout.apiserver.interfaces.web.v1.request.CreateUserRequest
import com.inout.apiserver.interfaces.web.v1.request.UpdateUserRequest
import com.inout.apiserver.domain.User
import com.inout.apiserver.error.ConflictException
import com.inout.apiserver.error.NotFoundException
import com.inout.apiserver.repository.user.UserRepository
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service

@Service
class UserService(
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder
) {
    fun createUser(request: CreateUserRequest): User {
        val existingUser = getUserByEmail(request.email)
        if (existingUser != null) {
            throw ConflictException(message = "User already exists", code = "USER_1")
        }

        return userRepository.save(
            User.newOf(
                email = request.email,
                password = passwordEncoder.encode(request.password),
                nickname = request.nickname
            )
        )
    }

    fun updateUser(request: UpdateUserRequest): User {
        val user = getUserById(request.id) ?: throw NotFoundException(message = "User not found", code = "USER_2")
        return userRepository.update(user.copy(nickname = request.nickname))
    }

    fun getUserByEmail(email: String): User? {
        return userRepository.findByEmail(email.lowercase())
    }

    fun getUserById(id: Long): User? {
        return userRepository.findById(id)
    }
}