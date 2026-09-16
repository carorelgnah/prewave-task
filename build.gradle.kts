plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.spring)
    alias(libs.plugins.spring.boot)
    alias(libs.plugins.spring.dependency.management)
    alias(libs.plugins.jooq.codegen.gradle)
}

group = "com.prewave"
version = "0.0.1-SNAPSHOT"
description = "prewave-task"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(libs.spring.boot.starter.webmvc)
    implementation(libs.spring.boot.starter.validation)
    implementation(libs.spring.boot.starter.jooq)
    implementation(libs.kotlin.reflect)
    implementation(libs.springdoc.openapi.starter.webmvc.ui)
    implementation(libs.jackson.module.kotlin)
    implementation(libs.spring.boot.flyway)
    implementation(libs.flyway.database.postgres)

    runtimeOnly(libs.postgresql)
    jooqCodegen(libs.jooq.meta.extensions)

    testImplementation(libs.spring.boot.starter.webmvc.test)
    testImplementation(libs.spring.boot.starter.test)
    testImplementation(libs.spring.boot.starter.jooq.test)
    testImplementation(libs.kotlin.test.junit5)
    testImplementation(libs.spring.boot.testcontainers)
    testImplementation(libs.testcontainers.junit.jupiter)
    testImplementation(libs.testcontainers.postgresql)
    testImplementation(libs.assertk.jvm)

    testRuntimeOnly(libs.junit.platform.launcher)
}

kotlin {
    compilerOptions {
        freeCompilerArgs.addAll("-Xjsr305=strict", "-Xannotation-default-target=param-property")
    }
}

jooq {
    configuration {
        generator {
            name = "org.jooq.codegen.KotlinGenerator"
            database {
                name = "org.jooq.meta.extensions.ddl.DDLDatabase"
                properties {
                    property { key = "scripts"; value = "src/main/resources/db/migration/*.sql" }
                    property { key = "sort";    value = "flyway" }
                    property { key = "defaultNameCase"; value = "lower" }
                }
            }

            generate { isKotlinNotNullRecordAttributes = true }
            target {
                packageName = "com.prewave.prewavetask.jooq"
                directory = layout.buildDirectory.dir("generated-src/jooq").get().asFile.path
            }
        }
    }
}

tasks.named("jooqCodegen") {
    inputs.files(fileTree("src/main/resources/db/migration"))
}

tasks.named("compileKotlin") {
    dependsOn(tasks.named("jooqCodegen"))
}

tasks.withType<Test> {
    useJUnitPlatform()
}
