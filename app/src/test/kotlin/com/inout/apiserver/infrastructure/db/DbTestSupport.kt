package com.inout.apiserver.infrastructure.db

import com.inout.apiserver.config.jpa.JpaAuditingConfig
import org.junit.jupiter.api.BeforeEach
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.context.annotation.Import
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.TestConstructor

@ActiveProfiles("test")
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
@Import(DbCleanUp::class, JpaAuditingConfig::class)
abstract class DbTestSupport {
    @Autowired
    lateinit var dbCleanUp: DbCleanUp

    @BeforeEach
    fun setUp() {
        dbCleanUp.execute()
    }
}