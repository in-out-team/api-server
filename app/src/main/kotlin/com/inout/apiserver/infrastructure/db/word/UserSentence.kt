package com.inout.apiserver.infrastructure.db.word

import com.inout.apiserver.base.alias.SentenceId
import com.inout.apiserver.base.alias.UserId
import com.inout.apiserver.base.alias.UserSentenceId
import com.inout.apiserver.base.alias.WordDefinitionId
import com.inout.apiserver.base.enums.SentenceType
import com.inout.apiserver.domain.word.UserSentenceCreateObject
import com.inout.apiserver.infrastructure.db.TimestampedEntity
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
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
data class UserSentence(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: UserSentenceId? = null,
    val userId: UserId,
    val wordDefinitionId: WordDefinitionId,
    @Enumerated(EnumType.STRING)
    val type: SentenceType,
    val sentenceId: SentenceId,
) : TimestampedEntity() {
    companion object {
        fun fromCreateObject(userSentenceCreateObject: UserSentenceCreateObject): UserSentence =
            UserSentence(
                userId = userSentenceCreateObject.userId,
                wordDefinitionId = userSentenceCreateObject.wordDefinitionId,
                type = userSentenceCreateObject.type,
                sentenceId = userSentenceCreateObject.sentenceId,
            )
    }
}
