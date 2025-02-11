package com.inout.apiserver.infrastructure.db.user

import com.inout.apiserver.base.alias.UserId
import com.inout.apiserver.domain.user.UserCreateObject
import com.inout.apiserver.infrastructure.db.TimestampedEntity
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table

@Entity
@Table(name = "users")
data class User(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: UserId? = null,
    @Column(unique = true)
    val email: String,
    val password: String,
    val nickname: String,
) : TimestampedEntity() {
    companion object {
        fun fromCreateObject(createObject: UserCreateObject): User =
            User(
                email = createObject.email.lowercase(),
                password = createObject.password,
                nickname = createObject.nickname,
            )
    }
}
