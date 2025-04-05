package com.inout.apiserver.config.background

import com.inout.apiserver.config.AppConfig
import org.jobrunr.jobs.mappers.JobMapper
import org.jobrunr.storage.sql.postgres.PostgresStorageProvider
import org.springframework.boot.jdbc.DataSourceBuilder
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class JobrunrConfig {
    @Bean
    fun storageProvider(
        jobMapper: JobMapper,
        appConfig: AppConfig,
    ): PostgresStorageProvider {
        val dataSource =
            DataSourceBuilder
                .create()
                .url(appConfig.jobrunr.dataSourceUrl)
                .username(appConfig.jobrunr.dataSourceUsername)
                .password(appConfig.jobrunr.dataSourcePassword)
                .build()

        return PostgresStorageProvider(dataSource)
            .also { it.setJobMapper(jobMapper) }
    }
}
