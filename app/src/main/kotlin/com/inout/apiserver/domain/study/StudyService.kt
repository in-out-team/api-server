package com.inout.apiserver.domain.study

import com.inout.apiserver.base.enums.FsrsCardRating
import com.inout.apiserver.base.enums.FsrsCardState
import com.inout.apiserver.error.ConflictException
import com.inout.apiserver.error.InternalServerErrorException
import com.inout.apiserver.infrastructure.db.study.DailyStudySetEntity
import com.inout.apiserver.infrastructure.db.study.DailyStudySetRepository
import com.inout.apiserver.infrastructure.db.study.StudyEntity
import com.inout.apiserver.infrastructure.db.study.StudyRepository
import com.inout.fsrs.FSRS
import com.inout.fsrs.model.FSRSParameters
import com.inout.fsrs.model.enums.Grade
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
        userId: Long,
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
        userId: Long,
        wordDefinitionId: Long,
    ): Study? {
        return studyRepository.findByUserIdAndWordDefinitionId(userId, wordDefinitionId)
    }

    fun getById(id: Long): Study? {
        return studyRepository.findById(id)
    }

    fun getStudiesPastDue(
        userId: Long,
        due: Instant,
        excludeIds: List<Long>,
        count: Int,
    ): List<Study> {
        if (count <= 0) {
            return emptyList()
        }

        return studyRepository.findAllPastDueStudiesBy(
            userId = userId,
            due = due,
            excludeIds = excludeIds,
            pageable = PageRequest.of(0, count),
        ).content
    }

    fun getStudiesByIds(ids: List<Long>): List<Study> {
        return studyRepository.findAllByIds(ids)
    }

    fun createStudy(studyCreateObject: StudyCreateObject): Study {
        getByUserIdAndWordDefinitionId(
            userId = studyCreateObject.userId,
            wordDefinitionId = studyCreateObject.wordDefinitionId,
        )?.let {
            throw ConflictException(message = "Study already exists", code = "STUDY_1")
        }

        return studyRepository.save(StudyEntity.fromCreateObject(studyCreateObject))
    }

    fun rateStudy(
        study: Study,
        rating: FsrsCardRating,
    ): Study {
        val fsrsParam = FSRSParameters()
        val fsrs = FSRS(fsrsParam)
        val fsrsCard = study.toFsrsCard()
        val recordLog = fsrs.repeat(fsrsCard, study.due)
        val newRecordLogItem =
            recordLog.logs[Grade.fromRating(FsrsCardRating.toFsrsRating(rating))]
                ?: throw InternalServerErrorException(message = "FSRS Repeat Error", code = "STUDY_6")
        val updatedFsrsCard = newRecordLogItem.card
        val newFsrsReviewLog = newRecordLogItem.log

        val ratedStudy =
            study.copy(
                state = FsrsCardState.of(updatedFsrsCard.state),
                due = updatedFsrsCard.due,
                stability = updatedFsrsCard.stability,
                difficulty = updatedFsrsCard.difficulty,
                elapsedDays = updatedFsrsCard.elapsedDays,
                scheduledDays = updatedFsrsCard.scheduledDays,
                reps = updatedFsrsCard.reps,
                lapses = updatedFsrsCard.lapses,
                lastReview = updatedFsrsCard.lastReview,
                reviewLogs = study.reviewLogs + StudyReviewLog.newFrom(newFsrsReviewLog),
            )
        return studyRepository.save(StudyEntity.fromDomain(ratedStudy))
    }

    fun getDailyStudySet(
        userId: Long,
        date: LocalDate,
    ): DailyStudySet? {
        return dailyStudySetRepository.findByUserIdAndDate(userId, date)
    }

    fun getDailyStudySetById(id: Long): DailyStudySet? {
        return dailyStudySetRepository.findById(id)
    }

    fun addStudyToDailyStudySet(
        dailyStudySet: DailyStudySet,
        study: Study,
    ): DailyStudySet {
        if (dailyStudySet.studyIds.contains(study.id)) {
            throw ConflictException(message = "Study already exists in daily study set", code = "STUDY_7")
        }

        val updatedStudyIds = (dailyStudySet.studyIds + study.id).toSet().toList()
        return dailyStudySetRepository.save(DailyStudySetEntity.fromDomain(dailyStudySet.copy(studyIds = updatedStudyIds)))
    }

    fun getStudiesByDailyStudySet(dailyStudySet: DailyStudySet): List<Study> {
        val todayDate = LocalDate.now()
        val dailyStudySetStudies = studyRepository.findAllByIds(dailyStudySet.studyIds)
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
