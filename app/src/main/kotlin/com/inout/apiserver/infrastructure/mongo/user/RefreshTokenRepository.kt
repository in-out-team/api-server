package com.inout.apiserver.infrastructure.mongo.user

import org.bson.types.ObjectId
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.stereotype.Repository

@Repository
interface RefreshTokenRepository : MongoRepository<RefreshToken, ObjectId> {
    fun findByToken(token: String): RefreshToken?
}
