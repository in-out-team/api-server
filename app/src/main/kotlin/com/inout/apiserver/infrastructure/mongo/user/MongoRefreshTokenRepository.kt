package com.inout.apiserver.infrastructure.mongo.user

import org.bson.types.ObjectId
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.stereotype.Repository

@Repository
interface MongoRefreshTokenRepository : MongoRepository<MongoRefreshToken, ObjectId> {
    fun findByToken(token: String): MongoRefreshToken?
}
