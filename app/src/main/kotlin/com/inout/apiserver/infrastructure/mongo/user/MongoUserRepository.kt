package com.inout.apiserver.infrastructure.mongo.user

import org.bson.types.ObjectId
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.stereotype.Repository

@Repository
interface MongoUserRepository : MongoRepository<MongoUser, ObjectId> {
    fun findByEmail(email: String): MongoUser?
}
