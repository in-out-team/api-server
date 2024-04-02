package com.inout.apiserver.entity

import com.inout.apiserver.config.JpaAuditingConfig
import com.inout.apiserver.domain.User
import jakarta.persistence.*
import org.hibernate.annotations.DynamicUpdate
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedDate
import java.time.LocalDateTime

@Entity
@Table(name = "users")
@EntityListeners(JpaAuditingConfig::class)
@DynamicUpdate
data class UserEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long?,
    @Column(unique = true)
    val email: String,
    val password: String,
    val nickname: String,
    @CreatedDate
    @Column(updatable = false)
    val createdAt: LocalDateTime,
    @LastModifiedDate
    val updatedAt: LocalDateTime
) {
    fun toDomain(): User {
        return User(
            id = requireNotNull(id),
            email = email,
            password = password,
            nickname = nickname,
            createdAt = createdAt,
            updatedAt = updatedAt,
        )
    }
}
