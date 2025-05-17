package com.inout.apiserver.config.background

import com.inout.apiserver.config.AppConfig
import com.mongodb.ConnectionString
import com.mongodb.MongoClientSettings
import com.mongodb.client.MongoClients
import org.bson.UuidRepresentation
import org.jobrunr.jobs.mappers.JobMapper
import org.jobrunr.spring.autoconfigure.JobRunrProperties
import org.jobrunr.storage.nosql.mongo.MongoDBStorageProvider
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class JobrunrConfig {
    @Bean
    fun mongoStorageProvider(
        jobMapper: JobMapper,
        jobRunrProperties: JobRunrProperties,
        appConfig: AppConfig,
    ): MongoDBStorageProvider {
        val connectionString = ConnectionString(appConfig.jobrunr.uri)

        val settings =
            MongoClientSettings
                .builder()
                .applyConnectionString(connectionString)
                .uuidRepresentation(UuidRepresentation.STANDARD)
                .build()

        return MongoDBStorageProvider(
            MongoClients.create(settings),
            jobRunrProperties.database.databaseName,
        ).also { it.setJobMapper(jobMapper) }
    }
}
