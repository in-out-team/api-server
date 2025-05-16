package com.inout.apiserver.infrastructure.mongo.study

import org.bson.types.ObjectId
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.stereotype.Repository
import java.time.LocalDate

@Repository
interface DailyStudySetRepository : MongoRepository<DailyStudySet, ObjectId> {
    fun findByUserIdAndDate(
        userId: ObjectId,
        date: LocalDate,
    ): DailyStudySet?
}
