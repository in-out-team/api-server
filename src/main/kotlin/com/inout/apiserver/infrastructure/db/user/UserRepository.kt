package com.inout.apiserver.infrastructure.db.user

import com.inout.apiserver.domain.user.User
import org.springframework.data.jpa.domain.AbstractPersistable_.id
import org.springframework.stereotype.Repository

@Repository
class UserRepository(
    private val userJpaRepository: UserJpaRepository
) {
    fun findByEmail(email: String): User? {
        return userJpaRepository.findByEmail(email)?.toDomain()
    }

    fun save(user: UserEntity): User {
        return userJpaRepository.save(user).toDomain()
    }

    fun findById(id: Long): User? {
        return userJpaRepository.findById(id).orElse(null)?.toDomain()
    }
}