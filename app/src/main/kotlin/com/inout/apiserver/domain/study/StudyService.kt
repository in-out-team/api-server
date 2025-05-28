package com.inout.apiserver.domain.study

import com.inout.apiserver.error.BadRequestException
import com.inout.apiserver.error.ConflictException
import com.inout.apiserver.infrastructure.mongo.study.DailyStudySet
import com.inout.apiserver.infrastructure.mongo.study.DailyStudySetRepository
import com.inout.apiserver.infrastructure.mongo.study.Study
import com.inout.apiserver.infrastructure.mongo.study.StudyRepository
import com.inout.apiserver.infrastructure.mongo.user.User
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
class StudyService(
    private val studyRepository: StudyRepository,
    private val dailyStudySetRepository: DailyStudySetRepository,
) {
    fun getAllByUserId(
        userId: ObjectId,
        wordNamePrefix: String?,
        pageable: Pageable,
    ): Page<Study> =
        when (wordNamePrefix) {
            null -> {
                val validSorts = Study.validSorts
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

                studyRepository.findAllByUserId(userId = userId, pageable = pageRequest)
            }

            else -> {
                studyRepository.findAllByUserIdAndWordNamePrefix(
                    userId = userId,
                    wordNamePrefix = wordNamePrefix,
                    pageable =
                        PageRequest.of(
                            pageable.pageNumber,
                            pageable.pageSize,
                        ),
                )
            }
        }

    fun getByUserIdAndWordDefinitionId(
        userId: ObjectId,
        wordDefinitionId: ObjectId,
    ): Study? =
        studyRepository.findByUserIdAndWordDefinitionId(
            userId = userId,
            wordDefinitionId = wordDefinitionId,
        )

    fun getById(id: ObjectId): Study? = studyRepository.findById(id).orElse(null)

    fun getStudiesPastDue(
        userId: ObjectId,
        due: Instant,
        excludeIds: List<ObjectId>,
        count: Int,
    ): List<Study> {
        if (count <= 0) {
            return emptyList()
        }

        val pageRequest = PageRequest.of(0, count, Sort.by("due").ascending())

        return studyRepository
            .findAllPastDueStudiesBy(
                userId = userId,
                due = due,
                excludeIds = excludeIds,
                pageable = pageRequest,
            ).content
    }

    fun getStudiesByIds(ids: List<ObjectId>): List<Study> = studyRepository.findAllById(ids)

    fun createStudy(
        userId: ObjectId,
        wordDefinitionId: ObjectId,
    ): Study {
        getByUserIdAndWordDefinitionId(
            userId = userId,
            wordDefinitionId = wordDefinitionId,
        )?.let {
            throw ConflictException(message = "Study already exists", code = "STUDY_1")
        }

        return studyRepository.save(
            Study.fromCreateObject(
                userId = userId,
                wordDefinitionId = wordDefinitionId,
            ),
        )
    }

    fun updateStudy(study: Study): Study = studyRepository.save(study)

    fun getDailyStudySet(
        userId: ObjectId,
        date: LocalDate,
    ): DailyStudySet? =
        dailyStudySetRepository.findByUserIdAndDate(
            userId = userId,
            date = date,
        )

    fun getDailyStudySetById(id: ObjectId): DailyStudySet? = dailyStudySetRepository.findById(id).orElse(null)

    fun addStudyToDailyStudySet(
        dailyStudySet: DailyStudySet,
        study: Study,
    ): DailyStudySet {
        if (study.id in dailyStudySet.studyIds) {
            throw ConflictException(message = "Study already exists in daily study set", code = "STUDY_7")
        }

        val updatedStudyIds = (dailyStudySet.studyIds + study.id!!).distinct()

        return dailyStudySetRepository.save(
            dailyStudySet.copy(
                studyIds = updatedStudyIds,
            ),
        )
    }

    fun getStudiesByDailyStudySet(
        dailyStudySet: DailyStudySet,
        user: User,
    ): List<Study> {
        if (user.id != dailyStudySet.userId) {
            throw BadRequestException(message = "User does not match daily study set", code = "STUDY_8")
        }

        val userZoneId = ZoneId.of(user.timezone)
        val todayDate = LocalDate.now(userZoneId)
        val dailyStudySetStudies = studyRepository.findAllById(dailyStudySet.studyIds)
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
    ): DailyStudySet {
        getDailyStudySet(
            userId = userId,
            date = date,
        )?.let {
            throw ConflictException(message = "Daily study set already exists", code = "STUDY_5")
        }

        return dailyStudySetRepository.save(
            DailyStudySet.fromCreateObject(
                userId = userId,
                date = date,
            ),
        )
    }

    fun getStudiesByUserIdAndWordDefinitionIds(
        userId: ObjectId,
        wordDefinitionIds: List<ObjectId>,
    ): List<Study> =
        studyRepository.findAllByUserIdAndWordDefinitionIdIn(
            userId = userId,
            wordDefinitionIds = wordDefinitionIds,
        )

    fun existsStudyByWordDefinitionId(wordDefinitionId: ObjectId): Boolean = studyRepository.existsByWordDefinitionId(wordDefinitionId)
}
