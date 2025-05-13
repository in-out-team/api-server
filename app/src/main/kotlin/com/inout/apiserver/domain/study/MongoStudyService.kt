package com.inout.apiserver.domain.study

import com.inout.apiserver.error.BadRequestException
import com.inout.apiserver.error.ConflictException
import com.inout.apiserver.infrastructure.mongo.study.MongoDailyStudySet
import com.inout.apiserver.infrastructure.mongo.study.MongoDailyStudySetRepository
import com.inout.apiserver.infrastructure.mongo.study.MongoStudy
import com.inout.apiserver.infrastructure.mongo.study.MongoStudyRepository
import com.inout.apiserver.infrastructure.mongo.user.MongoUser
import org.bson.types.ObjectId
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId

@Service
class MongoStudyService(
    private val mongoStudyRepository: MongoStudyRepository,
    private val mongoDailyStudySetRepository: MongoDailyStudySetRepository,
) {
    fun getAllByUserId(
        userId: ObjectId,
        wordNamePrefix: String?,
        pageable: Pageable,
    ): Page<MongoStudy> =
        when (wordNamePrefix) {
            null -> {
                val validSorts = MongoStudy.validSorts
                val sorts = pageable.sort.filter { it.property in validSorts }.toList()
                val pageRequest =
                    if (sorts.isNotEmpty()) {
                        PageRequest.of(
                            pageable.pageNumber,
                            pageable.pageSize,
                            Sort.by(sorts),
                        )
                    } else {
                        PageRequest.of(
                            pageable.pageNumber,
                            pageable.pageSize,
                            Sort.by("due").ascending(),
                        )
                    }

                mongoStudyRepository.findAllByUserId(userId = userId, pageable = pageRequest)
            }

            else -> {
                mongoStudyRepository.findAllByUserIdAndWordNamePrefix(
                    userId = userId,
                    wordNamePrefix = wordNamePrefix,
                    pageable =
                        PageRequest.of(
                            pageable.pageNumber,
                            pageable.pageSize,
                            Sort.by("due").ascending(),
                        ),
                )
            }
        }

    fun getByUserIdAndWordDefinitionId(
        userId: ObjectId,
        wordDefinitionId: ObjectId,
    ): MongoStudy? =
        mongoStudyRepository.findByUserIdAndWordDefinitionId(
            userId = userId,
            wordDefinitionId = wordDefinitionId,
        )

    fun getById(id: ObjectId): MongoStudy? = mongoStudyRepository.findById(id).orElse(null)

    fun getStudiesPastDue(
        userId: ObjectId,
        due: Instant,
        excludeIds: List<ObjectId>,
        count: Int,
    ): List<MongoStudy> {
        if (count <= 0) {
            return emptyList()
        }

        val pageRequest = PageRequest.of(0, count, Sort.by("due").ascending())

        return mongoStudyRepository
            .findAllPastDueStudiesBy(
                userId = userId,
                due = due,
                excludeIds = excludeIds,
                pageable = pageRequest,
            ).content
    }

    fun getStudiesByIds(ids: List<ObjectId>): List<MongoStudy> = mongoStudyRepository.findAllById(ids)

    fun createStudy(
        userId: ObjectId,
        wordDefinitionId: ObjectId,
    ): MongoStudy {
        getByUserIdAndWordDefinitionId(
            userId = userId,
            wordDefinitionId = wordDefinitionId,
        )?.let {
            throw ConflictException(message = "Study already exists", code = "STUDY_1")
        }

        return mongoStudyRepository.save(
            MongoStudy.fromCreateObject(
                userId = userId,
                wordDefinitionId = wordDefinitionId,
            ),
        )
    }

    // FIXME: add rate & save logic here
    fun updateStudy(study: MongoStudy): MongoStudy = mongoStudyRepository.save(study)

    fun getDailyStudySet(
        userId: ObjectId,
        date: LocalDate,
    ): MongoDailyStudySet? =
        mongoDailyStudySetRepository.findByUserIdAndDate(
            userId = userId,
            date = date,
        )

    fun getDailyStudySetById(id: ObjectId): MongoDailyStudySet? = mongoDailyStudySetRepository.findById(id).orElse(null)

    fun addStudyToDailyStudySet(
        dailyStudySet: MongoDailyStudySet,
        study: MongoStudy,
    ): MongoDailyStudySet {
        if (study.id in dailyStudySet.studyIds) {
            throw ConflictException(message = "Study already exists in daily study set", code = "STUDY_7")
        }

        val updatedStudyIds = (dailyStudySet.studyIds + study.id!!).distinct()

        return mongoDailyStudySetRepository.save(
            dailyStudySet.copy(
                studyIds = updatedStudyIds,
            ),
        )
    }

    fun getStudiesByDailyStudySet(
        dailyStudySet: MongoDailyStudySet,
        user: MongoUser,
    ): List<MongoStudy> {
        if (user.id != dailyStudySet.userId) {
            throw BadRequestException(message = "User does not match daily study set", code = "STUDY_8")
        }

        val userZoneId = ZoneId.of(user.timezone)
        val todayDate = LocalDate.now(userZoneId)
        val dailyStudySetStudies = mongoStudyRepository.findAllById(dailyStudySet.studyIds)
        if (dailyStudySet.date.isBefore(todayDate)) {
            return dailyStudySetStudies
        }

        val endOfDay = todayDate.atTime(LocalTime.MAX).atZone(userZoneId).toInstant()
        val studiesPastDue =
            getStudiesPastDue(
                userId = dailyStudySet.userId,
                due = endOfDay,
                excludeIds = dailyStudySet.studyIds,
                count = maxOf(user.studyPerDay - dailyStudySetStudies.size, 0),
            )
        return dailyStudySetStudies + studiesPastDue
    }

    fun createDailyStudySet(
        userId: ObjectId,
        date: LocalDate,
    ): MongoDailyStudySet {
        getDailyStudySet(
            userId = userId,
            date = date,
        )?.let {
            throw ConflictException(message = "Daily study set already exists", code = "STUDY_5")
        }

        return mongoDailyStudySetRepository.save(
            MongoDailyStudySet.fromCreateObject(
                userId = userId,
                date = date,
            ),
        )
    }

    fun getStudiesByUserIdAndWordDefinitionIds(
        userId: ObjectId,
        wordDefinitionIds: List<ObjectId>,
    ): List<MongoStudy> =
        mongoStudyRepository.findAllByUserIdAndWordDefinitionIdIn(
            userId = userId,
            wordDefinitionIds = wordDefinitionIds,
        )

    fun existsStudyByWordDefinitionId(wordDefinitionId: ObjectId): Boolean = mongoStudyRepository.existsByWordDefinitionId(wordDefinitionId)
}
