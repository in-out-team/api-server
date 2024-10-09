package com.inout.apiserver.helper

import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.test.context.TestContext
import org.springframework.test.context.support.AbstractTestExecutionListener

class DatabaseCleanupListener : AbstractTestExecutionListener() {
    private lateinit var jdbcTemplate: JdbcTemplate

    override fun afterTestMethod(testContext: TestContext) {
        val applicationContext = testContext.applicationContext
        jdbcTemplate = applicationContext.getBean(JdbcTemplate::class.java)
        cleanupDatabase()
    }

    private fun cleanupDatabase() {
        val tables =
            jdbcTemplate.queryForList(
                """
        SELECT table_name 
        FROM information_schema.tables 
        WHERE table_schema = 'public' 
        AND table_type = 'BASE TABLE'
        """,
                String::class.java,
            )
        tables.forEach { tableName ->
            jdbcTemplate.execute("TRUNCATE TABLE $tableName RESTART IDENTITY CASCADE")
        }
    }
}
