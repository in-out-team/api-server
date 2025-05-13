package com.inout.apiserver.extension

import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.query.Query

fun MongoTemplate.cleanUp() {
    db.listCollectionNames().forEach {
        remove(Query(), it)
    }
}
