package com.inout.apiserver.infrastructure.db.user

import com.inout.apiserver.domain.User
import org.springframework.stereotype.Repository

@Repository
class UserRepository(
    private val userJpaRepository: UserJpaRepository
) {
    fun findByEmail(email: String): User? {
        return userJpaRepository.findByEmail(email)?.toDomain()
    }

    fun save(user: User): User {
        return userJpaRepository.save(
            UserEntity(
                id = null,
                email = user.email,
                password = user.password,
                nickname = user.nickname,
                createdAt = user.createdAt,
                updatedAt = user.updatedAt
            )
        ).toDomain()
    }

    fun update(user: User): User {
        return userJpaRepository.save(user.toEntity()).toDomain()
    }

    fun findById(id: Long): User? {
        return userJpaRepository.findById(id).orElse(null)?.toDomain()
    }
}