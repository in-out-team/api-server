package com.inout.apiserver.infrastructure.db.user

import com.inout.apiserver.config.jpa.JpaAuditingConfig
import com.inout.apiserver.domain.user.User
import com.inout.apiserver.domain.user.UserCreateObject
import com.inout.apiserver.error.InOutRequireNotNullException
import jakarta.persistence.*
import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.DynamicUpdate
import org.hibernate.annotations.UpdateTimestamp
import java.time.LocalDateTime

@Entity
@Table(name = "users")
@EntityListeners(JpaAuditingConfig::class)
@DynamicUpdate
data class UserEntity(
    @Column(unique = true)
    val email: String,
    val password: String,
    val nickname: String,
) : BaseEntity() {
    fun toDomain(): User {
        return User(
            id = id ?: throw InOutRequireNotNullException("User id is null", "IORNN_USER_1"),
            email = email,
            password = password,
            nickname = nickname,
            createdAt = createdAt,
            updatedAt = updatedAt,
        )
    }

    companion object {
        fun of(user: User): UserEntity {
            return UserEntity(
                email = user.email,
                password = user.password,
                nickname = user.nickname,
            ).apply {
                id = user.id
                createdAt = user.createdAt
                updatedAt = user.updatedAt
            }
        }

        fun fromCreateObject(userCreateObject: UserCreateObject): UserEntity {
            return UserEntity(
                email = userCreateObject.email.lowercase(),
                password = userCreateObject.password,
                nickname = userCreateObject.nickname,
            )
        }
    }
}
