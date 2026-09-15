package com.prewave.prewavetask

import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.testcontainers.service.connection.ServiceConnection
import org.testcontainers.postgresql.PostgreSQLContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers

@SpringBootTest
@Testcontainers
class PrewaveTaskApplicationTests {

    companion object {
        @Container
        @ServiceConnection
        val postgres = PostgreSQLContainer("postgres:18")
    }

    @Test
    fun contextLoads() {
    }
}
