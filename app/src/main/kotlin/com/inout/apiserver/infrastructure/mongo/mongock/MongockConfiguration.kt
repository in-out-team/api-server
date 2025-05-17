package com.inout.apiserver.infrastructure.mongo.mongock

import com.inout.apiserver.infrastructure.mongo.mongock.migration.Migration
import com.mongodb.client.MongoClient
import io.mongock.driver.mongodb.sync.v4.driver.MongoSync4Driver
import io.mongock.runner.springboot.MongockSpringboot
import io.mongock.runner.springboot.base.MongockInitializingBeanRunner
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.ApplicationContext
import org.springframework.context.ApplicationEventPublisher
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import java.net.URI

@Configuration
class MongockConfiguration(
    @Value("\${spring.data.mongodb.uri}")
    private val mongoUri: String,
) {
    @Profile("!test")
    @Bean
    fun getBuilder(
        mongoClient: MongoClient,
        context: ApplicationContext,
        applicationEventPublisher: ApplicationEventPublisher,
    ): MongockInitializingBeanRunner {
        val databaseName = URI(mongoUri).path.removePrefix("/")
        val driver = MongoSync4Driver.withDefaultLock(mongoClient, databaseName)

        return MongockSpringboot
            .builder()
            .setDriver(driver)
            .addMigrationScanPackage(Migration::class.java.`package`.name)
            .setDefaultAuthor("SYSTEM_ACCOUNT")
            .setSpringContext(context)
            .setEventPublisher(applicationEventPublisher)
            .setTransactional(false)
            .buildInitializingBeanRunner()
    }
}
