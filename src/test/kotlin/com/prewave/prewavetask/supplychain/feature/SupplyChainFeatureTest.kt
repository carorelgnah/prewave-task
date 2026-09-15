package com.prewave.prewavetask.supplychain.feature

import com.prewave.prewavetask.supplychain.api.SupplyChainController.EdgeCreateRequest
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.testcontainers.service.connection.ServiceConnection
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.post
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import org.testcontainers.postgresql.PostgreSQLContainer
import tools.jackson.databind.ObjectMapper
import java.util.*

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
class SupplyChainFeatureTest(
    private val mockMvc: MockMvc,
    private val objectMapper: ObjectMapper,
) {

    @Test
    fun `should create new edge`() {
        mockMvc.createEdge(EdgeCreateRequest(source = SOURCE_UUID, target = TARGET_UUID))
            .andExpect { status { isCreated() } }
    }

    @Test
    fun `should respond with error if edge already exists`() {
        mockMvc.createEdge(EdgeCreateRequest(source = SOURCE_UUID, target = TARGET_UUID))
            .andExpect { status { isConflict() } }
    }

    @Test
    fun `should delete existing edge`() {
        TODO("Not yet implemented")
    }


    @Test
    fun `should respond with error if deleting non existent edge`() {
        TODO("Not yet implemented")
    }

    @Test
    fun `should return supply chain tree from root node`() {
        TODO("Not yet implemented")
    }

    @Test
    fun `should return error when root node was not found to fetch supply chain tree`() {
        TODO("Not yet implemented")
    }

    private fun MockMvc.createEdge(createEdgeRequest: EdgeCreateRequest) =
        post("/supply-chain/edge") {
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(createEdgeRequest)
        }

    companion object {

        @Container
        @ServiceConnection
        val postgres = PostgreSQLContainer("postgres:18")

        private val SOURCE_UUID: UUID = UUID.randomUUID()
        private val TARGET_UUID: UUID = UUID.randomUUID()
    }
}