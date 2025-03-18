package com.inout.apiserver.helper

import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.util.TestPropertyValues
import org.springframework.context.ApplicationContextInitializer
import org.springframework.context.ConfigurableApplicationContext
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.TestConstructor
import org.springframework.test.context.TestExecutionListeners
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener
import org.springframework.test.context.support.DirtiesContextTestExecutionListener
import org.testcontainers.containers.PostgreSQLContainer

class TestDbInitializer : ApplicationContextInitializer<ConfigurableApplicationContext> {
    override fun initialize(applicationContext: ConfigurableApplicationContext) {
        val postgresSQLContainer = PostgreSQLContainer<Nothing>("postgres:14")

        postgresSQLContainer.start()

        val addedProperties =
            listOf(
                "spring.datasource.url=${postgresSQLContainer.jdbcUrl}",
                "spring.datasource.username=${postgresSQLContainer.username}",
                "spring.datasource.password=${postgresSQLContainer.password}",
                "JOBRUNR_DB_DATASOURCE=${postgresSQLContainer.jdbcUrl}",
                "JOBRUNR_DB_USERNAME=${postgresSQLContainer.username}",
                "JOBRUNR_DB_PASSWORD=${postgresSQLContainer.password}",
            )

        TestPropertyValues.of(addedProperties).applyTo(applicationContext.environment)
    }
}

@SpringBootTest
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
@ContextConfiguration(initializers = [TestDbInitializer::class])
@TestExecutionListeners(
    listeners = [
        DependencyInjectionTestExecutionListener::class,
        DirtiesContextTestExecutionListener::class,
    ],
)
annotation class InOutSpringBootTest
