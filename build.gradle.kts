import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("org.springframework.boot") version "3.2.4"
    id("io.spring.dependency-management") version "1.1.4"
    kotlin("jvm") version "1.9.23"
    kotlin("plugin.serialization") version "1.9.23" apply false
    kotlin("plugin.spring") version "1.9.23" apply false
    kotlin("plugin.jpa") version "1.9.23" apply false
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
}

allprojects {
    group = "com.in-out"
    version = "0.0.1-SNAPSHOT"

    repositories {
        mavenCentral()
    }
}

subprojects {
}

tasks.withType<KotlinCompile> {
    kotlinOptions {
        freeCompilerArgs += "-Xjsr305=strict"
        jvmTarget = "17"
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}
