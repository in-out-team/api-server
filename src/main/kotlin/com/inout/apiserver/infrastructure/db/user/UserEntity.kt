package com.inout.apiserver.infrastructure.db.user

import com.inout.apiserver.config.jpa.JpaAuditingConfig
import com.inout.apiserver.domain.user.User
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
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long?,
    @Column(unique = true)
    val email: String,
    val password: String,
    val nickname: String,
    @CreationTimestamp
    @Column(updatable = false)
    val createdAt: LocalDateTime? = null,
    @UpdateTimestamp
    val updatedAt: LocalDateTime? = null,
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
