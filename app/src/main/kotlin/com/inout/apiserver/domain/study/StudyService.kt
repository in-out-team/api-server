package com.inout.apiserver.domain.study

import com.inout.apiserver.infrastructure.db.study.StudyRepository
import org.springframework.stereotype.Service

@Service
class StudyService(
    private val studyRepository: StudyRepository,
) {
    fun getAllByUserId(userId: Long): List<Study> {
        TODO()
    }

    fun getByUserIdAndWordDefinitionId(userId: Long, wordDefinitionId: Long): Study? {
        TODO()
    }

    fun getById(id: Long): Study? {
        TODO()
    }

    fun createStudy(userId: Long, wordDefinitionId: Long): Study {
        TODO()
    }
}
