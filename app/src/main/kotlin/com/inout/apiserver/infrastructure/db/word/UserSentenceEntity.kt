package com.inout.apiserver.infrastructure.db.word

import com.inout.apiserver.base.enums.SentenceType
import com.inout.apiserver.domain.word.UserSentence
import com.inout.apiserver.domain.word.UserSentenceCreateObject
import com.inout.apiserver.error.InOutRequireNotNullException
import com.inout.apiserver.infrastructure.db.BaseEntity
import jakarta.persistence.Entity
import jakarta.persistence.Index
import jakarta.persistence.Table

@Entity
@Table(
    name = "user_sentences",
    indexes = [
        Index(
            columnList = "user_id, word_definition_id",
            name = "idx_user_sentences_user_id_word_definition_id",
        ),
        Index(
            columnList = "user_id, sentence_id",
            name = "idx_user_sentences_user_id_sentence_id",
        ),
    ],
)
data class UserSentenceEntity(
    val userId: Long,
    val wordDefinitionId: Long,
    val type: SentenceType,
    val sentenceId: Long,
) : BaseEntity() {
    companion object {
        fun fromCreateObject(userSentenceCreateObject: UserSentenceCreateObject): UserSentenceEntity =
            UserSentenceEntity(
                userId = userSentenceCreateObject.userId,
                wordDefinitionId = userSentenceCreateObject.wordDefinitionId,
                type = userSentenceCreateObject.type,
                sentenceId = userSentenceCreateObject.sentenceId,
            )

        fun of(userSentence: UserSentence): UserSentenceEntity =
            UserSentenceEntity(
                userId = userSentence.userId,
                wordDefinitionId = userSentence.wordDefinitionId,
                type = SentenceType.WRITING,
                sentenceId = userSentence.sentenceId,
            ).apply {
                id = userSentence.id
                createdAt = userSentence.createdAt
                updatedAt = userSentence.updatedAt
            }
    }

    fun toDomain(): UserSentence =
        UserSentence(
            id = id ?: throw InOutRequireNotNullException("UserSentence id is null", "IORNN_USER_SENTENCE_1"),
            userId = userId,
            wordDefinitionId = wordDefinitionId,
            type = type,
            sentenceId = sentenceId,
            createdAt = createdAt,
            updatedAt = updatedAt,
        )
}
