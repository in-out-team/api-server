package com.inout.apiserver.infrastructure.mongo.mongock.migration.seed

import com.inout.apiserver.domain.user.UserService
import com.inout.apiserver.infrastructure.mongo.mongock.BaseMigration
import com.inout.apiserver.infrastructure.mongo.user.UserRepository
import io.mongock.api.annotations.ChangeUnit
import org.springframework.stereotype.Component

@Component
@ChangeUnit(id = "S-SeedAdminUser", order = "19700101000001", author = "migration_seed")
class Seed0001AdminUser(
    private val userService: UserService,
    private val userRepository: UserRepository,
) : BaseMigration() {
    override fun migration() {
        logger.info("S-SeedAdminUser migration start")

        userService
            .getUserByEmail("in.out.ad2024@gmail.com")
            ?.let { logger.info("S-SeedAdminUser already exist data... skip migration") }
            ?: run {
                userService
                    .createUser(
                        email = "in.out.ad2024@gmail.com",
                        password = "abcd1234",
                        nickname = "admin",
                    ).let {
                        userRepository.save(
                            it.copy(
                                roles = setOf("USER", "ADMIN"),
                            ),
                        )
                    }
            }

        logger.info("S-SeedAdminUser migration done")
    }
}
