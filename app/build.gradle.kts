plugins {
    id("org.springframework.boot") version "3.2.4"
    id("io.spring.dependency-management") version "1.1.4"
    kotlin("jvm") version "1.9.23"
    kotlin("plugin.serialization") version "1.9.23"
    kotlin("plugin.spring") version "1.9.23"
    kotlin("plugin.jpa") version "1.9.23"
}

tasks.getByName("bootJar") {
    enabled = true
}

tasks.getByName("jar") {
    enabled = false
}

tasks.register<Wrapper>("wrapper") {
    gradleVersion = "8.8"
}

tasks.test {
    useJUnitPlatform()
    systemProperties["user.timezone"] = "UTC"
}

dependencies {
    implementation(project(":fsrs"))
    // spring
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-webflux")
    implementation("org.springframework.boot:spring-boot-starter-validation")

    // openai
    implementation("com.aallam.openai:openai-client:3.7.2")
    implementation("io.ktor:ktor-client-okhttp:2.3.11")
    runtimeOnly("org.jetbrains.kotlinx:kotlinx-coroutines-reactor:1.8.1")

    // serialization
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("org.jetbrains.kotlin:kotlin-reflect")

    // jwt
    implementation("io.jsonwebtoken:jjwt-api:0.12.3")
    implementation("io.jsonwebtoken:jjwt-impl:0.12.3")
    implementation("io.jsonwebtoken:jjwt-jackson:0.12.3")

    // swagger
    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.5.0")

    // db
    runtimeOnly("org.postgresql:postgresql")
    implementation("io.hypersistence:hypersistence-utils-hibernate-63:3.8.2")
    implementation("org.flywaydb:flyway-core:9.22.3")

    // logins
    // google api client
    implementation("com.google.api-client:google-api-client:2.2.0")

    // aws
    // TODO: aws-java-sdk 1.x entered maintenance mode and support ends on Dec 31, 2025
    implementation("com.amazonaws:aws-java-sdk-s3:1.12.780")

    // background processing
    implementation("org.jobrunr:jobrunr-spring-boot-3-starter:7.4.1")

    // test
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework.security:spring-security-test")
    testImplementation("io.mockk:mockk:1.13.10")
    testImplementation("org.postgresql:postgresql")
    testImplementation(kotlin("test"))
    testImplementation("io.kotest:kotest-runner-junit5:5.9.1")
    testImplementation("io.kotest:kotest-assertions-core:5.9.1")
    testImplementation("io.kotest.extensions:kotest-extensions-spring:1.3.0")
    testImplementation("org.mockito.kotlin:mockito-kotlin:5.4.0")
}
