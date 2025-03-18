package com.inout.apiserver.infrastructure.db.user

import com.inout.apiserver.base.alias.UserId
import com.inout.apiserver.infrastructure.db.InstantColumn
import com.inout.apiserver.infrastructure.db.TimestampedEntity
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Index
import jakarta.persistence.Table
import java.time.Instant

@Entity
@Table(
    name = "refresh_tokens",
    indexes = [
        Index(
            name = "idx_refresh_tokens_user_id",
            columnList = "user_id",
        ),
    ],
)
data class RefreshToken(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,
    val userId: UserId,
    @Column(unique = true)
    val token: String,
    @InstantColumn
    val expiresAt: Instant,
) : TimestampedEntity()
