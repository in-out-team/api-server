package com.inout.apiserver.domain.study

import com.inout.apiserver.base.alias.DailyStudySetId
import com.inout.apiserver.base.alias.StudyId
import com.inout.apiserver.base.alias.UserId
import com.inout.apiserver.base.alias.WordDefinitionId
import com.inout.apiserver.error.ConflictException
import com.inout.apiserver.infrastructure.db.study.DailyStudySetEntity
import com.inout.apiserver.infrastructure.db.study.DailyStudySetRepository
import com.inout.apiserver.infrastructure.db.study.Study
import com.inout.apiserver.infrastructure.db.study.StudyRepository
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset

@Service
class StudyService(
    private val studyRepository: StudyRepository,
    private val dailyStudySetRepository: DailyStudySetRepository,
) {
    companion object {
        const val DEFAULT_STUDY_SET_SIZE = 20 // TODO: later fix with user's settings
    }

    fun getAllByUserId(
        userId: UserId,
        pageable: Pageable,
    ): Page<Study> {
        //  TODO: ignore sort for now
        val sortIgnoredPageRequest =
            PageRequest.of(
                pageable.pageNumber,
                pageable.pageSize,
            )

        return studyRepository.findAllByUserId(userId, sortIgnoredPageRequest)
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

    fun getDailyStudySetById(id: DailyStudySetId): DailyStudySet? = dailyStudySetRepository.findById(id)

    fun addStudyToDailyStudySet(
        dailyStudySet: DailyStudySet,
        study: Study,
    ): DailyStudySet {
        if (dailyStudySet.studyIds.contains(study.id)) {
            throw ConflictException(message = "Study already exists in daily study set", code = "STUDY_7")
        }

        val updatedStudyIds = (dailyStudySet.studyIds + study.id!!).distinct()
        return dailyStudySetRepository.save(DailyStudySetEntity.fromDomain(dailyStudySet.copy(studyIds = updatedStudyIds)))
    }

    fun getStudiesByDailyStudySet(dailyStudySet: DailyStudySet): List<Study> {
        val todayDate = LocalDate.now()
        val dailyStudySetStudies = studyRepository.findAllById(dailyStudySet.studyIds)
        if (dailyStudySet.date.isBefore(todayDate)) {
            return dailyStudySetStudies
        }

        val endOfDay = todayDate.atStartOfDay().plusDays(1).toInstant(ZoneOffset.UTC)
        val due = endOfDay.atZone(ZoneOffset.UTC).toInstant()
        val studiesPastDue =
            getStudiesPastDue(
                userId = dailyStudySet.userId,
                due = due,
                excludeIds = dailyStudySet.studyIds,
                count = DEFAULT_STUDY_SET_SIZE - dailyStudySetStudies.size,
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

        return dailyStudySetRepository.save(DailyStudySetEntity.fromCreateObject(dailyStudySetCreateObject))
    }
}
