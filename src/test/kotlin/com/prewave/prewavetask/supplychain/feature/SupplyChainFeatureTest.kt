package com.prewave.prewavetask.supplychain.feature

import assertk.all
import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isNotNull
import assertk.assertions.prop
import com.prewave.prewavetask.jooq.tables.records.EdgeRecord
import com.prewave.prewavetask.supplychain.api.SupplyChainController.EdgeCreateRequest
import com.prewave.prewavetask.supplychain.api.SupplyChainController.EdgeCreatedResponse
import com.prewave.prewavetask.supplychain.repository.EdgeRepository
import org.flywaydb.core.Flyway
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
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
import tools.jackson.module.kotlin.readValue


@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
class SupplyChainFeatureTest(
    private val mockMvc: MockMvc,
    private val flyway: Flyway,
    private val objectMapper: ObjectMapper,
    private val edgeRepository: EdgeRepository
) {
    @BeforeEach
    fun clearDatabase() {
        flyway.clean()
        flyway.migrate()
    }
    @Test
    fun `should create new edge`() {
        val response: EdgeCreatedResponse =
            mockMvc.createEdge(EdgeCreateRequest(source = SOURCE_ID, target = TARGET_ID))
                .andExpect { status { isCreated() } }
                .andReturn().response.contentAsString.let { objectMapper.readValue<EdgeCreatedResponse>(it) }

        assertThat(response).prop(EdgeCreatedResponse::id).isNotNull()

        val savedEdge = edgeRepository.getEdgeById(response.id)
        assertThat(savedEdge).isNotNull().all{
            prop(EdgeRecord::sourceId).isEqualTo(SOURCE_ID)
            prop(EdgeRecord::targetId).isEqualTo(TARGET_ID)
        }
    }

    @Test
    fun `should respond with error if edge already exists`() {
        edgeRepository.createEdge(source = SOURCE_ID, target = TARGET_ID)

        mockMvc.createEdge(EdgeCreateRequest(source = SOURCE_ID, target = TARGET_ID))
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

        private const val SOURCE_ID: String = "Node 1"
        private const val TARGET_ID: String = "Node 2"
    }
}