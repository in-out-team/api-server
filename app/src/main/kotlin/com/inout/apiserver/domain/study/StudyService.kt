package com.inout.apiserver.domain.study

import com.inout.apiserver.base.alias.DailyStudySetId
import com.inout.apiserver.base.alias.StudyId
import com.inout.apiserver.base.alias.UserId
import com.inout.apiserver.base.alias.WordDefinitionId
import com.inout.apiserver.error.BadRequestException
import com.inout.apiserver.error.ConflictException
import com.inout.apiserver.infrastructure.db.study.DailyStudySet
import com.inout.apiserver.infrastructure.db.study.DailyStudySetRepository
import com.inout.apiserver.infrastructure.db.study.Study
import com.inout.apiserver.infrastructure.db.study.StudyRepository
import com.inout.apiserver.infrastructure.db.user.User
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
        userId: UserId,
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
                            Sort.by("due").descending(),
                        )
                    }

                studyRepository.findAllByUserId(userId, pageRequest)
            }

            else -> {
                PageRequest.of(
                    pageable.pageNumber,
                    pageable.pageSize,
                )

                studyRepository.findAllByUserIdAndWordNamePrefix(
                    userId = userId,
                    wordNamePrefix = wordNamePrefix,
                    pageable = pageable,
                )
            }
        }

    fun getByUserIdAndWordDefinitionId(
        userId: UserId,
        wordDefinitionId: WordDefinitionId,
    ): Study? = studyRepository.findByUserIdAndWordDefinitionId(userId, wordDefinitionId)

    fun getById(id: StudyId): Study? = studyRepository.findById(id).orElse(null)

    fun getStudiesPastDue(
        userId: UserId,
        due: Instant,
        excludeIds: List<StudyId>,
        count: Int,
    ): List<Study> {
        if (count <= 0) {
            return emptyList()
        }

        return studyRepository
            .findAllPastDueStudiesBy(
                userId = userId,
                due = due,
                excludeIds = excludeIds,
                pageable = PageRequest.of(0, count),
            ).content
    }

    fun getStudiesByIds(ids: List<StudyId>): List<Study> = studyRepository.findAllById(ids)

    fun createStudy(studyCreateObject: StudyCreateObject): Study {
        getByUserIdAndWordDefinitionId(
            userId = studyCreateObject.userId,
            wordDefinitionId = studyCreateObject.wordDefinitionId,
        )?.let {
            throw ConflictException(message = "Study already exists", code = "STUDY_1")
        }

        return studyRepository.save(Study.fromCreateObject(studyCreateObject))
    }

    fun createStudy(
        userId: UserId,
        wordDefinitionId: WordDefinitionId,
    ): Study {
        getByUserIdAndWordDefinitionId(
            userId = userId,
            wordDefinitionId = wordDefinitionId,
        )?.let {
            throw ConflictException(message = "Study already exists", code = "STUDY_1")
        }

        return studyRepository.save(
            Study.fromCreateObject(
                StudyCreateObject(
                    userId = userId,
                    wordDefinitionId = wordDefinitionId,
                ),
            ),
        )
    }

    fun updateStudy(study: Study): Study = studyRepository.save(study)

    fun getDailyStudySet(
        userId: UserId,
        date: LocalDate,
    ): DailyStudySet? = dailyStudySetRepository.findByUserIdAndDate(userId, date)

    fun getDailyStudySetById(id: DailyStudySetId): DailyStudySet? = dailyStudySetRepository.findById(id).orElse(null)

    fun addStudyToDailyStudySet(
        dailyStudySet: DailyStudySet,
        study: Study,
    ): DailyStudySet {
        if (dailyStudySet.studyIds.contains(study.id)) {
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

    fun createDailyStudySet(dailyStudySetCreateObject: DailyStudySetCreateObject): DailyStudySet {
        getDailyStudySet(
            userId = dailyStudySetCreateObject.userId,
            date = dailyStudySetCreateObject.date,
        )?.let {
            throw ConflictException(message = "Daily study set already exists", code = "STUDY_5")
        }

        return dailyStudySetRepository.save(DailyStudySet.fromCreateObject(dailyStudySetCreateObject))
    }

    fun getStudiesByUserIdAndWordDefinitionIds(
        userId: UserId,
        wordDefinitionIds: List<WordDefinitionId>,
    ): List<Study> = studyRepository.findAllByUserIdAndWordDefinitionIdIn(userId, wordDefinitionIds)
}
