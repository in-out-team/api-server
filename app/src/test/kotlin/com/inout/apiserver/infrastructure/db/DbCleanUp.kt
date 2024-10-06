package com.inout.apiserver.infrastructure.db

import jakarta.annotation.PostConstruct
import jakarta.persistence.Entity
import jakarta.persistence.EntityManager
import jakarta.persistence.Table
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
class DbCleanUp(
    private val em: EntityManager,
) {
    private val tableNames = mutableListOf<String>()

    @PostConstruct
    fun init() {
        em.metamodel.entities
            .filter { it.javaType.getAnnotation(Entity::class.java) != null }
            .forEach {
                val tableName = it.javaType.getAnnotation(Table::class.java).name
                tableNames.add(tableName)
            }
    }

    @Transactional
    fun execute() {
        em.flush()
        tableNames.forEach { tableName ->
            em.createNativeQuery("TRUNCATE TABLE $tableName RESTART IDENTITY CASCADE").executeUpdate()
        }
    }
}
