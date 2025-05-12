package com.inout.apiserver.infrastructure.mongo.user

import com.inout.apiserver.base.enums.LanguageType
import com.inout.apiserver.domain.user.UserCreateObject
import org.bson.types.ObjectId
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.Id
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.mongodb.core.index.Indexed
import org.springframework.data.mongodb.core.mapping.Document
import java.time.Instant

@Document(collection = "users")
data class MongoUser(
    @Id
    val id: ObjectId? = null,
    @Indexed(unique = true)
    val email: String,
    val password: String,
    val nickname: String,
    val studyLanguage: LanguageType = LanguageType.ENGLISH,
    val nativeLanguage: LanguageType = LanguageType.KOREAN,
    val studyPerDay: Int = 5,
    val timezone: String = "Asia/Seoul",
    val roles: Set<String> = setOf("USER"),
    @CreatedDate
    val createdAt: Instant? = null,
    @LastModifiedDate
    val updatedAt: Instant? = null,
) {
    companion object {
        fun fromCreateObject(createObject: UserCreateObject): MongoUser =
            MongoUser(
                email = createObject.email.lowercase(),
                password = createObject.password,
                nickname = createObject.nickname,
            )
    }
}
