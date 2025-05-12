package com.inout.apiserver.infrastructure.db

import org.springframework.context.annotation.Configuration
import org.springframework.data.jpa.repository.config.EnableJpaRepositories

@Configuration
@EnableJpaRepositories(
    basePackages = ["com.inout.apiserver.infrastructure.db"],
    entityManagerFactoryRef = "entityManagerFactory",
    transactionManagerRef = "transactionManager",
)
class JpaConfig
