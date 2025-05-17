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
import org.testcontainers.containers.MongoDBContainer

class TestDbInitializer : ApplicationContextInitializer<ConfigurableApplicationContext> {
    override fun initialize(applicationContext: ConfigurableApplicationContext) {
        val mongoDBContainer = MongoDBContainer("mongo:8.0.9")

        mongoDBContainer.start()

        val addedProperties =
            listOf(
                // JobRunr
                "app.jobrunr.uri=${mongoDBContainer.replicaSetUrl}",
                // MongoDB
                "spring.data.mongodb.uri=${mongoDBContainer.replicaSetUrl}",
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
