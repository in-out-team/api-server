package com.inout.apiserver.helper

import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.TestConstructor
import org.springframework.test.context.TestExecutionListeners
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener
import org.springframework.test.context.support.DirtiesContextTestExecutionListener

@SpringBootTest
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
@TestExecutionListeners(
    listeners = [
        DependencyInjectionTestExecutionListener::class,
        DirtiesContextTestExecutionListener::class,
        DatabaseCleanupListener::class,
    ],
)
abstract class BaseIntegrationTest
