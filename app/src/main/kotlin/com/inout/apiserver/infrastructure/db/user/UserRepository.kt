package com.inout.apiserver.infrastructure.db.user

import com.inout.apiserver.base.alias.UserId
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface UserRepository : JpaRepository<User, UserId> {
    fun findByEmail(email: String): User?
}
