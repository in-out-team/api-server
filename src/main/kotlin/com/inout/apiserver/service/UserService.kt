package com.inout.apiserver.service

import com.inout.apiserver.controller.v1.request.CreateUserRequest
import com.inout.apiserver.domain.User
import com.inout.apiserver.error.ConflictException
import com.inout.apiserver.repository.user.UserRepository
import org.springframework.stereotype.Service

@Service
class UserService(
    private val userRepository: UserRepository
) {
    fun createUser(request: CreateUserRequest): User {
        val existingUser = getUserByEmail(request.email)
        if (existingUser != null) {
            throw ConflictException(message = "User already exists", code = "USER_1")
        }

        return userRepository.save(
            User.newOf(email = request.email, password = request.password, nickname = request.nickname)
        )
    }

    fun getUserByEmail(email: String): User? {
        return userRepository.findByEmail(email.lowercase())
    }
}