package com.inout.apiserver.domain.study

import com.inout.apiserver.error.ConflictException
import com.inout.apiserver.infrastructure.db.study.DailyStudySetEntity
import com.inout.apiserver.infrastructure.db.study.DailyStudySetRepository
import com.inout.apiserver.infrastructure.db.study.StudyEntity
import com.inout.apiserver.infrastructure.db.study.StudyRepository
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import java.time.Instant
import java.time.LocalDate

@Service
class StudyService(
    private val studyRepository: StudyRepository,
    private val dailyStudySetRepository: DailyStudySetRepository,
) {
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
        count: Int,
    ): List<Study> {
        if (count <= 0) {
            return emptyList()
        }

        return studyRepository.findAllPastDueStudiesBy(userId, due, PageRequest.of(0, count)).content
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

    fun getDailyStudySet(
        userId: Long,
        date: LocalDate,
    ): DailyStudySet? {
        return dailyStudySetRepository.findByUserIdAndDate(userId, date)
    }

    fun createDailyStudySet(dailyStudySetCreateObject: DailyStudySetCreateObject): DailyStudySet {
        return dailyStudySetRepository.save(DailyStudySetEntity.fromCreateObject(dailyStudySetCreateObject))
    }
}
