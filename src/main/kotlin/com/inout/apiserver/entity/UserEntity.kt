package com.inout.apiserver.entity

import com.inout.apiserver.domain.User
import jakarta.persistence.*

@Entity
@Table(name = "users")
data class UserEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long?,
    @Column(unique = true)
    val email: String,
    val password: String,
    val nickname: String,
) {
    fun toDomain(): User {
        return User(
            id = requireNotNull(id),
            email = email,
            password = password,
            nickname = nickname,
        )
    }
}
