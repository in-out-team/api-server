package com.inout.apiserver.infrastructure.mongo.user

import org.bson.types.ObjectId
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.Id
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.mongodb.core.index.Indexed
import org.springframework.data.mongodb.core.mapping.Document
import java.time.Instant

@Document(collection = "refresh_tokens")
data class RefreshToken(
    @Id
    val id: ObjectId? = null,
    val userId: ObjectId,
    @Indexed(unique = true)
    val token: String,
    val expiresAt: Instant,
    @CreatedDate
    val createdAt: Instant? = null,
    @LastModifiedDate
    val updatedAt: Instant? = null,
)
