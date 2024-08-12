package com.inout.apiserver.domain.user

import com.inout.apiserver.interfaces.web.v1.request.CreateUserRequest
import com.inout.apiserver.interfaces.web.v1.request.UpdateUserRequest
import com.inout.apiserver.error.ConflictException
import com.inout.apiserver.error.NotFoundException
import com.inout.apiserver.infrastructure.db.user.UserEntity
import com.inout.apiserver.infrastructure.db.user.UserRepository
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service

@Service
class UserService(
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder
) {
    /**
     * TODO: change parameter to...
     * 1. accept dto from different package
     * 2. flatten the request object (if not over 3 parameters)
     */
    fun createUser(request: CreateUserRequest): User {
        val existingUser = getUserByEmail(request.email)
        if (existingUser != null) {
            throw ConflictException(message = "User already exists", code = "USER_1")
        }
        // TODO: validate user parameters like email length/pattern, password length/pattern, nickname length, etc
        val userCreateObject = UserCreateObject(
            email = request.email,
            password = passwordEncoder.encode(request.password),
            nickname = request.nickname
        )
        return userRepository.save(UserEntity.fromCreateObject(userCreateObject))
    }

    /**
     * TODO: change parameter to...
     * 1. accept dto from different package
     * 2. flatten the request object (if not over 3 parameters)
     */
    fun updateUser(request: UpdateUserRequest): User {
        val user = getUserById(request.id) ?: throw NotFoundException(message = "User not found", code = "USER_2")
        return userRepository.save(UserEntity.of(user.copy(nickname = request.nickname)))
    }

    fun getUserByEmail(email: String): User? {
        return userRepository.findByEmail(email.lowercase())
    }

    fun getUserById(id: Long): User? {
        return userRepository.findById(id)
    }
}