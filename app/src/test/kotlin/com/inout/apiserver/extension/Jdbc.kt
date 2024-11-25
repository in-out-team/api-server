package com.inout.apiserver.extension

import org.springframework.jdbc.core.JdbcTemplate

fun JdbcTemplate.cleanUp() {
    val tables =
        queryForList(
            """
        SELECT table_name 
        FROM information_schema.tables 
        WHERE table_schema = 'public' 
        AND table_type = 'BASE TABLE'
        """,
            String::class.java,
        )
    tables.forEach { tableName ->
        execute("TRUNCATE TABLE $tableName RESTART IDENTITY CASCADE")
    }
}
