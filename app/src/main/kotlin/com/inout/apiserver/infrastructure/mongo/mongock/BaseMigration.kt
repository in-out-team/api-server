package com.inout.apiserver.infrastructure.mongo.mongock

import io.mongock.api.annotations.Execution
import io.mongock.api.annotations.RollbackExecution
import org.slf4j.LoggerFactory

abstract class BaseMigration {
    protected val logger = LoggerFactory.getLogger(this::class.java)

    @RollbackExecution
    fun rollbackExecution() {
        logger.info("Rollback not implemented for ${this::class.simpleName}")
    }

    @Execution
    fun execution() {
        logger.info("Executing migration for ${this::class.simpleName}")
        migration()
    }

    abstract fun migration()
}
