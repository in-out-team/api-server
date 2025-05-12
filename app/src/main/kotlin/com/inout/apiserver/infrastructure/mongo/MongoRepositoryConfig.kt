package com.inout.apiserver.infrastructure.mongo

import org.springframework.context.annotation.Configuration
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories

@Configuration
@EnableMongoRepositories(
    basePackages = ["com.inout.apiserver.infrastructure.mongo"],
    mongoTemplateRef = "mongoTemplate",
)
class MongoRepositoryConfig
