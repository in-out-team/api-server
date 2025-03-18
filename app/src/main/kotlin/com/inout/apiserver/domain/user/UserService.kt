package com.inout.apiserver.domain.user

import com.inout.apiserver.base.alias.UserId
import com.inout.apiserver.base.enums.LanguageType
import com.inout.apiserver.error.ConflictException
import com.inout.apiserver.infrastructure.db.user.User
import com.inout.apiserver.infrastructure.db.user.UserRepository
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service

@Service
class UserService(
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder,
) {
    fun createUser(
        email: String,
        password: String,
        nickname: String,
    ): User {
        val existingUser = getUserByEmail(email)
        if (existingUser != null) {
            throw ConflictException(message = "User already exists", code = "USER_1")
        }

        val userCreateObject =
            UserCreateObject(
                email = email,
                password = passwordEncoder.encode(password),
                nickname = nickname,
            )
        return userRepository.save(User.fromCreateObject(userCreateObject))
    }

    fun updateUser(
        user: User,
        nickname: String,
        studyLanguage: LanguageType,
        nativeLanguage: LanguageType,
        studyPerDay: Int,
        timezone: String,
    ): User =
        userRepository.save(
            user.copy(
                nickname = nickname,
                studyLanguage = studyLanguage,
                nativeLanguage = nativeLanguage,
                studyPerDay = studyPerDay,
                timezone = timezone,
            ),
        )

    fun getUserByEmail(email: String): User? = userRepository.findByEmail(email.lowercase())

    fun getUserById(id: UserId): User? = userRepository.findById(id).orElse(null)
}
