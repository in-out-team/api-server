package com.inout.apiserver.domain.study

import com.inout.apiserver.error.ConflictException
import com.inout.apiserver.infrastructure.db.study.StudyEntity
import com.inout.apiserver.infrastructure.db.study.StudyRepository
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service

@Service
class StudyService(
    private val studyRepository: StudyRepository,
) {
    fun getAllByUserId(
        userId: Long,
        pageable: Pageable,
    ): Page<Study> {
        //  TODO: ignore sort for now
        val sortIgnoredPageRequest = PageRequest.of(
            pageable.pageNumber,
            pageable.pageSize
        )

        return studyRepository.findAllByUserId(userId, sortIgnoredPageRequest)
    }

    fun getByUserIdAndWordDefinitionId(userId: Long, wordDefinitionId: Long): Study? {
        return studyRepository.findByUserIdAndWordDefinitionId(userId, wordDefinitionId)
    }

    fun getById(id: Long): Study? {
        return studyRepository.findById(id)
    }

    fun createStudy(studyCreateObject: StudyCreateObject): Study {
        getByUserIdAndWordDefinitionId(
            userId = studyCreateObject.userId,
            wordDefinitionId = studyCreateObject.wordDefinitionId
        )?.let {
            throw ConflictException(message = "Study already exists", code = "STUDY_1")
        }

        return studyRepository.save(StudyEntity.fromCreateObject(studyCreateObject))
    }
}
